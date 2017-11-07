package net.dirtydeeds.discordsoundboard.listeners;

import java.util.*;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

public class MoveListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Move");

  private static final List<String> WELCOMES = Arrays.asList(new String[] {
        "Welcome, %s!",
        "%s has joined. Brace yourselves.",
        "%s has joined. Ermagherd.",
        "Leave your weapons by the door, %s.",
        "%s just appeared. Seems OP - nerf.",
        "You're killing it, %s.",
        "Oh hey! It's %s.",
        "We salute you, %s.",
        "ようこそ %s.",
        "Turn it up to eleven. %s is here!"
      });

  private static final List<String> WELCOME_BACKS = Arrays.asList(new String[] {
        "😪", "😴", "😡", "🖕", "Uh, hi.", "Welcome... back?", "glhf"
      });

  private static final List<String> WHATS = Arrays.asList(new String[] {
        "What?", "Nani?", "Huh?", "( ͡° ͜ʖ ͡°)"
      });

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

  private void onJoin(VoiceChannel voiceChannel, User user) {
    Guild guild = voiceChannel.getGuild();
    VoiceChannel afk = guild.getAfkChannel();

    if (bot.isUser(user)) {
      if (voiceChannel.getMembers().size() == 1) {
        LOG.info("Moved to an empty channel.");
        leaveVoiceInGuild(guild);
      }
      return;
    } else if (user.isBot()) {
      return;
    }

    LOG.info(user.getName() + " joined " + voiceChannel.getName() +
             " in " + guild.getName() + ".");

    if (!bot.isAllowedToPlaySound(user) ||
        (afk != null && afk.getId().equals(voiceChannel.getId()))) {
      return;
    } else if (bot.isMuted(guild)) {
      LOG.info("Bot is currently muted. Doing nothing.");
      return;
    } else if (voiceChannel.getUserLimit() ==
               voiceChannel.getMembers().size()) {
      LOG.info("Channel is full.");
      return;
    }

    String fileToPlay = bot.getEntranceForUser(user),
           soundInfo = "";

    if (fileToPlay == null || fileToPlay.isEmpty()) return;

    if (bot.getSoundMap().get(fileToPlay) == null) {
      bot.sendMessageToUser("**Uh oh!** Your entrance `" + fileToPlay +
                            "` doesn't exist anymore. *Update it!*", user);
      LOG.info(user.getName() + " has stale entrance. Alerted and clearing.");
      bot.setEntranceForUser(user, null, null);
      return;
    }

    // Clear previous message(s).
    boolean recentEntrance = false;
    if (pastEntrances.get(guild) == null) {
      pastEntrances.put(guild, new LinkedList<EntranceEvent>());
    } else {
      Queue<EntranceEvent> entrances = pastEntrances.get(guild);
      while (!entrances.isEmpty()) {
        EntranceEvent entrance = entrances.poll();
        entrance.message.delete().queue();
        if (entrance.user.equals(user) && !recentEntrance) {
          LOG.info("User has heard entrance recently.");
          recentEntrance = true;
        }
      }
    }

    // Play a sound.
    if (!recentEntrance) {
      try {
        if (bot.playFileForEntrance(fileToPlay, user, voiceChannel)) {
          SoundFile s = bot.getDispatcher().getSoundFileByName(fileToPlay);
          soundInfo = "Played " +
                      formatString(Strings.SOUND_DESC, fileToPlay,
                                   s.getCategory(),
                                   s.getNumberOfPlays());
        }
      } catch (Exception e) { e.printStackTrace(); }
    } else if (bot.getConnectedChannel(guild) == null) {
      bot.moveToChannel(voiceChannel); // Move to channel otherwise.
    }

    // Send a message greeting them into the server.
    VoiceChannel joined = bot.getConnectedChannel(guild);
    if (joined != null && joined.equals(voiceChannel)) {
      if (bot.getBotChannel(guild) != null) {
        embed(bot.getBotChannel(guild),
              welcomeMessage(user, voiceChannel, soundInfo, !recentEntrance),
              (Message m)-> pastEntrances.get(guild).add(
                new EntranceEvent(m, user)));
      }
    }
  }

  public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    onLeave(event.getChannelLeft(), event.getMember().getUser());
  }

  private void onLeave(VoiceChannel channel, User  user) {
    Guild guild = channel.getGuild();
    VoiceChannel botsChannel = bot.getConnectedChannel(guild);

    // Ignore if it is just the bot or not even connected.
    if (botsChannel == null || bot.isUser(user)) return;

    LOG.info(user.getName() + " left " + channel.getName() + " in " +
             guild.getName() + ".");

    if (VoiceUtils.numUsersInVoiceChannels(guild) == 0) {
      LOG.info("No more users in " + guild.getName());
      leaveVoiceInGuild(guild);
    } else if (botsChannel.getMembers().size() == 1) {
      for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
        int numMembers = voiceChannel.getMembers().size();
        if (botsChannel.equals(voiceChannel)) continue;
        else if (numMembers > 0
                 && (guild.getAfkChannel() == null
                     || !voiceChannel.getId().equals(
                       guild.getAfkChannel().getId()))) {
          if (numMembers == 1
              && voiceChannel.getMembers().get(0).getUser().isBot()) {
            continue;
          }
          if (bot.moveToChannel(voiceChannel)) {
            LOG.info("Moving to voice channel " + voiceChannel.getName() +
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

  public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
    if (bot.isUser(event.getMember().getUser()) &&
        bot.isMuted(event.getGuild())) {
      LOG.info("Was server muted.");
      leaveVoiceInGuild(event.getGuild());
    }
  }

  private void leaveVoiceInGuild(Guild guild) {
    if (guild == null) return;
    if (pastEntrances.get(guild) == null) {
      pastEntrances.put(guild, new LinkedList<EntranceEvent>());
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
                   user.getName() + " has entered the channel.";
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
    m.setColor(StringUtils.toColor(user.getName()));
    return m;
  }

}
