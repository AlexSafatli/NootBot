package net.dirtydeeds.discordsoundboard.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.managers.AudioManager;

public class AudioPlayerSendHandler implements AudioSendHandler {
	
  private final AudioManager voice;
  private final AudioPlayer audioPlayer;
  private AudioFrame lastFrame;

  public AudioPlayerSendHandler(AudioManager voice, AudioPlayer audioPlayer) {
	this.voice = voice;
    this.audioPlayer = audioPlayer;
  }
  
	public AudioPlayer getPlayer() {
		return this.audioPlayer;
	}

  @Override
  public boolean canProvide() {
	if (!voice.isConnected()) return false;
	if (voice.getQueuedAudioConnection() != null) return false;
	lastFrame = audioPlayer.provide();
    return lastFrame != null;
  }

  @Override
  public byte[] provide20MsAudio() {
    return (lastFrame != null) ? lastFrame.data : new byte[0];
  }

  @Override
  public boolean isOpus() {
    return true;
  }
}