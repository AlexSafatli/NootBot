package net.dirtydeeds.discordsoundboard.games;

import java.util.Date;
import java.util.List;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.GameMode;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.common.Side;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.currentgame.Participant;
import com.robrua.orianna.type.core.staticdata.Mastery;
import com.robrua.orianna.type.core.summoner.Summoner;

import net.dirtydeeds.discordsoundboard.async.DeleteMessageJob;
import net.dirtydeeds.discordsoundboard.games.AbstractGameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class LeagueOfLegendsGameStartProcessor extends AbstractGameUpdateProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("LoLGameStartProcessor");
	
	private static final int MIN_NUM_PLAYERS = 3;
	private static final int NUMBER_SEC_BETWEEN = 5;
	
	private GameStartEvent pastEvent;
	private boolean enabled = false;
	
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
	
	public LeagueOfLegendsGameStartProcessor(SoundboardBot bot) {
		super(bot);
		String riotApiKey = bot.getDispatcher().getAppProperties().getProperty("riot_api_key");
		if (riotApiKey != null && !riotApiKey.isEmpty()) {
			RiotAPI.setRegion(Region.NA);
			RiotAPI.setAPIKey(riotApiKey);
			enabled = true;
		}
	}
	
	public boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user) {
		if (!enabled) return false;
		Guild guild = event.getGuild();
		VoiceChannel channel = null;
		Member member = (guild != null) ? guild.getMemberById(user.getId()) : null;
		try { channel = bot.getUsersVoiceChannel(user); } catch (Exception e) { LOG.fatal("Problem retrieving voice channel for user."); }
		String game = (member != null && member.getGame() != null) ? member.getGame().getName() : null;
		return (game != null && game.equals("League of Legends") && channel != null && channel.getMembers().size() >= MIN_NUM_PLAYERS);
	}
	
	protected void handleEvent(UserGameUpdateEvent event, User user) {
		int numPlayers = 0;
		Guild guild = event.getGuild();
		String game = "League of Legends";
		VoiceChannel channel = null;
		try { channel = bot.getUsersVoiceChannel(user); } catch (Exception e) { LOG.fatal("Problem retrieving voice channel for user."); return; }
		// See if there have been multiple people that started playing the game in a channel.
		// If so: acquire game information IF any of the players' names can be identified
		// as a League of Legends account.
		for (Member m : channel.getMembers()) {
			if (m.getGame() != null && m.getGame().getName().equals(game)) ++numPlayers;
		}
		if (numPlayers >= MIN_NUM_PLAYERS) {
			// Ensure enough time has passed.
			Date now = new Date(System.currentTimeMillis());
			if (pastEvent != null && pastEvent.channel != null && pastEvent.channel.equals(channel)) {
		    	long secSince = (now.getTime() - pastEvent.time.getTime())/1000;
		    	if (secSince < NUMBER_SEC_BETWEEN) {
		    		LOG.info("Not enough time since last event in this channel."); return;
		    	}
			}
			LOG.info("Checking for summoner information for " + user.getName());
			// Check API.
			Summoner summoner = RiotAPI.getSummonerByName(user.getName());
			if (summoner == null || summoner.getName() == null || summoner.getName().isEmpty()) {
				net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
				for (String handle : u.getAlternateHandles()) {
					if (summoner == null) summoner = RiotAPI.getSummonerByName(handle);
				}
				if (summoner == null) {
					LOG.info("No League of Legends summoner found with name " + user.getName());
					askUserAboutAlternateHandles(user);
					return;
				}
			}
			CurrentGame currentGame = summoner.getCurrentGame();
			if (currentGame != null) {
				LOG.info("Found a current game for summoner " + summoner.getName() + " associated with user " + user.getName());
				TextChannel publicChannel = guild.getPublicChannel();
				if (pastEvent != null && pastEvent.message != null) pastEvent.message.deleteMessage();
				pastEvent = new GameStartEvent(channel, now, null);
				embed(publicChannel, getGameInformation(currentGame, summoner.getName()),
						(Message m)-> bot.getDispatcher().getAsyncService().runJob(new DeleteMessageJob(m, 2400)));
			}
		}
	}
	
	private StyledEmbedMessage getGameInformation(CurrentGame g, String summoner) {
		StyledEmbedMessage msg = new StyledEmbedMessage("A League of Legends game with multiple people has started.");
		List<Participant> players = g.getParticipants();
		for (int i = 0; i < players.size(); ++i) {
			Participant p = players.get(i);
			Mastery keystone = p.getMasteries().get(p.getMasteries().size()-1).getMastery();
			String side = "blue";
			if (p.getTeam().equals(Side.PURPLE)) side = "purple";
			msg.addContent(p.getSummonerName(), String.format("Is on %s with summoners %s and %s, keystone %s, and on %s side.", p.getChampion().getName(), 
					p.getSummonerSpell1().getName(), p.getSummonerSpell2().getName(), keystone.getName(), side), true);
		}
		String gameMode = "normal";
		if (!g.getMode().equals(GameMode.CLASSIC)) {
			gameMode = g.getMode().toString();
		}
		msg.addDescription(formatString(Strings.LEAGUE_START_MESSAGE, summoner) + " \u2014 " + String.format("It is a %s game.", gameMode));
		return msg;
	}
	
	private void askUserAboutAlternateHandles(User user) {
		net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
		if (u.getAlternateHandles().isEmpty() && u.getEntrance() != null && !u.wasAskedAboutAlternateHandles()) {
			bot.sendMessageToUser("I noticed you entered a game of League of Legends \u2014 you might not be using your Discord name as"
					+ " your summoner name, though. If you want me to track your other summoner names, or alternate user handles, "
					+ "type `.alt` to me for each of your other handles.", user);
			u.setAskedAboutAlternateHandles(true);
			bot.getDispatcher().saveUser(u);
			LOG.info("Asked user " + u.getUsername() + " about alternate handles.");
		}
	}
	
	public boolean isMutuallyExclusive() {
		return false;
	}

}
