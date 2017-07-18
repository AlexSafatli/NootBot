package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListLowSoundsProcessor extends AbstractChatCommandProcessor {
  
  private static final int NUMBER_TO_SHOW = 50;
  
  public ListLowSoundsProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Least Played Sounds", bot);
  }
  
  private List<String> getLowSounds() {
    int numberOfSoundFiles = 0;
    MessageBuilder sb = new MessageBuilder();
      sb.append("The **" + NUMBER_TO_SHOW + 
          " least played sound files** are, in ascending order:\n\n");
    List<SoundFile> soundFiles = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
    Set<String> activeFileNames = bot.getSoundMap().keySet();
    for (int i = soundFiles.size() - 1; i >= 0; --i) {
      if (numberOfSoundFiles >= NUMBER_TO_SHOW) break;
      SoundFile file = soundFiles.get(i);
      String name = file.getSoundFileId();
      if (activeFileNames.contains(name)) {
        sb.append("`?" + name + "` (" + file.getNumberOfPlays() + ") ");
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
    List<String> lowSounds = getLowSounds();
    for (String s : lowSounds) m(event, s);
  }
  
  @Override
  public String getCommandHelpString() {
    return "`" + getPrefix() + "` \u2014 list the **" + NUMBER_TO_SHOW + 
        "** least played sound files";
  }

}
