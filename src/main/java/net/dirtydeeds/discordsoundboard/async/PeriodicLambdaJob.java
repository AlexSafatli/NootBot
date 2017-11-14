package net.dirtydeeds.discordsoundboard.async;

import java.util.Date;
import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

public class PeriodicLambdaJob implements SoundboardJob {

  private Consumer<SoundboardBot> lambda;
  private int numSecondsBetween;
  private Date next; // only run this after a certain time

  public PeriodicLambdaJob(Consumer<SoundboardBot> lambda) {
    this(lambda, 6000);
  }

  public PeriodicLambdaJob(Consumer<SoundboardBot> lambda,
                           int numSecondsBetween) {
    this.lambda = lambda;
    this.numSecondsBetween = numSecondsBetween;
    this.next = newTimestamp();
  }

  private Date newTimestamp() {
    return new Date(System.currentTimeMillis() + numSecondsBetween * 1000);
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
    next = newTimestamp();
  }

}
