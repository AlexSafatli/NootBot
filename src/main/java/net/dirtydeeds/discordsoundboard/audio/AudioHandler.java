package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.utils.SimpleLog;

public class AudioHandler implements AudioLoadResultHandler {

	public static final SimpleLog LOG = SimpleLog.getLog("AudioHandler");
	
	private final AudioPlayer player;
	
	public AudioHandler(AudioPlayer player) {
		this.player = player;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		player.startTrack(track, false);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		LOG.info("Loaded playlist " + playlist.getName());
		player.startTrack(playlist.getTracks().get(0), false);
	}

	@Override
	public void noMatches() {
		LOG.warn("No match for audio identifier.");
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		exception.printStackTrace();
	}

}
