package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListSoundsProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListSoundsProcessor");
	private static final int MAX_LINE_LENGTH    = 50;
    private static final int MAX_MESSAGE_LENGTH = 2000;
	
	public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	private Map<String, List<SoundFile>> getCategoryMappings() {
		Map<String, List<SoundFile>> categoryFiles = new TreeMap<String, List<SoundFile>>();
		for (SoundFile file : soundPlayer.getAvailableSoundFiles().values()) {
			String category = (file.getCategory().equalsIgnoreCase("sounds")) ? "Uncategorized" : file.getCategory();
			if (categoryFiles.get(category) == null) {
				categoryFiles.put(category, new LinkedList<SoundFile>()); 
				LOG.info("Constructing list of files for category: " + category);
			}
			categoryFiles.get(category).add(file);
		}
		return categoryFiles;
	}
	
	private List<String> getMessagesForCategory(String category, List<SoundFile> soundFiles) {
		List<String> strings = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("**" + category + "**\n");
		int currentLineSize = 0;
		for (SoundFile file : soundFiles) {
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	int lengthOfAdd = 3 + name.length();
        	// Keep a maximum line size of 80 characters.
        	if (currentLineSize + lengthOfAdd > MAX_LINE_LENGTH) {
        		sb.append("\n"); currentLineSize = 0;
        	}
        	// Avoid oversized messages.
            if (sb.length() + lengthOfAdd >= MAX_MESSAGE_LENGTH) {
            	strings.add(sb.toString());
            	sb = new StringBuilder();
            	currentLineSize = sb.length();
            }
            currentLineSize += lengthOfAdd;
        	sb.append("`?").append(name).append("` ");
		}
		if (sb.length() > 0) {
			strings.add(sb.toString());
		}
		return strings;
	}
	
	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
		StringBuilder sb = new StringBuilder();
        Map<String, SoundFile> soundFiles = soundPlayer.getAvailableSoundFiles();
        Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
        List<String> categories = soundPlayer.getSoundCategories();
        if (soundFiles.size() > 0) {
        	sb.append(soundFiles.size()).append(" files found. ");
            sb.append("They are organized by category. Type any of these commands to play the sound.\n\n");
            event.getChannel().sendMessage(sb.toString());
            for (String category : categories) {
            	for (String msg : getMessagesForCategory(category, categoryFiles.get(category))) {
                	event.getChannel().sendMessage(msg);
            	}
            }
            LOG.info("Responded to list request.");
        } else {
            event.getChannel().sendMessage("There are no available sounds to play.");
        }
	}

}
