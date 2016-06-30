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
public class EntranceListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Entrance");
    
    private SoundboardBot bot;
    
    public EntranceListener(SoundboardBot bot) {
        this.bot = bot;
    }
    
	public void onVoiceJoin(VoiceJoinEvent event) {

		User user = event.getUser();
    	if (bot.isUser(user)) return; // Ignore if it is just the bot.
    	
    	LOG.info(user.getUsername() + " joined " + event.getChannel().getName() + 
    			" in " + event.getGuild().getName());
    	
        
        if (!bot.isAllowedToPlaySound(user)) {
        	LOG.info("User " + user.getUsername() + " cannot play sounds so ignoring entrance.");
        	return;
        }
        
        String fileToPlay = bot.getEntranceForUser(user);
        if (fileToPlay != null && !fileToPlay.equals("")) {
        	if (bot.getAvailableSoundFiles().get(fileToPlay) == null) {
        		user.getPrivateChannel().sendMessageAsync("**Uh oh!** Your entrance `" + fileToPlay + 
        				"` does not seem to exist anymore. You should update it!", null);
        		LOG.info("User " + user.getUsername() + " seems to have a stale entrance. Alerted them.");
        	} else {
	        	try {
	        		bot.playFileForEntrance(fileToPlay, event);
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        		LOG.fatal("Could not play file for entrance.");
	        	}
        	}
        }
        
    }
	
}