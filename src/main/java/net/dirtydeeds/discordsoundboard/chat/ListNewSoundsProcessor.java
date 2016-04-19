package net.dirtydeeds.discordsoundboard.chat;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListNewSoundsProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListNewSoundsProcessor");
	
	public ListNewSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}
	
	private String getNewSounds(Collection<SoundFile> soundFiles) {
		StringBuilder sb = new StringBuilder();
		for (SoundFile file : soundFiles) {
			Date lastModified = new Date(file.getSoundFile().lastModified());
			if (!lastModified.after(new Date(System.currentTimeMillis()-48*60*60*1000)))
				continue;
			String filename = file.getSoundFile().getName();
			String name = filename.substring(0, filename.indexOf("."));
        	sb.append("`?").append(name).append("` ");
		}
		return sb.toString();
	}
	
	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
        Map<String, SoundFile> soundFiles = bot.getAvailableSoundFiles();
        if (soundFiles.size() > 0) {
            bot.sendMessageToChannel("The newest sound files added (in the last 48h) were:\n\n", event.getChannel());
            bot.sendMessageToChannel(getNewSounds(soundFiles.values()), event.getChannel());
            LOG.info("Responded to list new sounds request.");
        } else {
            bot.sendMessageToChannel("There are no available sounds to play.", event.getChannel());
        }
	}

}
