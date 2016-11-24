package net.dirtydeeds.discordsoundboard.chat;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListNewSoundsProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListNewSoundsProcessor");
	
	private static final int MIN_NUMBER_OF_HOURS = 168; // 7 days
	private static final int MAX_NUMBER_OF_HOURS = 504; // 3 weeks
	private static final int NUM_HOURS_FOR_DAY_TRANSFORM = 72; // 3 days
	private static final int DAYS = 24;
	
	public ListNewSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}
	
	private Map<String, List<SoundFile>> getCategoryMappings(Collection<SoundFile> newSounds) {
		Map<String, List<SoundFile>> categoryFiles = new TreeMap<String, List<SoundFile>>();
		for (SoundFile file : newSounds) {
			String category = (file.getCategory().equalsIgnoreCase("sounds")) ? "Uncategorized" : file.getCategory();
			if (categoryFiles.get(category) == null) {
				categoryFiles.put(category, new LinkedList<SoundFile>()); 
			}
			categoryFiles.get(category).add(file);
		}
		return categoryFiles;
	}
	
	private List<String> getMessagesForCategory(String category, Collection<SoundFile> soundFiles) {
		MessageBuilder b = new MessageBuilder();
		b.append("**" + category + "** (" + soundFiles.size() + ")\n");
		for (SoundFile file : soundFiles) {
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	b.append("`?" + name + "` ");
		}
		return b.getStrings();
	}
	
	private Collection<SoundFile> getNewSounds(Collection<SoundFile> soundFiles, int numHours) {
		List<SoundFile> newSounds = new LinkedList<>();
		for (SoundFile file : soundFiles) {
			Date lastModified = new Date(file.getSoundFile().lastModified());
			if (!lastModified.after(new Date(System.currentTimeMillis()-numHours*60*60*1000)))
				continue;
			newSounds.add(file);
		}
		return newSounds;
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		MessageChannel channel = event.getChannel();
        Map<String, SoundFile> soundFiles = bot.getSoundMap();
        if (soundFiles.isEmpty()) {
        	channel.sendMessage("There are **no sound files** at all!");
        	return;
        }
    	String timeType = "hours";
        int numHours = MIN_NUMBER_OF_HOURS;
        Collection<SoundFile> newSounds = null;
        while (numHours <= MAX_NUMBER_OF_HOURS && newSounds == null) {
	        newSounds = getNewSounds(soundFiles.values(), numHours);
	        if (newSounds != null && !newSounds.isEmpty()) {
	        	int numTime = numHours;
	        	if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
	        		numTime /= DAYS;
	        		timeType = "days";
	        	}
	        	channel.sendMessageAsync("The **newest sound files** added (in the last " + numTime + 
	        			" " + timeType + ") were:\n\n", null);
	        	Map<String, List<SoundFile>> catMap = getCategoryMappings(newSounds);
	        	for (String category : catMap.keySet()) {
	            	for (String msg : getMessagesForCategory(category, catMap.get(category))) {
	                	channel.sendMessageAsync(msg, null);
	            	}
	            }
	            LOG.info("Listed new sounds in last " + numHours + " hours for user " + 
	        			event.getAuthor().getUsername());
	        } else {
	        	numHours += 48; // Add 2 days.
	        }
        }
        if (newSounds == null) {
        	int numTime = numHours;
        	if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
        		numTime /= DAYS;
        		timeType = "days";
        	}
	        channel.sendMessageAsync("There were no **new sounds** found (from the last " + 
	        		numTime + " " + timeType + ").", null);
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - list the newest sound files at least **" + 
				MIN_NUMBER_OF_HOURS/DAYS + "** days old";
	}

}