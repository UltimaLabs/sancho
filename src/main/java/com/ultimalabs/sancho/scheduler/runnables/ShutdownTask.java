package com.ultimalabs.sancho.scheduler.runnables;

import com.ultimalabs.sancho.scheduler.model.ScheduledTaskDetails;
import com.ultimalabs.sancho.scheduler.service.SchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reloads the task scheduler
 */
@Slf4j
@RequiredArgsConstructor
public class ShutdownTask implements Runnable {

    /**
     * Reference to the traking scheduler service
     */
    private final SchedulerService schedulerService;

    @Override
    public void run() {

        log.info("Started ShutdownTask on thread {}", Thread.currentThread().getName());

        // we wait a little so the caller can send a response
        // back to the client
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (ScheduledTaskDetails task : schedulerService.getScheduledTasks()) {
            task.getFuture().cancel(false);
        }

        System.exit(0);
   
    }
}
