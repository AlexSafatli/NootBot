package net.dirtydeeds.discordsoundboard;

import java.util.Map;
import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * This class handles waiting for people to enter a discord voice channel and responding to their entrance.
 */
public class EntranceSoundBoardListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("EntranceListener");
    
    private SoundboardBot soundPlayer;

    public EntranceSoundBoardListener(SoundboardBot soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    @SuppressWarnings("rawtypes")
	public void onVoiceJoin(VoiceJoinEvent event) {        
    	
        String joined = event.getUser().getUsername().toLowerCase();

        //Respond
        Set<Map.Entry<String, SoundFile>> entrySet = soundPlayer.getAvailableSoundFiles().entrySet();
        if (entrySet.size() > 0) {
        	String fileToPlay = "";
            for (Map.Entry entry : entrySet) {
            	String fileEntry = (String)entry.getKey();
                if (joined.startsWith(fileEntry) && fileEntry.length() > fileToPlay.length()) {
                	fileToPlay = fileEntry;
                }
            }
            if (!fileToPlay.equals("")) {
            	LOG.info("Responding to entrance of " + joined + " with " + fileToPlay);
            	try {
            		soundPlayer.playFileForEntrance(fileToPlay, event);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            } else {
            	LOG.info("Could not find any sound that starts with " + joined + ", so ignoring.");
            }
        }
    }
}