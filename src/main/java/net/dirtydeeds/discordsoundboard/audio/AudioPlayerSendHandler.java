package net.dirtydeeds.discordsoundboard.audio;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.managers.AudioManager;

public class AudioPlayerSendHandler implements AudioSendHandler {

  private final AudioManager voice;
  private final AudioPlayer audioPlayer;
  private AudioFrame lastFrame;
  private int numberFramesSinceLastFrame;

  public AudioPlayerSendHandler(AudioManager voice, AudioPlayer audioPlayer) {
    this.voice = voice;
    this.audioPlayer = audioPlayer;
  }

  public AudioPlayer getPlayer() {
    return this.audioPlayer;
  }

  @Override
  public boolean canProvide() {
    if (!voice.isConnected() || voice.getQueuedAudioConnection() != null)
      return false;
    if (lastFrame != null)
      lastFrame = audioPlayer.provide();
    else
      ++numberFramesSinceLastFrame;
    return lastFrame != null && (numberFramesSinceLastFrame > 25);
  }

  @Override
  public ByteBuffer provide20MsAudio() {
    if (lastFrame != null)
      return ByteBuffer.wrap(lastFrame.getData());
    else
      return null;
  }

  @Override
  public boolean isOpus() {
    return true;
  }
}