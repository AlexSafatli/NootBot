package net.dirtydeeds.discordsoundboard.games.leagueoflegends;

import java.util.Date;

import com.github.theholywaffle.lolchatapi.LolStatus.GameStatus;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

import net.dirtydeeds.discordsoundboard.games.AbstractGameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class LeagueOfLegendsGameStartProcessor extends
		AbstractGameUpdateProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("LoLGameStartProcessor");
	private final String LOL = "League of Legends";
	
	private static final int MIN_NUM_PLAYERS = 3;
	private static final int NUMBER_MIN_BETWEEN = 2;
	
	private LeagueOfLegendsGameStartEvent pastEvent;
	private Message pastMessage;
	
	private SoundboardDispatcher dispatcher;
	private LeagueOfLegendsChatEndpoint endpoint;
	
	private class LeagueOfLegendsGameStartEvent {
		public VoiceChannel channel;
		public Date time;
		public LeagueOfLegendsGameStartEvent(VoiceChannel channel, Date time) {
			this.channel = channel;
			this.time = time;
		}
	}
	
	public LeagueOfLegendsGameStartProcessor(SoundboardBot bot, SoundboardDispatcher dispatcher) {
		super(bot);
		this.dispatcher = dispatcher;
	}
	
	public boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user) {
		if (endpoint == null) endpoint = dispatcher.getLeagueOfLegendsEndpoint();
		return (user.getCurrentGame() != null && user.getCurrentGame().equals(LOL) && 
				endpoint != null);
	}
	
	protected void handleEvent(UserGameUpdateEvent event, User user) {
		if (endpoint == null)
			endpoint = dispatcher.getLeagueOfLegendsEndpoint();
		int numLOLPlayers = 0;
		String category = (bot.isASoundCategory(LOL)) ? LOL : null;
		if (category == null && bot.isASoundCategory("Games")) category = "Games";
		VoiceChannel channel = null;
		Friend userAsFriend = endpoint.getFriendByName(user.getUsername());
		try {
			channel = bot.getUsersVoiceChannel(user);
		} catch (Exception e) {
			LOG.fatal("Problem retrieving voice channel for user.");
			return;
		}
		if (channel == null) {
			LOG.info("User " + user.getUsername() + " not in a voice channel! Not continuing.");
			return;
		}
		// See if there have been multiple people that started playing the game in a channel.
		// If so: play a sound randomly from a category either matching this game name or is
		// generalized for games. If neither such category exists, play a random sound.
		if (channel.getUsers().size() >= MIN_NUM_PLAYERS) {
			for (User u : channel.getUsers()) {
				String name = u.getUsername();
				Friend friend = endpoint.getFriendByName(name);
				boolean ingame = false;
				if (friend != null)  {
					LOG.info("Found user " + name + " and identified as LoL friend " + friend.getName());
					ingame = endpoint.isIngame(friend);
				}
				if (u.getCurrentGame() != null && u.getCurrentGame().equals(LOL)) {
					if (friend != null && ingame) continue;
					else if (friend != null && !ingame) {
						LOG.info("Friend " + friend.getName() + " registered as being in-game.");
						endpoint.setIngame(friend);
					} ++numLOLPlayers;
				}
			}
			if (numLOLPlayers >= MIN_NUM_PLAYERS) {
				LOG.info("Found " + user.getUsername() + " and " + numLOLPlayers + 
						" other users playing " + LOL + " in channel " + channel);
				Date now = new Date(System.currentTimeMillis());
				if (pastEvent != null && pastEvent.channel != null && pastEvent.channel.equals(channel)) {
			    	long minutesSince = (now.getTime() - pastEvent.time.getTime())/(1000*60);
			    	if (minutesSince < NUMBER_MIN_BETWEEN) {
			    		LOG.info("Not enough time since last start event. Stopping.");
			    		return;
			    	}
				}
				if (userAsFriend != null && !userAsFriend.getStatus().getGameStatus().equals(GameStatus.SPECTATING)) {
					LOG.info("Chat bot shows mapped user is spectating on League of Legends. Stopping.");
					return;
				}
				pastEvent = new LeagueOfLegendsGameStartEvent(channel, now);
				try {
					String filePlayed = null;
					if (category != null)
						filePlayed = bot.playRandomFileForCategory(user, category);
					else filePlayed = bot.playRandomFile(user);
					if (pastMessage != null && bot.hasPermissionInChannel((TextChannel)pastMessage.getChannel(),
							Permission.MESSAGE_MANAGE)) pastMessage.deleteMessage();
					channel.getGuild().getPublicChannel().sendMessageAsync(
							String.format("Played random sound `%s` because you started a game of **%s** with multiple people ",filePlayed,LOL) + 
							user.getAsMention() + " et al.!",
							(Message m) -> pastMessage = m);
					LOG.info("Played random sound in channel: \"" + filePlayed + "\".");
				} catch (Exception e) {
					LOG.fatal("While playing sound for game start: " + e.toString());
				}
				return;
			}
		}
	}

}
