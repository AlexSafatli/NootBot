package net.dirtydeeds.discordsoundboard.async;

import java.util.List;
import java.util.LinkedList;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.dirtydeeds.discordsoundboard.audio.AudioScheduler;
import net.dirtydeeds.discordsoundboard.audio.AudioTrackScheduler;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;

public class PlaySoundsJob implements SoundboardJob {

  private String[] sounds;
  private SoundboardBot bot;
  private User user;
  private String category;

  public PlaySoundsJob(String[] sounds, SoundboardBot bot, User user) {
    this.sounds = sounds;
    this.bot = bot;
    this.user = user;
  }

  public PlaySoundsJob(String[] sounds, SoundboardBot bot, User user,
                       String category) {
    this(sounds, bot, user);
    this.category = category;
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    return true;
  }

  private long schedule(SoundboardDispatcher dispatcher,
                        AudioTrackScheduler scheduler, String name) throws
          InterruptedException, ExecutionException, TimeoutException {
    long time = 0;
    SoundFile f = bot.getSoundMap().get(name);
    f.addOneToNumberOfPlays();
    Long duration = f.getDuration();
    if (duration != null) time = duration;
    bot.getDispatcher().saveSound(f);
    scheduler.load(f.getSoundFile().getPath(),
            new AudioScheduler(scheduler)).get(5, TimeUnit.SECONDS);
    return time;
  }

  public void run(SoundboardDispatcher dispatcher) {
    if (bot == null || sounds == null ||
            sounds.length == 0 ||
            !dispatcher.getBots().contains(bot))
      return;

    boolean same = true, randomed = false, allRandomed = true;

    Guild guild;
    VoiceChannel voice;

    String firstSound = null, end = "";

    long timePlaying = 0;

    MessageBuilder mb = new MessageBuilder(1024);

    try {
      voice = bot.getUsersVoiceChannel(user);
      if (voice == null) {
        bot.sendMessageToUser("You're not in a channel.", user);
        return;
      }
      guild = voice.getGuild();
      bot.moveToChannel(voice);
    } catch (Exception e) {
      bot.sendMessageToUser("I couldn't join your channel:\n`" + e.getMessage() + "`",
              user);
      return;
    }

    AudioTrackScheduler scheduler = bot.getSchedulerForGuild(guild);
    for (int i = 0; i < sounds.length; ++i) {
      String sound = sounds[i];
      if (sound == null || sound.equals("*")) {
        if (category == null) try {
          sound = bot.getRandomSoundName();
          if (sound != null)
            timePlaying += schedule(dispatcher, scheduler, sound);
        } catch (Exception e) {
          continue;
        }
        else try {
          sound = bot.getRandomSoundNameForCategory(category);
          if (sound != null)
            timePlaying += schedule(dispatcher, scheduler, sound);
        } catch (Exception e) {
          continue;
        }
        randomed = true;
      } else {
        if (allRandomed) allRandomed = false;
        try {
          timePlaying += schedule(dispatcher, scheduler, sound);
        } catch (Exception e) {
          continue;
        }
      }
      if (sound != null) {
        if (firstSound == null) firstSound = sound;
        if (!sound.equals(firstSound)) same = false;
        SoundFile f = dispatcher.getSoundFileByName(sound);
        if (f != null)
          mb.append("`" + sound + "` (**" + f.getNumberOfPlays() + "**)");
        else
          mb.append("`" + sound + "`");
        if (i == sounds.length - 2 && sounds.length > 1) mb.append(", and ");
        else if (i < sounds.length - 1) mb.append(", ");
      }
    }

    if (category != null) {
      Category c = bot.getSoundCategory(category);
      if (c != null) end += "from category **" + c.getName() + "** ";
    }
    if (allRandomed) end += "*which were all randomed*";
    else if (randomed) end += "*some randomed*";

    if (guild != null) {
      List<Message> messages;
      if (!same || sounds.length == 1) {
        messages = makeMessages(
                "Queued sound" + (sounds.length > 1 ? "s " : " ") +
                        end + " \u2014 " + user.getAsMention(), user, mb, sounds,
                timePlaying);
      } else {
        messages = makeMessages(user.getAsMention(), user, null, sounds, timePlaying);
      }

      final int timeUntilDelete = (int)timePlaying + 120;
      TextChannel c = bot.getBotChannel(guild);
      for (Message msg : messages) {
        if (c != null) {
          RestAction<Message> m = c.sendMessage(msg);
          if (m != null) {
            m.queue((Message s) -> dispatcher.getAsyncService().runJob(new DeleteMessageJob(s, timeUntilDelete)));
          }
        }
      }
    }
  }

  private List<Message> makeMessages(String description, User user,
                                     MessageBuilder mb, String[] sounds,
                                     long duration) {
    List<Message> messages = new LinkedList<>();
    if (mb != null) {
      for (String str : mb) {
        boolean paginatedMessage = messages.size() >= 1;
        String titleSuffix =
                paginatedMessage ? " (" + (messages.size() + 1) + ")" : "";
        StyledEmbedMessage msg =
                StyledEmbedMessage.forUser(
                        bot, user, "Playing **" + sounds.length + "** Sounds" +
                                titleSuffix, paginatedMessage ? "" : description);
        msg.addContent("Sounds Queued", str, false);
        if (duration > 0)
          msg.addContent("Total Duration",duration + " seconds", false);
        messages.add(msg.getMessage());
      }
    } else {
      messages.add(StyledEmbedMessage.forUser(
              bot, user, "Looping `" + sounds[0] + "`", description).getMessage());
    }
    return messages;
  }

}
