package net.dirtydeeds.discordsoundboard.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.dirtydeeds.discordsoundboard.audio.AudioScheduler;
import net.dirtydeeds.discordsoundboard.audio.AudioTrackScheduler;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
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
	
	public PlaySoundsJob(String[] sounds, SoundboardBot bot, User user, String category) {
		this(sounds, bot, user);
		this.category = category;
	}

	public boolean isApplicable(SoundboardDispatcher dispatcher) {
		return true;
	}

	private void play(SoundboardDispatcher dispatcher, AudioTrackScheduler scheduler, String name) throws InterruptedException, ExecutionException, TimeoutException {
		SoundFile f = bot.getSoundMap().get(name);
	    scheduler.load(f.getSoundFile().getPath(), new AudioScheduler(scheduler)).get(5, TimeUnit.SECONDS);
	}
	
	public void run(SoundboardDispatcher dispatcher) {
		if (bot != null && sounds != null && sounds.length > 0) {
			boolean same = true, randomed = false, allRandomed = true;
			Guild guild = null;
			VoiceChannel voice = null;
			String firstSound = null;
			try {
				voice = bot.getUsersVoiceChannel(user);
				guild = voice.getGuild();
				bot.moveToChannel(voice);
			} catch (Exception e) { return; }
			StringBuilder sb = new StringBuilder();
			AudioTrackScheduler scheduler = bot.getSchedulerForGuild(guild);
			for (int i = 0; i < sounds.length; ++i) {
				String sound = sounds[i];
				if (sound == null || sound.equals("*")) {
					if (category == null) try {
						sounds[i] = bot.getRandomSoundName();
						if (sounds[i] != null) {
							play(dispatcher, scheduler, sounds[i]);
						}
					} catch (Exception e) { e.printStackTrace(); continue; }
					else try {
						sounds[i] = bot.getRandomSoundNameForCategory(category);
						if (sounds[i] != null) {
							play(dispatcher, scheduler, sounds[i]);
						}
					} catch (Exception e) { e.printStackTrace(); continue; }
					randomed = true;
				} else {
					if (allRandomed) allRandomed = false;
					try {
						if (sound != null) {
							play(dispatcher, scheduler, sounds[i]);
						}
					} catch (Exception e) { e.printStackTrace(); continue; }
				}
				if (sounds[i] != null) {
					if (sound == null) sound = sounds[i];
					if (firstSound == null) firstSound = sound;
					if (!sound.equals(firstSound)) same = false;
					sb.append("`" + sound + "` (**" + dispatcher.getSoundFileByName(sound).getNumberOfPlays() + "** plays)");
					if (i == sounds.length-2 && sounds.length > 1) sb.append(", and ");
					else if (i < sounds.length-1) sb.append(", ");
				}
			}
			String end = "";
			if (category != null) end += " from category **" + category + "**";
			if (allRandomed) end += " *all of which were randomed*";
			else if (randomed) end += " *some of which were randomed*";
			if (guild != null) {
				RestAction<Message> m = null;
				if (!same || sounds.length == 1) m = guild.getPublicChannel().sendMessage(embedMessage("Queued sound(s) " + end + " " + user.getAsMention() + ".", user, sb));
				else m = guild.getPublicChannel().sendMessage(embedMessage("Looping `" + firstSound + "` **" + sounds.length + "** times " + user.getAsMention() + ".", user, null));
				if (m != null) {
					try {
						dispatcher.getAsyncService().runJob(new DeleteMessageJob(m.block(), 120));
					} catch (RateLimitedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private Message embedMessage(String description, User user, StringBuilder sb) {
		StyledEmbedMessage msg = new StyledEmbedMessage("Playing Multiple Sounds");
		msg.addDescription(description);
		if (sb != null) msg.addContent("Sounds Queued", sb.toString(), false);
		return msg.getMessage();
	}
	
}
