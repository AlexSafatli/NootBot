package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.audio.AudioScheduler;
import net.dirtydeeds.discordsoundboard.audio.AudioTrackScheduler;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlaySoundsStaggeredJob implements SoundboardJob {

  private static final int MAX_DURATION = 4;

  private Random rand = new Random();
  private String[] sounds;
  private SoundboardBot bot;
  private User user;
  private String category;
  private Date timestamp;

  public PlaySoundsStaggeredJob(String[] sounds, SoundboardBot bot, User user) {
    this.sounds = sounds;
    this.bot = bot;
    this.user = user;
    this.timestamp = someTimeFromNow();
  }

  public PlaySoundsStaggeredJob(String[] sounds, SoundboardBot bot, User user, String category) {
    this(sounds, bot, user);
    this.category = category;
    this.timestamp = someTimeFromNow();
  }

  private Date someTimeFromNow() {
    return new Date(System.currentTimeMillis() + 1000 + rand.nextInt(600000));
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (timestamp != null) {
      Date now = new Date(System.currentTimeMillis());
      return now.after(timestamp);
    }
    return true;
  }

  private void handleException(Exception e) {
    bot.sendMessageToUser("Something went wrong when I tried " +
            "to play a sound for you" + ((sounds[0] != null) ? " `" +
            sounds[0] + "`." : ".") + " Encountered exception => " +
            e.getMessage(), user);
  }

  private void tryAgain(SoundboardDispatcher dispatcher) {
    dispatcher.getAsyncService().runJob(new PlaySoundsStaggeredJob(sounds, bot, user, category));
  }

  private void next(SoundboardDispatcher dispatcher) {
    dispatcher.getAsyncService().runJob(new PlaySoundsStaggeredJob(Arrays.copyOfRange(sounds, 1, sounds.length), bot, user, category));
  }

  private void schedule(AudioTrackScheduler scheduler, String name) throws
          InterruptedException, ExecutionException, TimeoutException {
    if (name == null) return;
    SoundFile f = bot.getSoundMap().get(name);
    f.addOneToNumberOfPlays();
    bot.getDispatcher().saveSound(f);
    scheduler.load(f.getSoundFile().getPath(),
            new AudioScheduler(scheduler)).get(5, TimeUnit.SECONDS);
  }

  private VoiceChannel getVoiceChannel() {
    VoiceChannel voice = null;
    try {
      voice = bot.getUsersVoiceChannel(user);
      if (voice != null) bot.moveToChannel(voice);
    } catch (Exception e) {
      handleException(e);
    }
    return voice;
  }

  public void run(SoundboardDispatcher dispatcher) {
    if (bot == null || sounds == null || sounds.length == 0 || !dispatcher.getBots().contains(bot))
      return;

    VoiceChannel voice = getVoiceChannel();
    if (voice == null) {
      tryAgain(dispatcher);
      return;
    }
    Guild guild = voice.getGuild();

    AudioTrackScheduler scheduler = bot.getSchedulerForGuild(guild);
    String sound = sounds[0];
    if (sound == null || sound.equals("*")) {
      if (category == null) try {
        schedule(scheduler, bot.getRandomSoundName(MAX_DURATION));
      } catch (Exception e) {
        handleException(e);
      }
      else try {
        schedule(scheduler, bot.getRandomSoundNameForCategory(category, MAX_DURATION));
      } catch (Exception e) {
        handleException(e);
      }
    } else {
      try {
        schedule(scheduler, sound);
      } catch (Exception e) {
        handleException(e);
      }
    }
    next(dispatcher);
  }

}
