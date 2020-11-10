package net.dirtydeeds.discordsoundboard.service;

import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dirtydeeds.discordsoundboard.utils.Periodic;
import net.dv8tion.jda.internal.utils.JDALogger;
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

  private List<SoundboardJob> jobs;
  private Stack<SoundboardJob> tasks;

  public AsyncService() {
    jobs = new LinkedList<>();
    tasks = new Stack<>();
  }

  // Adds to a job to be run periodically.
  synchronized void addJob(SoundboardJob job) {
    JDALogger.getLog("Jobs").info("Adding job " + job.getClass().getSimpleName());
    jobs.add(job);
  }

  // Runs a job only once as a "task".
  public synchronized void runJob(SoundboardJob job) {
    JDALogger.getLog("Jobs").info("Adding task " + job.getClass().getSimpleName());
    tasks.push(job);
  }

  @Async
  public void maintain(SoundboardDispatcher dispatcher) {
    List<SoundboardBot> bots = dispatcher.getBots();
    while (!bots.isEmpty()) {
      // Only fire every 15 minutes.
      long millisecondsToWait = Periodic.EVERY_QUARTER_HOUR * 1000;
      try {
        Thread.sleep(millisecondsToWait);
      } catch (InterruptedException e) {
        JDALogger.getLog("Jobs").error("Thread sleep failed for time: " + millisecondsToWait);
      }
      // See if there are any tasks.
      Collection<SoundboardJob> unrunTasks = new LinkedList<>();
      while (!tasks.isEmpty()) {
        SoundboardJob task = tasks.pop();
        if (task.isApplicable(dispatcher)) {
          try {
            task.run(dispatcher);
          } catch (Exception e) {
            JDALogger.getLog("Jobs").error("Exception when running task " + task.getClass().getSimpleName() +
                      ": " + e.toString() + " => " + e.getMessage());
            continue;
          }
          JDALogger.getLog("Jobs").info("Finished running task " + task.getClass().getSimpleName());
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
            JDALogger.getLog("Jobs").error("Exception when running " + job.getClass().getSimpleName() +
                    ": " + e.toString() + " => " + e.getMessage());
            continue;
          }
          JDALogger.getLog("Jobs").info("Finished running job " + job.getClass().getSimpleName());
        }
      }
    }
  }

}
