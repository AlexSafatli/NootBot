package net.dirtydeeds.discordsoundboard.games;

import java.util.Date;

import net.dirtydeeds.discordsoundboard.games.AbstractGameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
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

	private static final String MESSAGE_TITLE = "Whoa! You're all playing.";
	private static final String MESSAGE_REPORT_SUBTITLE = "Annoying?";

	private static final int MIN_NUM_PLAYERS = 3;
	private static final int NUMBER_SEC_BETWEEN = 60;

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
		public boolean isTooSoon(VoiceChannel in) {
			Date now = new Date(System.currentTimeMillis());
			if (channel != null && channel.equals(in)) {
				long secSince = (now.getTime() - time.getTime()) / 1000;
				return (secSince < NUMBER_SEC_BETWEEN);
			}
			return false;
		}
		public void clear() {
			if (message != null) {
				message.delete().queue();
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
		VoiceChannel userChannel, botChannel = bot.getConnectedChannel(guild);
		try { userChannel = bot.getUsersVoiceChannel(user); }
		catch (Exception e) { return false; }
		if (guild == null || userChannel == null || botChannel == null
		    || !userChannel.equals(botChannel)) return false;
		Game game = guild.getMemberById(user.getId()).getGame();
		return (game != null && !game.getType().equals(GameType.STREAMING) &&
		        userChannel.getMembers().size() >= MIN_NUM_PLAYERS);
	}

	protected void handleEvent(UserGameUpdateEvent event, User user) {
		int numPlayers = 0;
		Game currentGame = event.getGuild().getMemberById(user.getId()).getGame();
		String game = currentGame.getName();
		VoiceChannel channel;
		try {
			channel = bot.getUsersVoiceChannel(user);
		} catch (Exception e) {
			error(event, e);
			return;
		}

		// See if there are multiple people that are playing the game in channel.
		// If so: play a sound randomly.
		User[] users = new User[channel.getMembers().size()];
		for (Member m : channel.getMembers()) {
			if (m.getGame() != null && m.getGame().getName().equals(game) &&
			    m.getUser() != null) {
				LOG.info(m.getUser().getName() + " in this channel is playing " + game);
				users[numPlayers++] = m.getUser();
			}
		}

		if (numPlayers >= MIN_NUM_PLAYERS) {
			LOG.info("Found " + user.getName() + " + " +
			         (numPlayers - 1) + " others playing " + game + " in " +
			         channel.getName() + " of guild " + event.getGuild().getName() +
			         ".");
			if (pastEvent != null) {
				if (pastEvent.isTooSoon(channel)) {
					LOG.info("Not enough time since last event in this channel!");
					return;
				}
				pastEvent.clear();
			}

			String filePlayed = (bot.isASoundCategory(game)) ?
			                    bot.getRandomSoundNameForCategory(game) :
			                    bot.getRandomSoundName();
			if (filePlayed != null) {
				TextChannel publicChannel = bot.getBotChannel(channel.getGuild());
				SoundFile f = bot.getSoundMap().get(filePlayed);
				long numPlays = (f != null) ? f.getNumberOfPlays() : 0;
				try {
					bot.playFileForUser(filePlayed, user);
					pastEvent = new GameStartEvent(channel,
					                               new Date(System.currentTimeMillis()),
					                               null);
					LOG.info("Played random sound: \"" + filePlayed + "\".");
					embed(publicChannel,
					      announcement(filePlayed, game, users, numPlayers, numPlays),
					      (Message m)-> { pastEvent.message = m; });
				} catch (Exception e) {
					error(event, e);
				}
			}
		}
	}

	public StyledEmbedMessage announcement(String soundPlayed, String game,
	                                       User[] users, int numPlaying,
	                                       long numPlays) {
		StyledEmbedMessage m = new StyledEmbedMessage(MESSAGE_TITLE, bot);
		String mentions = "";
		for (int i = 0; i < numPlaying; ++i) {
			if (users[i] != null) {
				mentions += users[i].getAsMention() + " ";
			}
		}
		m.addDescription(formatString(Strings.GAME_START_MESSAGE, soundPlayed,
		                              numPlays, game, mentions));
		m.addContent(MESSAGE_REPORT_SUBTITLE,
		             lookupString(Strings.SOUND_REPORT_INFO), false);
		if (thumbnail != null) m.setThumbnail(thumbnail);
		return m;
	}

	public boolean isMutuallyExclusive() {
		return false;
	}

}
