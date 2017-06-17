package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListSoundsProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListSoundsProcessor");
	
	public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, "Sounds", soundPlayer);
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
	
	private List<String> getStringsForCategory(String category, List<SoundFile> soundFiles) {
		MessageBuilder b = new MessageBuilder();
		if (soundFiles != null && !soundFiles.isEmpty()) {
			for (SoundFile file : soundFiles) {
				String filename = file.getSoundFile().getName();
				String name = filename.substring(0, filename.indexOf("."));
	        	b.append("`?" + name + "` ");
			}
		}
		return b.getStrings();
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		String cat = getArgument();
        Map<String, SoundFile> soundFiles = bot.getSoundMap();
        // List the sound files.
        Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
        if (soundFiles.size() > 0) {
        	if (cat == null) {
	            m(event, "**" + soundFiles.size() + " files are stored**. They are organized in **" + 
	        			bot.getDispatcher().getNumberOfCategories() + "** categories. Type any of these to play them.\n\n");
	            // List everything uncategorized.
	            if (categoryFiles.get("Uncategorized") != null && categoryFiles.get("Uncategorized").size() > 0) {
		    		for (String s : getStringsForCategory("Uncategorized", categoryFiles.get("Uncategorized"))) {
		    			m(event, s);
		    		}
	            }
	            // Traverse category tree.
	            for (Category category : bot.getDispatcher().getCategoryTree().getChildren()) {
	            	listByCategory(category, null, categoryFiles, event);
	            }
        	} else {
        		if (bot.isASoundCategory(cat)) {
        			for (Category category : bot.getDispatcher().getCategories()) {
        				if (category.getName().equalsIgnoreCase(cat)) {
        					LOG.info("Listing sounds for category " + category.getName() + " in " + event.getGuild());
        					listByCategory(category, null, categoryFiles, event);
        					return;
        				}
        			}
        		} else {
        			m(event, formatString(Strings.NOT_FOUND, cat));
        			LOG.info(event.getAuthor() + " requested a list for category " + cat + " but it wasn't found.");
        		}
        	}
        } else m(event, "There are no sounds that can be played.");
	}

	private void listByCategory(Category category, Category parent, Map<String, List<SoundFile>> categoryFiles, MessageReceivedEvent event) {
		List<SoundFile> sounds = categoryFiles.get(category.getName());
		List<String> strings = getStringsForCategory(category.getName(), sounds);
		String title = category.getName() + " (" + sounds.size() + ")";
		if (parent != null) title += " \u2014 subcategory of " + parent.getName();
		for (String s : strings) {
			StyledEmbedMessage em = new StyledEmbedMessage(title);
			em.addDescription(s);
			embed(event, em);
		}
		if (!category.getChildren().isEmpty()) {
			for (Category child : category.getChildren()) listByCategory(child, category, categoryFiles, event);
		}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [category]` - list files (or those just for a category)";
	}
	
}