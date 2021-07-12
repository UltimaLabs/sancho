package com.ultimalabs.sancho.scheduler.runnables;

import java.util.concurrent.atomic.AtomicBoolean;

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
    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run() {

        running.set(true);
        log.info("Started ScheduledTasksCleanupTask on thread {}", Thread.currentThread().getName());

        while (running.get()) {
            schedulerService.getScheduledTasks().removeIf(task -> task.getFuture().isDone());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running.set(false);
                Thread.currentThread().interrupt();
            }
        }
    }

}
