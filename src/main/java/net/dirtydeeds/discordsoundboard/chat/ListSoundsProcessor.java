package net.dirtydeeds.discordsoundboard.chat;

import java.util.Map;
import java.util.Set;

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

	@SuppressWarnings("rawtypes")
	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
    	StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, SoundFile>> entrySet = soundPlayer.getAvailableSoundFiles().entrySet();
        if (entrySet.size() > 0) {
        	int currentLineSize = 0;
        	sb.append(entrySet.size()).append(" files found. ");
            sb.append("Type any of the following to play the sound.\n\n```");
            for (Map.Entry entry : entrySet) {
            	String name = (String)entry.getKey();
            	int lengthOfAdd = 2 + name.length();
            	// Keep a maximum line size of 80 characters.
            	if (currentLineSize + lengthOfAdd > MAX_LINE_LENGTH) {
            		sb.append("\n"); currentLineSize = 0;
            	}
            	// Avoid oversized messages.
                if (sb.length() + lengthOfAdd + 3 >= MAX_MESSAGE_LENGTH) {
                	sb.append("```");
                	event.getChannel().sendMessage(sb.toString());
                	sb = new StringBuilder();
                	sb.append("```");
                	currentLineSize = sb.length();
                }
                currentLineSize += lengthOfAdd;
            	sb.append("?").append(name).append(" ");
            }
            if (sb.length() > 3) {
            	sb.append("```");
            	event.getChannel().sendMessage(sb.toString());
            }
            LOG.info("Responding to list request.");
        } else {
            event.getChannel().sendMessage("There are no available sounds to play.");
        }
	}

}
