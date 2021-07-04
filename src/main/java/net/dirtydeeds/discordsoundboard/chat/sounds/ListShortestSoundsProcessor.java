package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.AbstractChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

public class ListShortestSoundsProcessor extends AbstractChatCommandProcessor {

  private static final int NUMBER_TO_SHOW = 50;

  public ListShortestSoundsProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "Shortest Sounds", bot);
    commands.addCommands(new CommandData("shortest", "List shortest duration sound files."));
  }

  private List<String> getShortestSounds() {
    int numberOfSoundFiles = 0;
    MessageBuilder sb = new MessageBuilder();
    sb.append("The **" + NUMBER_TO_SHOW +
              " shortest sound files** are, in ascending order:\n\n");
    List<SoundFile> soundFiles =
      bot.getDispatcher().getSoundFilesOrderedByDuration();
    Set<String> activeFileNames = bot.getSoundMap().keySet();
    for (int i = soundFiles.size() - 1; i >= 0; --i) {
      if (numberOfSoundFiles >= NUMBER_TO_SHOW) break;
      SoundFile file = soundFiles.get(i);
      String name = file.getSoundFileId();
      if (activeFileNames.contains(name)) {
        sb.append("`?" + name + "` (**" + file.getDuration() + "**s) ");
        ++numberOfSoundFiles;
      }
    }
    return sb.getStrings();
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    Map<String, SoundFile> soundFiles = bot.getSoundMap();
    if (soundFiles.isEmpty()) {
      e(event, "There are **no sound files** at all!");
      return;
    }
    List<String> topSounds = getShortestSounds();
    for (String msg : topSounds) m(event, msg);
  }

  protected void handleEvent(SlashCommandEvent event) {
    Map<String, SoundFile> soundFiles = bot.getSoundMap();
    if (soundFiles.isEmpty()) {
      e(event, "There are **no sound files** at all!");
      return;
    }
    List<String> topSounds = getShortestSounds();
    for (String msg : topSounds) m(event, msg);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list the " + NUMBER_TO_SHOW +
           " shortest sound files";
  }

}
