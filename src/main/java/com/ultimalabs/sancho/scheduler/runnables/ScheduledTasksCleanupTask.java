package com.ultimalabs.sancho.scheduler.runnables;

import java.util.List;

import com.ultimalabs.sancho.scheduler.model.ScheduledTaskDetails;
import com.ultimalabs.sancho.scheduler.service.SchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cleanup of scheduled tasks
 */
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasksCleanupTask implements Runnable {

    private final SchedulerService schedulerService;

    @Override
    public void run() {
        log.info("Started ScheduledTasksCleanupTask on thread {}", Thread.currentThread().getName());

        while (true) { // NOSONAR
            schedulerService.getScheduledTasks().removeIf(task -> task.getFuture().isDone());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Interrupted exception: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

}
