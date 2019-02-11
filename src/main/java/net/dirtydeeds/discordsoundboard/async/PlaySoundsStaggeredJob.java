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

  public PlaySoundsStaggeredJob(String[] sounds, SoundboardBot bot, User user,
                                String category) {
    this(sounds, bot, user);
    this.category = category;
    this.timestamp = someTimeFromNow();
  }

  private Date someTimeFromNow() {
    return new Date(System.currentTimeMillis() + Math.max(rand.nextInt(60000), 60000));
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (timestamp != null) {
      Date now = new Date(System.currentTimeMillis());
      return now.after(timestamp);
    }
    return true;
  }

  private void tryAgain(SoundboardDispatcher dispatcher) {
    dispatcher.getAsyncService().runJob(new PlaySoundsStaggeredJob(sounds, bot, user));
  }

  private void next(SoundboardDispatcher dispatcher) {
    dispatcher.getAsyncService().runJob(new PlaySoundsStaggeredJob(Arrays.copyOfRange(sounds, 1, sounds.length), bot, user));
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
      if (voice == null) {
        bot.sendMessageToUser(SoundboardBot.NOT_IN_VOICE_CHANNEL_MESSAGE, user);
        return null;
      }
      bot.moveToChannel(voice);
    } catch (Exception e) {
      return voice;
    }
    return voice;
  }

  public void run(SoundboardDispatcher dispatcher) {
    if (bot == null || sounds == null || sounds.length == 0) return;
    VoiceChannel voice = getVoiceChannel();
    if (voice == null) tryAgain(dispatcher);
    Guild guild = voice.getGuild();

    AudioTrackScheduler scheduler = bot.getSchedulerForGuild(guild);
    String sound = sounds[0];
    if (sound == null || sound.equals("*")) {
      if (category == null) try {
        schedule(scheduler, bot.getRandomSoundName());
        next(dispatcher);
      } catch (Exception e) {
        next(dispatcher);
      }
      else try {
        schedule(scheduler, bot.getRandomSoundNameForCategory(category));
        next(dispatcher);
      } catch (Exception e) {
        next(dispatcher);
      }
    } else {
      try {
        schedule(scheduler, sound);
        next(dispatcher);
      } catch (Exception e) {
        next(dispatcher);
      }
    }
  }

}
