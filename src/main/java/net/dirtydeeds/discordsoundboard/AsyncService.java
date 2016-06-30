package net.dirtydeeds.discordsoundboard;

import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.utils.SimpleLog;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author asafatli.
 */
@Service
public class AsyncService {

	public static final SimpleLog LOG = SimpleLog.getLog("Jobs");
	private static final float TICK_RATE_PER_MINUTE = 1;
	private List<SoundboardJob> jobs;
	
	public AsyncService() {
		jobs = new LinkedList<>();
	}
	
	public void addJob(SoundboardJob job) {
		LOG.info("Adding job " + job.getClass().getSimpleName());
		jobs.add(job);
	}
	
    @Async
    public void maintain(SoundboardDispatcher dispatcher) {
    	LOG.info("Starting service with tick rate per minute: " + TICK_RATE_PER_MINUTE);
    	List<SoundboardBot> bots = dispatcher.getBots();
    	while (!bots.isEmpty()) {
    		// Only fire a certain amount of times a minute.
    		long millisecondsToWait = (long)(1/TICK_RATE_PER_MINUTE) * 60000;
    		try {
				Thread.sleep(millisecondsToWait);
			} catch (InterruptedException e) {
				LOG.fatal("Thread sleep failed for ms waiting time: " + 
						millisecondsToWait);
			}
    		// Perform the action(s).
    		for (SoundboardJob job : jobs) {
    			if (job.isApplicable(dispatcher)) {
    				try {
        				job.run(dispatcher);
        			} catch (Exception e) {
        				LOG.fatal("Exception when running " + job.getClass().getSimpleName() + 
        						": " + e.toString() + " => " + e.getMessage());
        				continue;
        			}
        			LOG.info("Finished running job " + job.getClass().getSimpleName());
    			}
    		}
    	}
    }

}
