package net.dirtydeeds.discordsoundboard.chat;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListNewSoundsProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListNewSoundsProcessor");
	
	public ListNewSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}
	
	private String getNewSounds(Collection<SoundFile> soundFiles) {
		if (soundFiles.isEmpty()) return null;
		boolean foundNewSound = false;
		StringBuilder sb = new StringBuilder();
		for (SoundFile file : soundFiles) {
			Date lastModified = new Date(file.getSoundFile().lastModified());
			if (!lastModified.after(new Date(System.currentTimeMillis()-48*60*60*1000)))
				continue;
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	sb.append("`?").append(name).append("` ");
        	foundNewSound = true;
		}
		if (!foundNewSound) return null;
		return sb.toString();
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
        Map<String, SoundFile> soundFiles = bot.getAvailableSoundFiles();
        String newSounds = getNewSounds(soundFiles.values());
        if (soundFiles.size() > 0 && newSounds != null) {
        	event.getChannel().sendMessageAsync("The **newest sound files** added (in the last 48h) were:\n\n" + newSounds, null);
            LOG.info("Listed new sounds for user " + event.getAuthor().getUsername());
        } else {
        	event.getChannel().sendMessageAsync("There were no **new sounds** found (from the last 48h).", null);
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - lists all new sound files from last 48h";
	}

}
