package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.api.entities.Message;

import java.util.Date;

public class DeleteMessageJob implements SoundboardJob {

  private Message message;
  private Date timestamp; // only run this after a certain time

  public DeleteMessageJob(Message message) {
    this.message = message;
    this.timestamp = new Date(System.currentTimeMillis() + 2000); // Run this 2s later.
  }

  public DeleteMessageJob(Message message, int numSecondsLater) {
    this.message = message;
    this.timestamp = new Date(System.currentTimeMillis() +
                              numSecondsLater * 1000);
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (timestamp != null) {
      return (new Date(System.currentTimeMillis())).after(timestamp);
    }
    return true;
  }

  public void run(SoundboardDispatcher dispatcher) {
    message.delete().queue();
  }
}