package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.Map;
import java.util.Set;

/**
 * @author dfurrer.
 *
 * This class handles listening to commands in discord text channels and responding to them.
 */
public class ChatSoundBoardListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("ChatListener");
    private static final int MAX_FILES_TO_LIST_IN_SINGLE_MESSAGE = 50;
    
    private SoundPlayerImpl soundPlayer;

    public ChatSoundBoardListener(SoundPlayerImpl soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    @SuppressWarnings("rawtypes")
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {      
    	
        String message = event.getMessage().getContent().toLowerCase();
        StringBuilder sb = new StringBuilder();

        //Respond
        if (message.startsWith("?list")) {
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
                    	currentFileCount = 0;
                    }
                }
                if (currentFileCount > 0) {
                	event.getChannel().sendMessage(sb.toString());
                }
                LOG.info("Responding to list request.");
            } else {
                sb.append("There are no available sounds to play.");
                event.getChannel().sendMessage(sb.toString());
            }
        //If the command is not list and starts with ? try and play that "command" or sound file.
        } else if (message.startsWith("?")) {
            try {
                String fileNameRequested = message.substring(1, message.length());
                LOG.info("Attempting to play file: " + fileNameRequested + ".");
                soundPlayer.playFileForChatCommand(fileNameRequested, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (message.startsWith(".random")) {
        	try {
        		String fileName = soundPlayer.playRandomFile(event);
        		event.getChannel().sendMessage("Played random sound file `" + fileName + "`.");
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        if (message.startsWith(".") || message.startsWith("?")) event.getMessage().deleteMessage();
        
    }
}
