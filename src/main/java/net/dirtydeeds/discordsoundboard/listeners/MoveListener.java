package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.async.DeleteMessageJob;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dirtydeeds.discordsoundboard.utils.VoiceUtils;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class MoveListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Move");

  private static final List<String> WELCOMES = Arrays.asList(
          "%s wants off this planet.",
          "Just like old times, %s.",
          "Can it wait for a bit, %s? I'm in the middle of some calibrations.",
          "%s? I don't know what to do with them...",
          "%s came when he was six years old. *Nice*.",
          "You subverted my expectations, %s.",
          "Why do you think I came all this way %s?",
          "All hail %s, King of the Andals and the First Men, Lord of the Six Kingdoms and Protector of the Realm");

  private Map<Guild, Queue<EntranceEvent>> pastEntrances;

  public MoveListener(SoundboardBot bot) {
    this.bot = bot;
    this.pastEntrances = new HashMap<>();
  }

  private static class EntranceEvent {
    public Message message;
    public User user;
    EntranceEvent(Message m, User u) {
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
    } else if (vc.getUserLimit() == vc.getMembers().size()) {
      LOG.info("Channel is full.");
      return;
    }

    String fileToPlay = bot.getEntranceForUser(user), soundInfo = "";
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
                  String.format("`%s` from **%s** (**%d** plays)", fileToPlay,
                          s.getCategory(),
                          s.getNumberOfPlays());
        }
      } catch (Exception e) {
        embed(bot.getBotChannel(guild), errorMessage(e, user), (Message m) -> bot.getDispatcher().getAsyncService().runJob(new DeleteMessageJob(m, 240)));
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
    pastEntrances.computeIfAbsent(guild, k -> new LinkedList<>());
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

    private StyledEmbedMessage errorMessage(Exception e, User user) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    String title = String.format(Objects.requireNonNull(StringUtils.randomString(WELCOMES)),
                    "**" + user.getName() + "**");
    StyledEmbedMessage m = new StyledEmbedMessage(title, bot);
    m.setThumbnail(user.getEffectiveAvatarUrl());
    m.addDescription("Bwah" + Strings.SEPARATOR + user.getAsMention());
    m.addContent("Error", StringUtils.truncate(sw.toString(), 512), false);
    m.isError(true);
    return m;
  }

  private StyledEmbedMessage welcomeMessage(User user, Channel channel,
                                           String soundInfo, boolean welcomeInTitle) {
    String title = (welcomeInTitle) ?
            String.format(Objects.requireNonNull(StringUtils.randomString(WELCOMES)),
                    "**" + user.getName() + "**") :
            user.getName() + " came, again.";
    String description = "";
    if (!soundInfo.isEmpty()) {
      description = soundInfo + Strings.SEPARATOR + user.getAsMention();
    }
    StyledEmbedMessage m = StyledEmbedMessage.forUser(bot, user, title, description);
    m.setThumbnail(user.getEffectiveAvatarUrl());
    m.addContent(StringUtils.randomString(Strings.WHATS),
            "I play sounds and automate things. Type `.help` for commands.",
            false);
    Color color = StringUtils.toColor(user.getName());
    m.addFooterText(String.format("(%d, %d, %d)", color.getRed(),
            color.getGreen(), color.getBlue()));
    return m;
  }

}
