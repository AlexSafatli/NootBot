package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListMostReportsProcessor extends AbstractChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("ListMostReportsProcessor");
  
  private static final int NUMBER_OF_TOP_TO_SHOW = 25;
  
  public ListMostReportsProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Most Controversial Sounds", bot);
  }
  
  private List<String> getTopSounds() {
    int numberOfSoundFiles = 0;
    MessageBuilder sb = new MessageBuilder();
    sb.append("The **" + NUMBER_OF_TOP_TO_SHOW + 
          " most controversial sound files** are, in descending order:\n\n");
    List<SoundFile> soundFiles = bot.getDispatcher().getSoundFilesOrderedByNumberOfReports();
    Set<String> activeFileNames = bot.getSoundMap().keySet();
    for (SoundFile file : soundFiles) {
      if (numberOfSoundFiles >= NUMBER_OF_TOP_TO_SHOW) break;
      String name = file.getSoundFileId();
      if (activeFileNames.contains(name)) {
        sb.append("`?" + name + "` (" + file.getNumberOfReports() + ") ");
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
      for (String msg : topSounds) m(event, msg);
        LOG.info("Listed the " + NUMBER_OF_TOP_TO_SHOW + " most controverial sounds for user " + event.getAuthor().getName());
  }
  
  @Override
  public String getCommandHelpString() {
    return "`" + getPrefix() + "` - list the **" + NUMBER_OF_TOP_TO_SHOW + 
        "** most controverial sound files (most reports)";
  }

}
