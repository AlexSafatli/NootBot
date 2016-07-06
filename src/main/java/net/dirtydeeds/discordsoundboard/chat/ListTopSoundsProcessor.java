package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListTopSoundsProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListTopSoundsProcessor");
	
	private static final int NUMBER_OF_TOP_TO_SHOW = 15;
	
	public ListTopSoundsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	private String getTopSounds() {
		int numberOfSoundFiles = 0;
		StringBuilder sb = new StringBuilder();
		List<SoundFile> soundFiles = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
		Set<String> activeFileNames = bot.getSoundMap().keySet();
		for (SoundFile file : soundFiles) {
			if (numberOfSoundFiles >= NUMBER_OF_TOP_TO_SHOW) break;
			String name = file.getSoundFileId();
			if (activeFileNames.contains(name)) {
				sb.append("`?").append(name).append("` (" + file.getNumberOfPlays() + ") ");
        		++numberOfSoundFiles;
			}
		}
		return sb.toString();
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		MessageChannel channel = event.getChannel();
        Map<String, SoundFile> soundFiles = bot.getSoundMap();
        if (soundFiles.isEmpty()) {
        	channel.sendMessage("There are **no sound files** at all!");
        	return;
        }
        String topSounds = getTopSounds();
    	channel.sendMessageAsync("The **" + NUMBER_OF_TOP_TO_SHOW + 
    			" top played sound files** are, in descending order:\n\n" + topSounds, null);
        LOG.info("Listed the " + NUMBER_OF_TOP_TO_SHOW + " top sounds for user " + event.getAuthor().getUsername());
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - list the **" + NUMBER_OF_TOP_TO_SHOW + 
				"** top sound files based on number of plays";
	}

}
