package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.internal.utils.JDALogger;

public class AudioHandler implements AudioLoadResultHandler {

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
    player.startTrack(playlist.getTracks().get(0), false);
  }

  @Override
  public void noMatches() {
    JDALogger.getLog("Audio").warn("No match for audio identifier.");
  }

  @Override
  public void loadFailed(FriendlyException exception) {
    exception.printStackTrace();
  }
}