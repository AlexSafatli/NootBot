package net.dirtydeeds.discordsoundboard.audio;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;


public class AudioTrackScheduler extends AudioEventAdapter {

  private Queue<AudioTrack> tracks;
  private AudioPlayerManager audioManager;
  private AudioPlayer audioPlayer;

  public AudioTrackScheduler(AudioPlayerManager manager, AudioPlayer player) {
    this.tracks = new LinkedList<>();
    this.audioManager = manager;
    this.audioPlayer = player;
  }

  public Future<Void> load(String identifier, AudioLoadResultHandler handler) {
    return audioManager.loadItem(identifier, handler);
  }

  public void queue(AudioTrack track) {
    if (!audioPlayer.startTrack(track, true)) {
      tracks.add(track); // Queue it if not started.
    }
  }

  public List<String> clear() {
    List<String> ids = getIdentifiers();
    tracks.clear();
    return ids;
  }

  public List<String> getIdentifiers() {
    List<String> identifiers = new LinkedList<>();
    for (AudioTrack t : tracks) {
      identifiers.add(t.getIdentifier());
    }
    return identifiers;
  }

  @Override
  public void onPlayerPause(AudioPlayer player) {
    super.onPlayerPause(player);
  }

  @Override
  public void onPlayerResume(AudioPlayer player) {
    super.onPlayerResume(player);
  }

  @Override
  public void onTrackStart(AudioPlayer player, AudioTrack track) {
    super.onTrackStart(player, track);
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    // TODO have + to numberOfPlays for a sound done here
    super.onTrackEnd(player, track, endReason);
    if (endReason.mayStartNext && !tracks.isEmpty()) {
      player.playTrack(tracks.poll());
    }

    // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
    // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
    // endReason == STOPPED: The player was stopped.
    // endReason == REPLACED: Another track started playing while this had not finished
    // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
    //                       clone of this back to your queue
  }

  @Override
  public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
    super.onTrackException(player, track, exception);
  }

  @Override
  public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
    super.onTrackStuck(player, track, thresholdMs);
  }

  @Override
  public void onEvent(AudioEvent event) {
    super.onEvent(event);
  }
}