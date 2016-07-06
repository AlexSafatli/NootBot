package net.dirtydeeds.discordsoundboard.chat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListSoundsProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListSoundsProcessor");
	private Map<MessageChannel,List<Message>> pastMessages;
	
	public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
		this.pastMessages = new HashMap<>();
	}

	private Map<String, List<SoundFile>> getCategoryMappings() {
		Map<String, List<SoundFile>> categoryFiles = new TreeMap<String, List<SoundFile>>();
		for (SoundFile file : bot.getSoundMap().values()) {
			String category = (file.getCategory().equalsIgnoreCase("sounds")) ? "Uncategorized" : file.getCategory();
			if (categoryFiles.get(category) == null) {
				categoryFiles.put(category, new LinkedList<SoundFile>()); 
			}
			categoryFiles.get(category).add(file);
		}
		return categoryFiles;
	}
	
	private List<String> getMessagesForCategory(String category, List<SoundFile> soundFiles) {
		MessageBuilder b = new MessageBuilder();
		b.append("**" + category + "** (" + soundFiles.size() + ")\n");
		for (SoundFile file : soundFiles) {
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	b.append("`?" + name + "` ");
		}
		return b.getStrings();
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		String cat = getArgument();
		MessageChannel channel = event.getChannel();
        Map<String, SoundFile> soundFiles = bot.getSoundMap();
        Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
        List<String> categories = bot.getSoundCategories();
        // Clear past list message.
        if (!event.isPrivate() && bot.hasPermissionInChannel((TextChannel)channel, Permission.MESSAGE_MANAGE)) {
        	List<Message> past = pastMessages.get(channel);
        	if (past != null && past.size() > 0) {
        		((TextChannel)channel).deleteMessages(past);
        		past.clear();
        	}
        }
        // List the sound files.
        if (soundFiles.size() > 0) {
        	if (pastMessages.get(channel) == null) pastMessages.put(channel, new LinkedList<Message>());
        	if (cat == null) {
	        	String s = "**" + soundFiles.size() + " files are stored**. They are organized in **" + 
	        			categories.size() + "** categories. Type any of these to play them.\n\n";
	            channel.sendMessageAsync(s, (Message m)-> pastMessages.get(channel).add(m));
	            for (String category : categories) {
	            	for (String msg : getMessagesForCategory(category, categoryFiles.get(category))) {
	                	channel.sendMessageAsync(msg, (Message m)-> pastMessages.get(channel).add(m));
	            	}
	            }
        	} else {
        		if (bot.isASoundCategory(cat)) {
        			for (String category : categories) {
        				if (category.equalsIgnoreCase(cat)) {
        					LOG.info("Listing sounds for category " + category + " in " + event.getGuild());
        					for (String msg : getMessagesForCategory(category, categoryFiles.get(category))) {
        						channel.sendMessageAsync(msg, null);
        					}
        					break;
        				}
        			}
        		} else {
        			channel.sendMessageAsync("No category `" + cat + "` was found.", null);
        			LOG.info(event.getAuthor() + " requested a list for category " + cat + " but it wasn't found.");
        		}
        	}
        } else channel.sendMessageAsync("There are no sounds that can be played.", null);
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [category]` - list all files or all those for a category";
	}
	
}
