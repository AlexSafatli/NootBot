package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.internal.utils.JDALogger;

public class AudioHandler implements AudioLoadResultHandler {

  private final AudioPlayer player;
  private final boolean nointerrupt;

  public AudioHandler(AudioPlayer player) {
    this.player = player;
    this.nointerrupt = false;
  }

  public AudioHandler(AudioSendHandler audio) {
    this.player = ((AudioPlayerSendHandler)(audio)).getPlayer();
    this.nointerrupt = false;
  }

  public AudioHandler(AudioSendHandler audio, boolean nointerrupt) {
    this.player = ((AudioPlayerSendHandler)(audio)).getPlayer();
    this.nointerrupt = nointerrupt;
  }

  @Override
  public void trackLoaded(AudioTrack track) {
    player.startTrack(track, nointerrupt);
  }

  @Override
  public void playlistLoaded(AudioPlaylist playlist) {
    player.startTrack(playlist.getTracks().get(0), nointerrupt);
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