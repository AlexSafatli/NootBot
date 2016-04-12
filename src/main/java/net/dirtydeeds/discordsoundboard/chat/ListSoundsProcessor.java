package net.dirtydeeds.discordsoundboard.chat;

import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListSoundsProcessor extends ChatSoundBoardProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListSoundsProcessor");
    private static final int MAX_FILES_TO_LIST_IN_SINGLE_MESSAGE = 50;
	
	public ListSoundsProcessor(String prefix, SoundPlayerImpl soundPlayer) {
		super(prefix, soundPlayer);
	}

	@SuppressWarnings("rawtypes")
	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
    	StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, SoundFile>> entrySet = soundPlayer.getAvailableSoundFiles().entrySet();
        if (entrySet.size() > 0) {
        	int currentFileCount = 0;
        	sb.append(entrySet.size()).append(" files found. ");
            sb.append("Type any of the following to play the sound:\n\n```");
            for (Map.Entry entry : entrySet) {
                ++currentFileCount;
            	sb.append("?").append(entry.getKey()).append("\n");
            	// Keep a maximum list of 50 files to show to avoid oversized messages.
                if (currentFileCount >= MAX_FILES_TO_LIST_IN_SINGLE_MESSAGE) {
                	sb.append("```");
                	event.getChannel().sendMessage(sb.toString());
                	sb = new StringBuilder();
                	sb.append("```");
                	currentFileCount = 0;
                }
            }
            if (currentFileCount > 0) {
            	event.getChannel().sendMessage(sb.toString());
            }
            LOG.info("Responding to list request.");
        } else {
            event.getChannel().sendMessage("There are no available sounds to play.");
        }
	}

}
