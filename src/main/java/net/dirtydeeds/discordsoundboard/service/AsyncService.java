package net.dirtydeeds.discordsoundboard.service;

import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dirtydeeds.discordsoundboard.utils.Periodic;
import net.dv8tion.jda.internal.utils.SimpleLogger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author asafatli
 * Independent server thread that ticks periodically to perform asynchronous tasks.
 */
@Service
public class AsyncService {

  public static final SimpleLogger LOG = SimpleLogger.getLog("Jobs");
  private List<SoundboardJob> jobs;
  private Stack<SoundboardJob> tasks;

  public AsyncService() {
    jobs = new LinkedList<>();
    tasks = new Stack<>();
  }

  // Adds to a job to be run periodically.
  synchronized void addJob(SoundboardJob job) {
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
    List<SoundboardBot> bots = dispatcher.getBots();
    while (!bots.isEmpty()) {
      // Only fire a certain amount of times a minute.
      long millisecondsToWait = Periodic.EVERY_QUARTER_HOUR * 1000;
      try {
        Thread.sleep(millisecondsToWait);
      } catch (InterruptedException e) {
        LOG.fatal("Thread sleep failed for time: " + millisecondsToWait);
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
