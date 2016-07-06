package net.dirtydeeds.discordsoundboard.async;

import java.util.Date;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

public abstract class AbstractAsyncJob implements SoundboardJob {

	protected AbstractAsyncEvent pastEvent;
	protected long NUMBER_HOURS_BETWEEN = 12;
	
	protected class AbstractAsyncEvent {
		public Date time;
		public AbstractAsyncEvent(Date time) {
			this.time = time;
		}
	}
	
	@Override
	public boolean isApplicable(SoundboardDispatcher dispatcher) {
		Date now = new Date(System.currentTimeMillis());
    	long hours = 0;
    	if (pastEvent != null) hours = (now.getTime() - pastEvent.time.getTime())/(1000*60*60);
		return (pastEvent == null || hours >= NUMBER_HOURS_BETWEEN);
	}
	
	public void run(SoundboardDispatcher dispatcher) {
		handle(dispatcher);
		pastEvent = new AbstractAsyncEvent(new Date(System.currentTimeMillis()));
	}
	
	public abstract void handle(SoundboardDispatcher dispatcher);

}
