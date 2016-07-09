package net.dirtydeeds.discordsoundboard.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.VoiceUtils;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
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
    private Map<Guild,Queue<EntranceEvent>> pastEntrances;
    
    public EntranceListener(SoundboardBot bot) {
        this.bot = bot;
        this.pastEntrances = new HashMap<>();
    }
    
    private class EntranceEvent {
    	public Message message;
    	public User user;
    	public EntranceEvent(Message m, User u) {
    		message = m; user = u;
    	}
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
        	if (bot.getSoundMap().get(fileToPlay) == null) {
        		user.getPrivateChannel().sendMessageAsync("**Uh oh!** Your entrance `" + fileToPlay + 
        				"` doesn't exist anymore. *Update it!*", null);
        		LOG.info(user.getUsername() + " has stale entrance. Alerted and clearing.");
        		bot.setEntranceForUser(user, null);
        	} else {
        		boolean userHasHeardEntranceRecently = false;
        		String soundInfo = "";
    			Guild guild = event.getGuild();
    			// Clear previous message(s).
        		if (pastEntrances.get(guild) == null) {
        			pastEntrances.put(guild, new LinkedList<EntranceEvent>());
        		} else {
        			Queue<EntranceEvent> entrances = pastEntrances.get(guild);
        			while (!entrances.isEmpty()) {
        				EntranceEvent entrance = entrances.poll();
        				entrance.message.deleteMessage();
        				if (entrance.user.equals(user) && !userHasHeardEntranceRecently) {
        					userHasHeardEntranceRecently = true;
        					LOG.info("User has heard entrance recently.");
        				}
        			}
        		}
        		// Play a sound if there are others to hear it or this person has not heard it recently.
    			if (VoiceUtils.numUsersInVoiceChannels(guild) > 1 || !userHasHeardEntranceRecently) {
		        	try {
		        		if (bot.playFileForEntrance(fileToPlay, event)) {
		        			SoundFile sound = bot.getDispatcher().getSoundFileByName(fileToPlay);
		        			String desc = sound.getDescription();
		        			if (desc != null && !desc.isEmpty()) {
		        				desc = "(" + desc + ") ";
		        			} else desc = "";
		        			soundInfo = " Your entrance `" + fileToPlay + "` " + desc + 
		        					"has now played **" + sound.getNumberOfPlays() + "** times.";
		        		}
		        	} catch (Exception e) {
		        		e.printStackTrace();
		        		LOG.fatal("Could not play entrance.");
		        	}
    			} else if (bot.getConnectedChannel(guild) == null) {
    				bot.moveToChannel(event.getChannel()); // Move to channel otherwise.
    			}
    			// Send a message greeting them into the server.
    			guild.getPublicChannel().sendMessageAsync(
    					"**Welcome, " + user.getAsMention() + "**!" + soundInfo,
    						(Message m)-> pastEntrances.get(guild).add(new EntranceEvent(m, user)));
        	}
        }
        
    }

}