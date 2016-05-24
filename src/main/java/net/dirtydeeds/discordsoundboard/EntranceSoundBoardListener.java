package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * This class handles waiting for people to enter a discord voice channel and responding to their entrance.
 */
public class EntranceSoundBoardListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Entrance");
    
    private SoundboardBot bot;
    
    public EntranceSoundBoardListener(SoundboardBot bot) {
        this.bot = bot;
    }
    
	public void onVoiceJoin(VoiceJoinEvent event) {

		User user = event.getUser();
    	if (bot.isUser(user)) return; // Ignore if it is just the bot.
    	
    	LOG.info(user.getUsername() + " joined " + event.getChannel().getName() + 
    			" in " + event.getGuild().getName());
    	
        
        if (!bot.isAllowedToPlaySound(user)) {
        	LOG.info("User " + user.getUsername() + 
        			" is not allowed to play sounds and so ignoring their entrance.");
        	return;
        }
        String fileToPlay = bot.getEntranceForUser(user);
        if (fileToPlay != null && !fileToPlay.equals("") && 
        		bot.getAvailableSoundFiles().get(fileToPlay) != null) {
        	try {
        		bot.playFileForEntrance(fileToPlay, event);
        	} catch (Exception e) {
        		LOG.fatal("Could not play file for entrance of " + user.getUsername() + 
        				" because: " + e.toString());
        	}
        }
        
    }
}