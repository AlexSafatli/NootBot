package net.dirtydeeds.discordsoundboard.listeners;

import java.util.*;
import java.awt.Color;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class MoveListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Move");

  private static final List<String> WELCOMES = Arrays.asList("Welcome, %s!",
          "Leave your weapons by the door, %s.",
          "%s used Enter Channel. It's super effective!",
          "You're killing it, %s.",
          "ようこそ %s.",
          "I like %s and I cannot lie. You other brothers can't deny.",
          "Hit me %s one more time.",
          "Oh no not %s, I will survive.",
          "Take a sad %s and make it better...",
          "Love you like a love song %s.",
          "Knees weak, arms are heavy. There's %s on his sweater already.",
          "That's %s in the corner.",
          "%s wants off this planet.",
          "A little bit of %s on the side.",
          "Real G's move in silence like %s.",
          "Climb the ladder to success, %s style.",
          "The best part about being %s is there's so many them.",
          "Your booty don't need no explaining, %s.",
          "To the left, to the left %s.",
          "Do you ever feel like a plastic bag %s?");

  private static final List<String> WELCOME_BACKS = Arrays.asList("😪", "😴", "😡", "🖕", "???", "gg", "Baka!");

  private static final List<String> WHATS = Arrays.asList("What?", "Nani?", "Huh?", "( ͡° ͜ʖ ͡°)", "なんてこったい？", "Que?");

  private Map<Guild, Queue<EntranceEvent>> pastEntrances;

  public MoveListener(SoundboardBot bot) {
    this.bot = bot;
    this.pastEntrances = new HashMap<>();
  }

  private class EntranceEvent {
    public Message message;
    public User user;
    public EntranceEvent(Message m, User u) {
      message = m;
      user = u;
    }
  }

  public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    onJoin(event.getChannelJoined(), event.getMember().getUser());
  }

  private void onJoin(VoiceChannel vc, User user) {
    Guild guild = vc.getGuild();
    VoiceChannel afk = guild.getAfkChannel();

    if (bot.isUser(user)) {
      if (vc.getMembers().size() == 1) {
        LOG.info("Moved to an empty channel.");
        leaveVoiceInGuild(guild);
      }
      return;
    } else if (user.isBot()) {
      return;
    }

    LOG.info(user.getName() + " joined " + vc.getName() +
            " in " + guild.getName() + ".");

    if (!bot.isAllowedToPlaySound(user) ||
            (afk != null &&
                    afk.getId().equals(vc.getId()))) {
      return;
    } else if (bot.isMuted(guild)) {
      LOG.info("Bot is currently muted. Doing nothing.");
      return;
    } else if (vc.getUserLimit() ==
            vc.getMembers().size()) {
      LOG.info("Channel is full.");
      return;
    }

    String fileToPlay = bot.getEntranceForUser(user),
            soundInfo = "";

    if (fileToPlay == null || fileToPlay.isEmpty()) return;

    if (bot.getSoundMap().get(fileToPlay) == null) {
      bot.sendMessageToUser("**Uh oh!** `" + fileToPlay +
              "` used to be your entrance but I can't find that" +
              " sound anymore. *Update your entrance!*", user);
      LOG.info(user.getName() + " has stale entrance. Alerted and clearing.");
      bot.setEntranceForUser(user, null, null);
      return;
    }

    // Clear previous message(s).
    boolean recentEntrance = false;
    if (pastEntrances.get(guild) == null) {
      pastEntrances.put(guild, new LinkedList<>());
    } else {
      Queue<EntranceEvent> entrances = pastEntrances.get(guild);
      while (!entrances.isEmpty()) {
        EntranceEvent entrance = entrances.poll();
        entrance.message.delete().queue();
        if (entrance.user.equals(user) && !recentEntrance) {
          recentEntrance = true;
        }
      }
    }

    // Play a sound.
    if (!recentEntrance) {
      try {
        if (bot.playFileForEntrance(fileToPlay, user, vc)) {
          SoundFile s = bot.getDispatcher().getSoundFileByName(fileToPlay);
          soundInfo = "Played " +
                  formatString(Strings.SOUND_DESC, fileToPlay,
                          s.getCategory(),
                          s.getNumberOfPlays());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (bot.getConnectedChannel(guild) == null) {
      bot.moveToChannel(vc); // Move to channel otherwise.
    }

    // Send a message greeting them into the server.
    VoiceChannel joined = bot.getConnectedChannel(guild);
    if (joined != null && joined.equals(vc)) {
      if (bot.getBotChannel(guild) != null) {
        embed(bot.getBotChannel(guild),
                welcomeMessage(user, vc, soundInfo, !recentEntrance),
                (Message m) ->
                        pastEntrances.get(guild).add(
                                new EntranceEvent(m, user)));
      }
    }
  }

  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    onLeave(event.getChannelLeft(), event.getMember().getUser());
  }

  private void onLeave(VoiceChannel vc, User user) {
    Guild guild = vc.getGuild();
    VoiceChannel botsChannel = bot.getConnectedChannel(guild);

    // Ignore if it is just the bot or not even connected.
    if (botsChannel == null || bot.isUser(user)) return;

    LOG.info(user.getName() + " left " + vc.getName() + " in " +
            guild.getName() + ".");

    if (VoiceUtils.numUsersInVoiceChannels(guild) == 0) {
      LOG.info("No more users in " + guild.getName());
      leaveVoiceInGuild(guild);
    } else if (botsChannel.getMembers().size() == 1) {
      for (VoiceChannel vc_ : guild.getVoiceChannels()) {
        int numMembers = vc_.getMembers().size();
        if (botsChannel.equals(vc_)) ;
        else if (numMembers > 0
                && (guild.getAfkChannel() == null
                || !vc_.getId().equals(
                guild.getAfkChannel().getId()))) {
          if (numMembers == 1 &&
                  vc_.getMembers().get(0).getUser().isBot()) {
            continue;
          }
          if (bot.moveToChannel(vc_)) {
            LOG.info("Moving to voice channel " + vc_.getName() +
                    " in server " + guild.getName());
            return;
          }
        }
      }
      leaveVoiceInGuild(guild);
    }
  }

  public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
    VoiceChannel botsChannel = bot.getConnectedChannel(event.getGuild());
    if (event.getChannelLeft().equals(botsChannel)) {
      onLeave(event.getChannelLeft(), event.getMember().getUser());
    }
    onJoin(event.getChannelJoined(), event.getMember().getUser());
  }

  private void leaveVoiceInGuild(Guild guild) {
    if (guild == null) return;
    if (pastEntrances.get(guild) == null) {
      pastEntrances.put(guild, new LinkedList<>());
    }
    if (guild.getAudioManager() != null) {
      LOG.info("Leaving voice in " + guild.getName());
      Queue<EntranceEvent> entrances = pastEntrances.get(guild);
      while (!entrances.isEmpty()) {
        EntranceEvent entrance = entrances.poll();
        if (entrance.message != null) entrance.message.delete().queue();
      }
      guild.getAudioManager().closeAudioConnection();
    }
  }

  public StyledEmbedMessage welcomeMessage(User user, Channel channel,
                                           String soundInfo, boolean welcomeInTitle) {
    String title = (welcomeInTitle) ?
            String.format(StringUtils.randomString(WELCOMES),
                    user.getName()) :
            user.getName() + " is here, *again*.";
    StyledEmbedMessage m = new StyledEmbedMessage(title, bot);
    m.setThumbnail(user.getEffectiveAvatarUrl());
    if (!soundInfo.isEmpty()) {
      m.addDescription(soundInfo + Strings.SEPARATOR + user.getAsMention());
    } else {
      m.addDescription(StringUtils.randomString(WELCOME_BACKS) +
              Strings.SEPARATOR + user.getAsMention());
    }
    m.addContent(StringUtils.randomString(WHATS),
            "I play sounds. Type `.help` for commands.", false);
    Color color = StringUtils.toColor(user.getName());
    m.setColor(color);
    m.addFooterText(String.format("(%d, %d, %d)", color.getRed(),
            color.getGreen(), color.getBlue()));
    return m;
  }

}
