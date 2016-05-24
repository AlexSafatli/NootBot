package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListSoundsProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListSoundsProcessor");
    private static final int MAX_MESSAGE_LENGTH = 2000;
	
	public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	private Map<String, List<SoundFile>> getCategoryMappings() {
		LOG.info("Constructing list of files for categories.");
		Map<String, List<SoundFile>> categoryFiles = new TreeMap<String, List<SoundFile>>();
		for (SoundFile file : bot.getAvailableSoundFiles().values()) {
			String category = (file.getCategory().equalsIgnoreCase("sounds")) ? "Uncategorized" : file.getCategory();
			if (categoryFiles.get(category) == null) {
				categoryFiles.put(category, new LinkedList<SoundFile>()); 
			}
			categoryFiles.get(category).add(file);
		}
		return categoryFiles;
	}
	
	private List<String> getMessagesForCategory(String category, List<SoundFile> soundFiles) {
		List<String> strings = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("**" + category + "**\n");
		for (SoundFile file : soundFiles) {
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	int lengthOfAdd = 3 + name.length();
        	// Avoid oversized messages.
            if (sb.length() + lengthOfAdd >= MAX_MESSAGE_LENGTH) {
            	strings.add(sb.toString());
            	sb = new StringBuilder();
            }
        	sb.append("`?").append(name).append("` ");
		}
		if (sb.length() > 0) strings.add(sb.toString());
		return strings;
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		String cat = getArgument();
        Map<String, SoundFile> soundFiles = bot.getAvailableSoundFiles();
        Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
        List<String> categories = bot.getSoundCategories();
        if (soundFiles.size() > 0) {
        	if (cat == null) {
        		StringBuilder sb = new StringBuilder();
	        	sb.append(soundFiles.size()).append(" files found. ");
	            sb.append("They are organized by category. Type any of these commands to play the sound.\n\n");
	            event.getChannel().sendMessageAsync(sb.toString(), null);
	            for (String category : categories) {
	            	for (String msg : getMessagesForCategory(category, categoryFiles.get(category))) {
	                	event.getChannel().sendMessageAsync(msg, null);
	            	}
	            }
        	} else {
        		if (bot.isASoundCategory(cat)) {
        			for (String category : categories) {
        				if (category.equalsIgnoreCase(cat)) {
        					LOG.info("Listing sounds for category " + category + " in " + event.getGuild());
        					for (String msg : getMessagesForCategory(category, categoryFiles.get(category))) {
        						event.getChannel().sendMessageAsync(msg, null);
        					}
        					break;
        				}
        			}
        		} else {
        			event.getChannel().sendMessageAsync("No category `" + cat + "` was found to list sounds for.", null);
        			LOG.info(event.getAuthor() + " requested a list for category " + cat + " but it wasn't found.");
        		}
        	}
        } else event.getChannel().sendMessageAsync("There are no available sounds to play.", null);
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - lists all files or all those for a category";
	}
	
}
