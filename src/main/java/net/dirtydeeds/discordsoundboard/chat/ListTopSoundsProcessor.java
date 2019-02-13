package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListTopSoundsProcessor extends AbstractChatCommandProcessor {

  private static final int NUMBER_TO_SHOW = 50;

  public ListTopSoundsProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Top Sounds", bot);
  }

  private List<String> getTopSounds() {
    int numberOfSoundFiles = 0;
    MessageBuilder sb = new MessageBuilder();
    sb.append("The **" + NUMBER_TO_SHOW +
              " top played sound files** are, in descending order:\n\n");
    List<SoundFile> soundFiles =
      bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
    Set<String> activeFileNames = bot.getSoundMap().keySet();
    for (SoundFile file : soundFiles) {
      if (numberOfSoundFiles >= NUMBER_TO_SHOW)
        break;
      String name = file.getSoundFileId();
      if (activeFileNames.contains(name) && file.getNumberOfPlays() > 0) {
        sb.append("`?" + name + "` (**" + file.getNumberOfPlays() + "**) ");
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
    List<String> topSounds = getTopSounds();
    for (String s : topSounds) m(event, s);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list the " + NUMBER_TO_SHOW +
           " top played sound files";
  }

}
