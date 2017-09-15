package net.dirtydeeds.discordsoundboard.async;

import java.util.Date;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.core.entities.Channel;

public class DeleteInactiveChannelJob implements SoundboardJob {

  private static final int MIN_INACTIVITY_TIME = 30 * 60 * 1000; // 30 minutes

  private Channel channel;
  private Date timestamp; // only run this after a certain time

  public DeleteInactiveChannelJob(Channel channel) {
    this.channel = channel;
    this.timestamp = new Date(System.currentTimeMillis() + MIN_INACTIVITY_TIME);
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (channel != null) {
      if (channel.getMembers().size() > 0) {
        timestamp = new Date(System.currentTimeMillis() + MIN_INACTIVITY_TIME);
        return false;
      }
      Date now = new Date(System.currentTimeMillis());
      return now.after(timestamp);
    }
    return true;
  }

  public void run(SoundboardDispatcher dispatcher) {
    channel.delete().queue();
  }
}