package net.dirtydeeds.discordsoundboard.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dv8tion.jda.core.utils.SimpleLog;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author asafatli
 * Independent server thread that ticks periodically to perform asynchronous tasks.
 */
@Service
public class AsyncService {

	public static final SimpleLog LOG = SimpleLog.getLog("Jobs");
	private static final float TICK_RATE_PER_MINUTE = 60;
	private List<SoundboardJob> jobs;
	private Stack<SoundboardJob> tasks;
	
	public AsyncService() {
		jobs = new LinkedList<>();
		tasks = new Stack<>();
	}
	
	// Adds to a job to be run periodically.
	public synchronized void addJob(SoundboardJob job) {
		LOG.info("Adding job " + job.getClass().getSimpleName());
		jobs.add(job);
	}
	
	// Runs a job only once as a "task".
	public synchronized void runJob(SoundboardJob job) {
		LOG.info("Adding task " + job.getClass().getSimpleName());
		tasks.push(job);
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
    		// See if there are any tasks.
    		Collection<SoundboardJob> unrunTasks = new LinkedList<>();
    		while (!tasks.isEmpty()) {
    			SoundboardJob task = tasks.pop();
    			if (task.isApplicable(dispatcher)) {
	    			try {
	    				task.run(dispatcher);
	    			} catch (Exception e) {
	    				LOG.fatal("Exception when running task " + task.getClass().getSimpleName() + 
	    						": " + e.toString() + " => " + e.getMessage());
	    				continue;
	    			}
	    			LOG.info("Finished running task " + task.getClass().getSimpleName());
    			} else {
    				unrunTasks.add(task);
    			}
    		}
    		for (SoundboardJob task : unrunTasks) tasks.push(task);
    		// Perform any async job(s).
    		Iterator<SoundboardJob> jobsIterator = jobs.iterator();
    		while (jobsIterator.hasNext()) {
    			SoundboardJob job = jobsIterator.next();
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
