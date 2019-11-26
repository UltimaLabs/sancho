package com.ultimalabs.sancho.scheduler.runnables;

import com.ultimalabs.sancho.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Next pass fetcher
 */
@Slf4j
@RequiredArgsConstructor
public class FetcherTask implements Runnable {

    private final SchedulerService schedulerService;

    @Override
    public void run() {
        log.info("Started FetcherTask on thread {}", Thread.currentThread().getName());
        schedulerService.scheduleNextEvent();
    }
}
