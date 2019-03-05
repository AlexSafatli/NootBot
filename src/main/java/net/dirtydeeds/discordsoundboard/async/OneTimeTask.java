package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

import java.util.Date;
import java.util.function.Consumer;

public class OneTimeTask implements SoundboardJob {

  private Consumer<SoundboardBot> lambda;
  private Date next; // only run this at a certain time

  public OneTimeTask(Consumer<SoundboardBot> lambda,
                     Date when) {
    this.lambda = lambda;
    this.next = when;
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (next != null) {
      Date now = new Date(System.currentTimeMillis());
      return now.after(next);
    }
    return true;
  }

  public void run(SoundboardDispatcher dispatcher) {
    dispatcher.runLambda(lambda);
  }

}
