package net.dirtydeeds.discordsoundboard.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dirtydeeds.discordsoundboard.utils.VoiceUtils;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * This class handles waiting for people to enter a discord voice channel and responding to their entrance.
 */
public class MoveListener extends AbstractListener {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Move");
    
    private Map<Guild,Queue<EntranceEvent>> pastEntrances;
    
    public MoveListener(SoundboardBot bot) {
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
    
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		onJoin(event.getChannelJoined(), event.getMember().getUser());
    }
	
	private void onJoin(VoiceChannel voiceChannel, User user) {
		Guild guild = voiceChannel.getGuild();
		VoiceChannel afkChannel = guild.getAfkChannel();
    	
		if (bot.isUser(user)) {
			if (voiceChannel.getMembers().size() == 1) {
				LOG.info("Moved to an empty channel. Closing audio connection.");
				guild.getAudioManager().closeAudioConnection();
			}
			return;
		} else if (user.isBot()) {
			return;
		}
		
    	LOG.info(user.getName() + " joined " + voiceChannel.getName() + 
    			" in " + guild.getName());
        
        if (!bot.isAllowedToPlaySound(user)) {
        	LOG.info("User " + user.getName() + " cannot play sounds. Ignoring.");
        	return;
        } else if (afkChannel != null && afkChannel.getId().equals(voiceChannel.getId())) {
        	LOG.info("User " + user.getName() + " joined an AFK channel. Ignoring.");
        	return;
        }
        
        String fileToPlay = bot.getEntranceForUser(user);
        if (fileToPlay != null && !fileToPlay.isEmpty()) {
        	if (bot.getSoundMap().get(fileToPlay) == null) {
        		if (!bot.isUser(user))
        			user.getPrivateChannel().sendMessage("**Uh oh!** Your entrance `" + fileToPlay + 
        					"` doesn't exist anymore. *Update it!*");
        		LOG.info(user.getName() + " has stale entrance. Alerted and clearing.");
        		bot.setEntranceForUser(user, null);
        	} else {
        		boolean userHasHeardEntranceRecently = false;
        		String soundInfo = "";
    			// Clear previous message(s).
        		if (pastEntrances.get(guild) == null) {
        			pastEntrances.put(guild, new LinkedList<EntranceEvent>());
        		} else {
        			Queue<EntranceEvent> entrances = pastEntrances.get(guild);
        			while (!entrances.isEmpty()) {
        				EntranceEvent entrance = entrances.poll();
        				entrance.message.deleteMessage().queue();
        				if (entrance.user.equals(user) && !userHasHeardEntranceRecently) {
        					userHasHeardEntranceRecently = true;
        					LOG.info("User has heard entrance recently.");
        				}
        			}
        		}
        		// Play a sound if there are others to hear it or this person has not heard it recently.
    			if (VoiceUtils.numUsersInVoiceChannels(guild) > 1 || !userHasHeardEntranceRecently) {
		        	try {
		        		if (bot.playFileForEntrance(fileToPlay, user, voiceChannel)) {
		        			SoundFile sound = bot.getDispatcher().getSoundFileByName(fileToPlay);
		        			soundInfo = " Played sound " + formatString(Strings.SOUND_DESC, fileToPlay, sound.getCategory(),
		        					sound.getNumberOfPlays()) + ".";
		        		}
		        	} catch (Exception e) {
		        		e.printStackTrace();
		        		LOG.fatal("Could not play entrance.");
		        	}
    			} else if (bot.getConnectedChannel(guild) == null) {
    				bot.moveToChannel(voiceChannel); // Move to channel otherwise.
    			}
    			// Send a message greeting them into the server.
    			if (bot.getConnectedChannel(guild) == null || bot.getConnectedChannel(guild).equals(voiceChannel)) {
	    			embed(guild.getPublicChannel(), welcomeMessage(user, voiceChannel, soundInfo),
	    					(Message m)-> pastEntrances.get(guild).add(new EntranceEvent(m, user)));
    			}
        	}
        }
	}
	
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    	onLeave(event.getChannelLeft(), event.getMember().getUser());
    }
	
	private void onLeave(VoiceChannel channel, User user) {
    	if (bot.isUser(user) || bot.getConnectedChannel(channel.getGuild()) == null)
    		return; // Ignore if it is just the bot or not even connected.
    	Guild guild = channel.getGuild();
    	AudioManager voice = guild.getAudioManager();
    	VoiceChannel botsChannel = voice.getConnectedChannel();
    	LOG.info(user.getName() + " left " + channel.getName() + 
    			" in " + guild.getName());
    	
    	if (botsChannel != null && VoiceUtils.numUsersInVoiceChannels(guild) == 0) {
            LOG.info("No more users! Leaving voice channel in server " + guild.getName());
            voice.closeAudioConnection();
    	} else if (botsChannel != null && botsChannel.getMembers().size() == 1) {
            for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            	if (botsChannel != null && botsChannel.equals(voiceChannel)) continue;
            	else if (voiceChannel.getMembers().size() > 0 && (guild.getAfkChannel() == null || !voiceChannel.getId().equals(guild.getAfkChannel().getId()))) {
        			if (voiceChannel.getMembers().size() == 1 && voiceChannel.getMembers().get(0).getUser().isBot()) {
        				continue;
        			}
        			bot.moveToChannel(voiceChannel);
        			LOG.info("Moving to voice channel " + voiceChannel.getName() + " in server " + guild.getName());
        			return;
            	}
            }	
    	}
	}
	
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		VoiceChannel botsChannel = bot.getConnectedChannel(event.getGuild());
		if (botsChannel != null && event.getChannelLeft().equals(botsChannel)) {
			onLeave(event.getChannelLeft(), event.getMember().getUser());
		}
		onJoin(event.getChannelJoined(), event.getMember().getUser());
	}
	
	public StyledEmbedMessage welcomeMessage(User user, Channel channel, String soundInfo) {
		StyledEmbedMessage m = new StyledEmbedMessage("Welcome, " + user.getName() + "!");
		if (!soundInfo.isEmpty()) {
			m.addDescription(soundInfo + " \u2014 " + user.getAsMention());
		} else {
			m.addDescription("I am " + bot.getBotName() + " and I am here to serve your whims.");
		}
		m.addContent("How Do I Use This Bot?", "Start by typing `.help`. That will give you a list of commands!", false);
		return m;
	}

}