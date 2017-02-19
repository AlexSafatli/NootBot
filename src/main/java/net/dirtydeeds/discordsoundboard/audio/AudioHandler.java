package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

public class AudioHandler implements AudioLoadResultHandler {

	public static final SimpleLog LOG = SimpleLog.getLog("AudioHandler");
	
	private final AudioPlayer player;
	private final AudioManager audio;
	
	private static final int WAIT_TIME = 100;
	
	public AudioHandler(AudioPlayer player) {
		this.player = player;
		this.audio = null;
	}
	
	public AudioHandler(AudioPlayer player, AudioManager audio) {
		this.player = player;
		this.audio = audio;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		waitOnChannelConnection();
		player.startTrack(track, false);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		LOG.info("Loaded playlist " + playlist.getName());
		waitOnChannelConnection();
		boolean started = player.startTrack(playlist.getTracks().get(0), false);
		LOG.info("Started first track with success: " + started);
	}

	@Override
	public void noMatches() {
		LOG.warn("No match for audio identifier.");
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		exception.printStackTrace();
	}
	
	private void waitOnChannelConnection() {
		if (audio != null) {
			int i = 0, maxIters = 50;
			synchronized (this) {
				while (!audio.isConnected() && audio.isAttemptingToConnect()) {
					try {
						wait(WAIT_TIME); ++i;
						if (i > maxIters) break;
					} catch (InterruptedException ie) {
						LOG.warn("Waiting for audio connection interrupted.");
					}
				}
			}
		}
	}

}
