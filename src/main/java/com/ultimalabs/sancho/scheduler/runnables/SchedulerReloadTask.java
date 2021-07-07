package com.ultimalabs.sancho.scheduler.runnables;

import com.ultimalabs.sancho.scheduler.service.SchedulerService;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reloads the task scheduler
 */
@Slf4j
@RequiredArgsConstructor
public class SchedulerReloadTask implements Runnable {

    /**
     * Reference to the task scheduler
     */
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Reference to the traking scheduler service
     */
    private final SchedulerService schedulerService;

    @Override
    public void run() {
        // TODO implement scheduler reload
        log.info("Started SchedulerReloadTask on thread {}", Thread.currentThread().getName());
    }
}
