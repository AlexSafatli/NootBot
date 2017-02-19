package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioScheduler implements AudioLoadResultHandler {

	private AudioTrackScheduler scheduler;
	
	public AudioScheduler(AudioTrackScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		scheduler.queue(track);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
	}

	@Override
	public void noMatches() {
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		exception.printStackTrace();
	}

}
