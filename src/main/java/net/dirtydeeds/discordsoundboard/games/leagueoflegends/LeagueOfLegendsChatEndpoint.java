package net.dirtydeeds.discordsoundboard.games.leagueoflegends;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.dirtydeeds.discordsoundboard.games.GameChatEventAdapter;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.utils.SimpleLog;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.FriendRequestPolicy;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.LolStatus.GameStatus;
import com.github.theholywaffle.lolchatapi.listeners.ChatListener;
import com.github.theholywaffle.lolchatapi.listeners.ConnectionListener;
import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.riotapi.RateLimit;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

public class LeagueOfLegendsChatEndpoint {

	public static final SimpleLog LOG = SimpleLog.getLog("LoL");
	private final String LOL = "League of Legends";
	
	private String summonerName;
	private SoundboardDispatcher dispatcher;
	private LolChat api;
	private Properties appProperties;
	private List<Friend> inGame;
	
	public LeagueOfLegendsChatEndpoint(SoundboardDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		appProperties = dispatcher.getAppProperties();
		inGame = new LinkedList<>();
		summonerName  = appProperties.getProperty("lol_summoner_name");
		String apiKey = appProperties.getProperty("lol_riot_api_key");
		String user   = appProperties.getProperty("lol_username");
		String pw     = appProperties.getProperty("lol_password");
		try {
			api = new LolChat(ChatServer.NA, FriendRequestPolicy.ACCEPT_ALL,
					new RiotApiKey(apiKey, RateLimit.DEFAULT));
		} catch (Exception e) {
			LOG.fatal("Building LoL endpoint and: " + e.toString()); return;
		}
		loadListeners();
		if (!api.login(user, pw))
			LOG.fatal("Could not initialize League of Legends chat endpoint.");
		else LOG.info("Logged into League of Legends NA server with username \"" + 
				user + "\" and summoner name: \"" + summonerName + "\".");
	}
	
	public String getName() {
		return this.summonerName;
	}
	
	public List<String> getFriendNames() {
		List<String> friends = new LinkedList<>();
		for (Friend friend : api.getFriends()) friends.add(friend.getName());
		return friends;
	}
	
	public Friend getFriendByName(String name) {
		Friend found = null;
		for (Friend friend : api.getFriends()) {
			try {
				if (found != null)
					if (friend.getName().equalsIgnoreCase(name))
						found = friend;
				else if (friend.getName().contains(name) || (name.length() >= 5 && name.contains(friend.getName())))
					found = friend;
			} catch (Exception e) {
				LOG.fatal("Received an improper response for League of Legends API while verifying friends.");
			}
		}
		return found;
	}
	
	public boolean isAFriend(String name) {
		for (Friend friend : api.getFriends()) {
			try {
				if (friend.getName().contains(name) || (name.length() >= 5 && name.contains(friend.getName())))
					return true;
			} catch (Exception e) {
				LOG.fatal("Received an improper response for League of Legends API while verifying friends.");
			}
		}
		return false;
	}
	
	public boolean isIngame(Friend f) {
		return (inGame.contains(f));
	}
	
	public void setIngame(Friend f) {
		inGame.add(f);
	}
	
	private void loadListeners() {
		api.addConnectionListener(new ConnectionListener() {
			public void connectionClosed() {
				LOG.debug("Connection closed!");
			}
			public void connectionClosedOnError(Exception e) {
				LOG.debug("Connection closed!");
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
			public void onFriendAvailable(Friend friend) {
				LOG.info("Friend " + friend.getName() + " now available.");
			}
			public void onFriendAway(Friend friend) {
				LOG.info("Friend " + friend.getName() + " now away.");
			}
			public void onFriendBusy(Friend friend) {
				LOG.info("Friend " + friend.getName() + " now busy.");
			}
			public void onFriendJoin(Friend friend) {
				LOG.info("Friend " + friend.getName() + " joined.");
			}
			public void onFriendLeave(Friend friend) {
				LOG.info("Friend " + friend.getName() + " left.");
			}
			public void onFriendStatusChange(Friend friend) { 
				GameStatus status = friend.getStatus().getGameStatus();
				LOG.info("Friend " + friend.getName() + " changed status to " + status);
				if (!status.equals(GameStatus.IN_GAME)) {
					if (inGame.contains(friend)) {
						inGame.remove(friend);
						LOG.info("Friend " + friend.getName() + " no longer in-game.");
					}
				} else {
					if (!inGame.contains(friend)) {
						inGame.add(friend);
						LOG.info("Friend " + friend.getName() + " is now in-game.");
					}
				}
			}
			public void onRemoveFriend(String userId, String name) {
				LOG.info("Removed from friends list: " + name);
			}
		});
		api.addChatListener(new ChatListener() {
			public void onMessage(Friend friend, String message) {
				if (message.startsWith(".list")) {
					friend.sendMessage("The list would be too long to send to you on " + LOL + ".");
					return;
				}
				boolean userFound = false, sentMessage = false;
				for (SoundboardBot bot : dispatcher.getBots()) {
					GameChatEventAdapter adapter = new GameChatEventAdapter(bot);
					List<VoiceChannel> channelsWithLeaguePlayers = new LinkedList<>();
					for (Guild guild : bot.getGuilds()) {
						VoiceChannel channel = bot.getConnectedChannel(guild);
						if (channel == null) continue;
						for (User user : channel.getUsers()) {
							if (user.getCurrentGame() != null && user.getCurrentGame().equals(LOL) && 
									!channelsWithLeaguePlayers.contains(channel))
								channelsWithLeaguePlayers.add(channel);
							String name = user.getUsername();
							if (friend.getName().contains(name) || (name.length() >= 5 && name.contains(friend.getName()))) {
								userFound = true;
								LeagueOfLegendsChatChannel c = new LeagueOfLegendsChatChannel(friend, user);
								adapter.process(c, message, c.getContext());
								if (message.startsWith("?")) friend.sendMessage("Desu~");
								return;
							}
						}
					}
					if (message.startsWith("?") && !userFound && channelsWithLeaguePlayers.size() > 0) { // Fall back to checking current games.
						boolean played = false;
						LOG.info("With bot " + bot.getName() + " could not find a matching user "
								+ "so playing for people playing League of Legends.");
						for (VoiceChannel channel : channelsWithLeaguePlayers) {
							played = playFile(bot, channel.getGuild(), friend, message.substring(1).toLowerCase());
							if (!played) return;
						}
						if (!sentMessage) { sentMessage = true; friend.sendMessage("Desu~"); }
					}
				}
			}
			private boolean playFile(SoundboardBot bot, Guild guild, Friend friend, String file) {
				if (bot.getAvailableSoundFiles().get(file) == null) {
					friend.sendMessage("Uh - I couldn't find that sound.");
					return false;
				}
				LOG.info("Playing file " + file + " in " + guild.getName());
				bot.playFile(file, guild);
				return true;
			}
		});
	}
	
	
}
