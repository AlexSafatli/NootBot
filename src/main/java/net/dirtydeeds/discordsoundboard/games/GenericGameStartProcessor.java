package net.dirtydeeds.discordsoundboard.games;

import java.util.Date;

import net.dirtydeeds.discordsoundboard.games.AbstractGameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class GenericGameStartProcessor extends AbstractGameUpdateProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("GameStartProcessor");
	
	private static final int MIN_NUM_PLAYERS = 3;
	private static final int NUMBER_SEC_BETWEEN = 3;
	private static final String POST_SCRIPT = "\nTo see other popular sounds, type `.top` into this channel.";
	
	private GameStartEvent pastEvent;
	
	private class GameStartEvent {
		public VoiceChannel channel;
		public Date time;
		public Message message;
		public GameStartEvent(VoiceChannel channel, Date time, Message msg) {
			this.channel = channel;
			this.time = time;
			this.message = msg;
		}
	}
	
	public GenericGameStartProcessor(SoundboardBot bot) {
		super(bot);
	}
	
	public boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user) {
		VoiceChannel channel = null;
		try {
			channel = bot.getUsersVoiceChannel(user);
		} catch (Exception e) {
			LOG.fatal("Problem retrieving voice channel for user.");
		}
		return (user.getCurrentGame() != null && event.getPreviousGame() == null // started a game
				&& channel != null && channel.getUsers().size() >= MIN_NUM_PLAYERS);
	}
	
	protected void handleEvent(UserGameUpdateEvent event, User user) {
		int numPlayers = 0;
		String game = user.getCurrentGame().getName();
		VoiceChannel channel = null;
		try { channel = bot.getUsersVoiceChannel(user); } catch (Exception e) { return; }
		// See if there have been multiple people that started playing the game in a channel.
		// If so: play a sound randomly from top played sounds.
		for (User u : channel.getUsers()) {
			if (u.getCurrentGame() != null && u.getCurrentGame().getName().equals(game)) {
				++numPlayers;
			}
		}
		if (numPlayers >= MIN_NUM_PLAYERS) {
			LOG.info("Found " + user.getUsername() + " and " + numPlayers + 
					" other users playing " + game + " in channel " + channel);
			Date now = new Date(System.currentTimeMillis());
			if (pastEvent != null && pastEvent.channel != null && pastEvent.channel.equals(channel)) {
		    	long secSince = (now.getTime() - pastEvent.time.getTime())/1000;
		    	if (secSince < NUMBER_SEC_BETWEEN) {
		    		LOG.info("Not enough time since last event in this channel."); return;
		    	}
			}
			TextChannel publicChannel = channel.getGuild().getPublicChannel();
			if (pastEvent != null && pastEvent.message != null) pastEvent.message.deleteMessage();
			pastEvent = new GameStartEvent(channel, now, null);
			try {
				String filePlayed = bot.getRandomTopPlayedSoundName();
				if (filePlayed != null) {
					bot.playFileForUser(filePlayed, user);
					publicChannel.sendMessageAsync(String.format("Played popular sound `%s` randomly. "
							+ "Others in the channel are playing *%s* too ",filePlayed,game) + 
							user.getAsMention() + "!" + POST_SCRIPT, (Message m)-> pastEvent.message = m);
					LOG.info("Played random top sound in channel: \"" + filePlayed + "\".");
				}
			} catch (Exception e) { LOG.fatal("While playing sound for game start: " + e.toString()); }
		}
	}

	public boolean isMutuallyExclusive() {
		return false;
	}

}
