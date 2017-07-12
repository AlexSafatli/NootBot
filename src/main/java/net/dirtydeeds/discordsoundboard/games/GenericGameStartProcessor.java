package net.dirtydeeds.discordsoundboard.games;

import java.util.Date;

import net.dirtydeeds.discordsoundboard.games.AbstractGameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class GenericGameStartProcessor extends AbstractGameUpdateProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("GameStartProcessor");
	
	private static final int MIN_NUM_PLAYERS = 3;
	private static final int NUMBER_SEC_BETWEEN = 60;
	private static final int MAX_DURATION = 2;
	
	private GameStartEvent pastEvent;
	private String thumbnail;
	
	private class GameStartEvent {
		public VoiceChannel channel;
		public Date time;
		public Message message;
		public GameStartEvent(VoiceChannel channel, Date time, Message msg) {
			this.channel = channel;
			this.time = time;
			this.message = msg;
		}
		public void clear() {
			if (message != null) {
				message.deleteMessage().queue();
			}
		}
	}
	
	public GenericGameStartProcessor(SoundboardBot bot) {
		super(bot);
	}

	public GenericGameStartProcessor(SoundboardBot bot, String url) {
		this(bot);
		thumbnail = url;
	}
	
	public boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user) {
		Guild guild = event.getGuild();
		VoiceChannel userChannel = null, botChannel = bot.getConnectedChannel(guild);
		try { userChannel = bot.getUsersVoiceChannel(user); } catch (Exception e) { return false; }
		if (guild == null || userChannel == null || botChannel == null || !userChannel.equals(botChannel)) return false;
		Game game = guild.getMemberById(user.getId()).getGame();
		return (game != null && userChannel.getMembers().size() >= MIN_NUM_PLAYERS);
	}
	
	protected void handleEvent(UserGameUpdateEvent event, User user) {
		int numPlayers = 0;
		Game currentGame = event.getGuild().getMemberById(user.getId()).getGame();
		String game = currentGame.getName();
		VoiceChannel channel = null;
		try { channel = bot.getUsersVoiceChannel(user); } catch (Exception e) { return; }
		// See if there have been multiple people that started playing the game in a channel.
		// If so: play a sound randomly from top played sounds.
		User[] users = new User[channel.getMembers().size()];
		for (Member m : channel.getMembers()) {
			if (m.getGame() != null && m.getGame().getName().equals(game)) {
				users[numPlayers++] = m.getUser();
			}
		}
		if (numPlayers >= MIN_NUM_PLAYERS) {
			LOG.info("Found " + user.getName() + " + " + numPlayers + " others playing " + game + " in " + channel.getName() + " of guild " + event.getGuild().getName() + ".");
			Date now = new Date(System.currentTimeMillis());
			if (pastEvent != null && pastEvent.channel != null && pastEvent.channel.equals(channel)) {
		    	long secSince = (now.getTime() - pastEvent.time.getTime())/1000;
		    	if (secSince < NUMBER_SEC_BETWEEN) { LOG.info("Not enough time since last event in this channel."); return; }
			}
			TextChannel publicChannel = channel.getGuild().getPublicChannel();
			if (pastEvent != null) pastEvent.clear();
			String filePlayed = bot.getRandomTopPlayedSoundName(MAX_DURATION);
			if (filePlayed != null) {
				SoundFile f = bot.getSoundMap().get(filePlayed);
				long numPlays = (f != null) ? f.getNumberOfPlays() : 0;
				Message msg = announcement(
					filePlayed, game, users, numPlayers, numPlays).getMessage();
				try {
					bot.playFileForUser(filePlayed, user);
					pastEvent = new GameStartEvent(channel, now, null);
					publicChannel.sendMessage(msg).queue((Message m)-> pastEvent.message = m);
					LOG.info("Played random top sound in channel: \"" + filePlayed + "\".");
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	public StyledEmbedMessage announcement(String soundPlayed, String game, User[] users, int numPlaying, long numPlays) {
		StyledEmbedMessage m = new StyledEmbedMessage("Whoa! You're all playing a game.", bot);
		String mentions = "";
		for (int i = 0; i < numPlaying; ++i) {
			if (users[i] != null) {
				mentions += users[i].getAsMention() + " ";
			}
		}
		m.addDescription(formatString(Strings.GAME_START_MESSAGE, soundPlayed, numPlays, game, mentions));
		m.addContent("Annoying?", lookupString(Strings.SOUND_REPORT_INFO), false);
		if (thumbnail != null) m.setThumbnail(thumbnail);
		return m;
	}

	
	public boolean isMutuallyExclusive() {
		return false;
	}

}
