package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

import java.util.Date;

public abstract class AbstractAsyncJob implements SoundboardJob {

  private AbstractAsyncEvent pastEvent;
  private long NUMBER_HOURS_BETWEEN = 12;

  protected static class AbstractAsyncEvent {
    public Date time;

    AbstractAsyncEvent(Date time) {
      this.time = time;
    }
  }

  @Override
  public boolean isApplicable(SoundboardDispatcher dispatcher) {
    Date now = new Date(System.currentTimeMillis());
    long hours = 0;
    if (pastEvent != null) hours = (now.getTime() - pastEvent.time.getTime()) / (1000 * 60 * 60);
    return (pastEvent == null || hours >= NUMBER_HOURS_BETWEEN);
  }

  public void run(SoundboardDispatcher dispatcher) {
    handle(dispatcher);
    pastEvent = new AbstractAsyncEvent(new Date(System.currentTimeMillis()));
  }

  abstract void handle(SoundboardDispatcher dispatcher);
}