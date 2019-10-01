package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

import java.util.Date;
import java.util.function.Consumer;

public class PeriodicLambdaJob implements SoundboardJob {

  private Consumer<SoundboardBot> lambda;
  private int numSecondsBetween;
  private Date next; // only run this after a certain time

  PeriodicLambdaJob(Consumer<SoundboardBot> lambda, int numSecondsBetween) {
    this.lambda = lambda;
    this.numSecondsBetween = numSecondsBetween;
    this.next = newTimestamp();
  }

  private Date newTimestamp() {
    return new Date(System.currentTimeMillis() + numSecondsBetween * 1000);
  }

  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    if (next != null) {
      return (new Date(System.currentTimeMillis())).after(next);
    }
    return true;
  }

  public void run(SoundboardDispatcher dispatcher) {
    dispatcher.runLambda(lambda);
    next = newTimestamp();
  }

}
