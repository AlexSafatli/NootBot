package net.dirtydeeds.discordsoundboard.games.leagueoflegends;

import java.util.Properties;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.utils.SimpleLog;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.LolStatus.Division;
import com.github.theholywaffle.lolchatapi.LolStatus.GameStatus;
import com.github.theholywaffle.lolchatapi.LolStatus.Tier;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RateLimit;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

public class LeagueOfLegendsChatEndpoint {

	public static final SimpleLog LOG = SimpleLog.getLog("LoL");
	
	private SoundboardDispatcher dispatcher;
	private LolChat api;
	private Properties appProperties;
	
	public LeagueOfLegendsChatEndpoint(SoundboardDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		appProperties = dispatcher.getAppProperties();
		String apiKey = appProperties.getProperty("lol_riot_api_key");
		String user   = appProperties.getProperty("lol_username");
		String pw     = appProperties.getProperty("lol_password");
		api = new LolChat(ChatServer.NA, FriendRequestPolicy.ACCEPT_ALL,
				new RiotApiKey(apiKey, RateLimit.DEFAULT));
		loadListeners();
		if (!api.login(user, pw)) LOG.fatal("Could not initialize League of Legends API endpoint.");
		else LOG.info("Logged into League of Legends NA server with username " + user);
	}
	
	private void loadListeners() {
		api.addConnectionListener(new ConnectionListener() {
			public void connectionClosed() {
				LOG.debug("Connection closed!");
			}
			public void connectionClosedOnError(Exception e) {
				LOG.debug("Connection closed!");
				LOG.fatal(e.toString());
			}
			public void reconnectingIn(int seconds) {
				LOG.debug("Reconnecting in " + seconds + " seconds.");
			}
			public void reconnectionFailed(Exception e) {
				LOG.fatal("Reconnection failed: " + e.toString());
			}
			public void reconnectionSuccessful() {
				LOG.debug("Reconnected successfully.");
			}
		});
		api.addFriendListener(new FriendListener() {
			public void onNewFriend(Friend friend) {
				LOG.info("Accepted a friend request from " + friend.getName());
				friend.sendMessage("Hello.");
			}
			public void onFriendAvailable(Friend friend) { }
			public void onFriendAway(Friend friend) { }
			public void onFriendBusy(Friend friend) { }
			public void onFriendJoin(Friend friend) { }
			public void onFriendLeave(Friend friend) { }
			public void onFriendStatusChange(Friend friend) { }
			public void onRemoveFriend(String userId, String name) {
				LOG.info("Removed from friends list: " + name);
			}
		});
		api.addChatListener(new ChatListener() {
			public void onMessage(Friend friend, String message) {
				if (message.startsWith("?")) {
					boolean playedFile = false;
					String file = message.substring(1);
					outerloop:
					for (SoundboardBot bot : dispatcher.getBots()) {
						for (Guild guild : bot.getGuilds()) {
							VoiceChannel channel = bot.getConnectedChannel(guild);
							if (channel == null) continue;
							for (net.dv8tion.jda.entities.User user : channel.getUsers()) {
								if ((user.getCurrentGame() != null && user.getCurrentGame().equals("League of Legends")) || friend.getName().contains(user.getUsername())) {
									if (bot.getAvailableSoundFiles().get(file) == null) {
										friend.sendMessage(bot.getName() + " does not know about this file.");
										continue outerloop;
									}
									bot.playFile(file, guild);
									playedFile = true;
									LOG.info("Playing file " + file + " in " + guild.getName());
									break;
								}
							}
						}
					}
					if (playedFile) friend.sendMessage("Played!");
					LOG.info("Play sound command from " + friend.getName() + " processed on League of Legends with file " + file);
				} else if (message.startsWith(".")) {
					if (message.equals(".invite")) {
						LOG.info("Invite command received from " + friend.getName());
						for (SoundboardBot bot : dispatcher.getBots()) {
							if (friend.getStatus().getGameStatus().equals(GameStatus.HOSTING_NORMAL_GAME))
								bot.sendMessageToAllGuilds(friend.getName() + " is hosting a normal game on League of Legends and is looking for people.");
							else if (friend.getStatus().getGameStatus().equals(GameStatus.HOSTING_RANKED_GAME))
								bot.sendMessageToAllGuilds(friend.getName() + " is hosting a ranked game (" + friend.getStatus().getRankedLeagueTier() + ") and is looking for people.");
						}
						if (!friend.getStatus().equals(GameStatus.HOSTING_NORMAL_GAME) && !friend.getStatus().equals(GameStatus.HOSTING_RANKED_GAME))
							friend.sendMessage("You're not hosting a (normal or ranked) game yet.");
					}
				}
			}
		});
	}
	
	
}
