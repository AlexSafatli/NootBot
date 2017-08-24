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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;

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
		if (bot == null || sounds == null || sounds.length == 0) return;
		boolean same = true, randomed = false, allRandomed = true;
		Guild guild = null;
		VoiceChannel voice = null;
		String firstSound = null, end = "";
		long timePlaying = 0;
		MessageBuilder mb = new MessageBuilder(1024);
		try {
			voice = bot.getUsersVoiceChannel(user);
			if (voice == null) {
				bot.sendMessageToUser(SoundboardBot.NOT_IN_VOICE_CHANNEL_MESSAGE, user);
				return;
			}
			guild = voice.getGuild();
			bot.moveToChannel(voice);
		} catch (Exception e) {
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
					} catch (Exception e) continue;
				else try {
						sound = bot.getRandomSoundNameForCategory(category);
						if (sound != null)
							timePlaying += schedule(dispatcher, scheduler, sound);
					} catch (Exception e) continue;
				randomed = true;
			} else {
				if (allRandomed) allRandomed = false;
				try {
					if (sound != null)
						timePlaying += schedule(dispatcher, scheduler, sound);
				} catch (Exception e) continue;
			}
			if (sound != null) {
				try {
					if (firstSound == null) firstSound = sound;
					if (!sound.equals(firstSound)) same = false;
					mb.append("`" + sound + "` (**" +
					          dispatcher.getSoundFileByName(sound).getNumberOfPlays() +
					          "**)");
					if (i == sounds.length - 2 && sounds.length > 1) mb.append(", and ");
					else if (i < sounds.length - 1) mb.append(", ");
				} catch (Exception e) continue;
			}
		}

		if (category != null) {
			Category c = bot.getSoundCategory(category);
			if (c != null) end += "from category **" + c.getName() + "** ";
		}
		if (allRandomed) end += "*all of which were randomed*";
		else if (randomed) end += "*some of which were randomed*";

		if (guild != null) {
			List<Message> msgs;
			if (!same || sounds.length == 1) {
				msgs = makeMessages("Queued sound(s) " + end + " \u2014 " +
				                    user.getAsMention(), user, mb, timePlaying);
			} else {
				msgs = makeMessages("Looping sound `" + firstSound + "` **" +
				                    sounds.length + "** times \u2014 " +
				                    user.getAsMention(), user, null, timePlaying);
			}

			TextChannel c = bot.getBotChannel(guild);
			for (Message msg : msgs) {
				if (c != null) {
					RestAction<Message> m = c.sendMessage(msg);
					if (m != null) {
						m.queue((Message s)-> {
							dispatcher.getAsyncService().runJob(new DeleteMessageJob(s, 1024));
						});
					}
				}
			}
		}
	}

	private List<Message> makeMessages(String description, User user,
	                                   MessageBuilder mb, long duration) {
		List<Message> msgs = new LinkedList<>();
		if (mb != null) {
			for (String str : mb) {
				String titleSuffix =
				  (msgs.size() >= 1) ? " (*" + (msgs.size() + 1) + "*)" : "";
				StyledEmbedMessage msg =
				  StyledEmbedMessage.forUser(
				    bot, user, "Playing Multiple Sounds" + titleSuffix, description);
				if (mb != null) msg.addContent("Sounds Queued", str, false);
				if (duration > 0)
					msg.addContent("Total Duration",
					               Long.toString(duration) + " seconds", false);
				msgs.add(msg.getMessage());
			}
		} else {
			msgs.add(StyledEmbedMessage.forUser(
			           bot, user, "Loop", description).getMessage());
		}
		return msgs;
	}

}
