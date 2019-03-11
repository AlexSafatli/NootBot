package net.dirtydeeds.discordsoundboard.games;

import java.util.Date;

import java.awt.Color;
import java.util.List;

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

  private static final String MESSAGE_TITLE = "You're all playing **%s**!";

  private static final int MIN_NUM_PLAYERS = 3;
  private static final int NUMBER_SEC_BETWEEN = 10;
  private static final int MAX_DURATION = 4;
  private static final int MAX_NUM_MENTIONS = 6;

  private GameStartEvent pastEvent;
  private String thumbnail;

  private class GameStartEvent {
    public VoiceChannel channel;
    public Date time;
    public Message message;

    GameStartEvent(VoiceChannel channel, Date time, Message msg) {
      this.channel = channel;
      this.time = time;
      this.message = msg;
    }

    boolean isTooSoon(VoiceChannel in) {
      if (channel != null && channel.equals(in)) {
        Date now = new Date(System.currentTimeMillis());
        long secSince = (now.getTime() - time.getTime()) / 1000;
        return (secSince < NUMBER_SEC_BETWEEN);
      }
      return false;
    }

    public void clear() {
      if (message != null) message.delete().queue();
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
    try {
      userChannel = bot.getUsersVoiceChannel(user);
    } catch (Exception e) {
      return false;
    }
    if (guild == null || userChannel == null || !userChannel.equals(botChannel))
      return false;
    Game previous = event.getPreviousGame();
    Game game = guild.getMemberById(user.getId()).getGame();
    return (game != null &&
            !(previous.getName().equals(SPOTIFY) && pastEvent.isTooSoon(userChannel)) &&
            !game.getType().equals(GameType.STREAMING) &&
            userChannel.getMembers().size() >= MIN_NUM_PLAYERS);
  }

  protected void handleEvent(UserGameUpdateEvent event, User user) {
    int numPlayers = 0;
    String game = event.getGuild().getMemberById(user.getId()).getGame().getName();
    VoiceChannel channel;
    try {
      channel = bot.getUsersVoiceChannel(user);
    } catch (Exception e) {
      error(event, e);
      return;
    }

    // See if multiple people are playing the game in channel.
    // If so: play a sound randomly.
    List<Member> members = channel.getMembers();
    User[] users = new User[members.size()];
    for (Member m : members) {
      Game g = m.getGame();
      if (g != null && g.getName().equals(game) && m.getUser() != null) {
        LOG.info(m.getUser().getName() + " in channel is playing " + game);
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
          LOG.info("Not enough time since last event in channel!");
          return;
        }
        pastEvent.clear();
      }

      String sound;
      if (bot.isASoundCategory(game)) {
        LOG.info(game + " is as a category. Can play from that category.");
        sound = (String)RandomUtils.chooseOne(
                bot.getRandomSoundNameForCategory(game, MAX_DURATION),
                bot.getRandomSoundName(MAX_DURATION));
      } else {
        sound = bot.getRandomSoundName(MAX_DURATION);
      }

      if (sound != null && !sound.isEmpty()) {
        TextChannel lobby = bot.getBotChannel(channel.getGuild());
        SoundFile f = bot.getSoundMap().get(sound);
        try {
          bot.playFileForUser(sound, user);
          pastEvent = new GameStartEvent(channel,
                  new Date(System.currentTimeMillis()),
                  null);
          LOG.info("Played random sound: \"" + sound + "\".");
          embed(lobby, announcement(event, sound, game, users, numPlayers,
                  (f != null) ? f.getNumberOfPlays() : 0),
                  (Message m) -> pastEvent.message = m);
        } catch (Exception e) {
          error(event, e);
        }
      }

    }
  }

  private StyledEmbedMessage announcement(UserGameUpdateEvent event,
                                         String soundPlayed, String game,
                                         User[] users, int numPlaying,
                                         long numPlays) {
    SoundFile played = bot.getSoundMap().get(soundPlayed);
    String mentions = "";
    for (int i = 0; i < numPlaying || i > MAX_NUM_MENTIONS; ++i) {
      if (users[i] != null) {
        mentions += users[i].getAsMention();
      }
      if (i == MAX_NUM_MENTIONS) {
        mentions += " et al.";
      } else if (i < numPlaying - 1) {
        mentions += " ";
      }
    }
    StyledEmbedMessage m = StyledEmbedMessage.forUser(bot,
            users[0], String.format(MESSAGE_TITLE, game),
            formatString(Strings.GAME_START_MESSAGE, soundPlayed,
            numPlays, game, mentions));
    m.addContent("Server", event.getGuild().getName(), true);
    m.addContent("Category",
            (bot.isASoundCategory(game) &&
                    played.getCategory().equals(game)) ?
                    game : "\u2014", true);
    Color color = StringUtils.toColor(game);
    m.setColor(color);
    m.addFooterText(String.format("(%d, %d, %d)", color.getRed(),
                                  color.getGreen(), color.getBlue()));
    if (thumbnail != null) m.setThumbnail(thumbnail);
    return m;
  }

  public boolean isMutuallyExclusive() {
    return false;
  }

}
