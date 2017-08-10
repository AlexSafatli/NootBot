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
        "Welcome", "Hello", "Yo", "Hiya", "Heya", "Sup"
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
    VoiceChannel afkChannel = guild.getAfkChannel();

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

    if (!bot.isAllowedToPlaySound(user)) {
      LOG.info("User " + user.getName() + " cannot play sounds. Ignoring.");
      return;
    } else if (afkChannel != null && afkChannel.getId().equals(
                 voiceChannel.getId())) {
      LOG.info("User " + user.getName() + " joined an AFK channel. Ignoring.");
      return;
    } else if (bot.isMuted(guild)) {
      LOG.info("Bot is currently muted. Doing nothing.");
      return;
    }

    String fileToPlay = bot.getEntranceForUser(user);
    if (fileToPlay != null && !fileToPlay.isEmpty()) {
      if (bot.getSoundMap().get(fileToPlay) == null) {
        bot.sendMessageToUser("**Uh oh!** Your entrance `" + fileToPlay +
                              "` doesn't exist anymore. *Update it!*", user);
        LOG.info(user.getName() + " has stale entrance. Alerted and clearing.");
        bot.setEntranceForUser(user, null, null);
      } else {
        boolean userHasHeardEntranceRecently = false;
        String soundInfo = "";
        // Clear previous message(s).
        if (pastEntrances.get(guild) == null) {
          pastEntrances.put(guild, new LinkedList<EntranceEvent>());
        } else {
          Queue<EntranceEvent> entrances = pastEntrances.get(guild);
          while (!entrances.isEmpty()) {
            EntranceEvent entrance = entrances.poll();
            entrance.message.delete().queue();
            if (entrance.user.equals(user) && !userHasHeardEntranceRecently) {
              userHasHeardEntranceRecently = true;
              LOG.info("User has heard entrance recently.");
            }
          }
        }
        // Play a sound if there are others to hear.
        if (VoiceUtils.numUsersInVoiceChannels(guild) > 1
            || !userHasHeardEntranceRecently) {
          try {
            if (bot.playFileForEntrance(fileToPlay, user, voiceChannel)) {
              SoundFile sound = bot.getDispatcher().getSoundFileByName(
                                  fileToPlay);
              soundInfo = "Played sound " + formatString(Strings.SOUND_DESC,
                          fileToPlay, sound.getCategory(),
                          sound.getNumberOfPlays()) + ".";
            } else {
              LOG.info("Wanted to play entrance \"" + fileToPlay +
                       "\" for user but did not play a sound.");
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
                  welcomeMessage(user, voiceChannel, soundInfo),
                  (Message m)-> pastEntrances.get(guild).add(
                    new EntranceEvent(m, user)));
          }
        }
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

    if (botsChannel != null && VoiceUtils.numUsersInVoiceChannels(guild) == 0) {
      LOG.info("No more users in " + guild.getName());
      leaveVoiceInGuild(guild);
    } else if (botsChannel != null && botsChannel.getMembers().size() == 1) {
      for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
        if (botsChannel != null && botsChannel.equals(voiceChannel)) continue;
        else if (voiceChannel.getMembers().size() > 0
                 && (guild.getAfkChannel() == null
                     || !voiceChannel.getId().equals(
                       guild.getAfkChannel().getId()))) {
          if (voiceChannel.getMembers().size() == 1
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
    if (botsChannel != null && event.getChannelLeft().equals(botsChannel)) {
      onLeave(event.getChannelLeft(), event.getMember().getUser());
    }
    onJoin(event.getChannelJoined(), event.getMember().getUser());
  }

  public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
    if (bot.isUser(event.getMember().getUser())) {
      LOG.info("Was guild muted.");
      leaveVoiceInGuild(event.getGuild());
    }
  }

  private void leaveVoiceInGuild(Guild guild) {
    if (guild.getAudioManager() != null) {
      LOG.info("Leaving voice channel in " + guild.getName());
      guild.getAudioManager().closeAudioConnection();
    }
  }

  public StyledEmbedMessage welcomeMessage(User user, Channel channel,
      String soundInfo) {
    StyledEmbedMessage m = new StyledEmbedMessage(
      StringUtils.randomString(WELCOMES) + ", " + user.getName() + "!", bot);
    m.setThumbnail(user.getEffectiveAvatarUrl());
    if (!soundInfo.isEmpty()) {
      m.addDescription(soundInfo + Strings.SEPARATOR + user.getAsMention());
    } else {
      m.addDescription("Hey, how you doin'?" + Strings.SEPARATOR +
                       user.getAsMention());
    }
    m.addContent("What Am I?", "I am a bot (*beep boop*). I play sounds. " +
                 "Type `.help` for more information.", false);
    return m;
  }

}
