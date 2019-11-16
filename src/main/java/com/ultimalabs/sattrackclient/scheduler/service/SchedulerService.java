package com.ultimalabs.sattrackclient.scheduler.service;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.model.PassEventData;
import com.ultimalabs.sattrackclient.predictclient.service.PredictClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Fetches next pass data and schedules its tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
class SchedulerService {

    /**
     * Config object
     */
    private final SatTrackClientConfig config;

    /**
     * Predict client service
     */
    private final PredictClientService predictClientService;

    /**
     * Reference to a task scheduler
     */
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Schedules tracking of the next pass
     */
    @PostConstruct
    private void scheduleNextEvent() { // NOSONAR

        PassEventData nextPass = predictClientService.getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass: " + nextPass.toString());
            scheduleTracking(nextPass);
        }

    }

    /**
     * Schedules tracking and the next pass data fetch
     * <p>
     * Tracking starts at the rise time of the next pass.
     * At that event's set time next pass data is fetched.
     *
     * @param passData next pass data
     */
    private void scheduleTracking(PassEventData passData) {

        Date trackerDate = passData.getRise();
        Date fetcherDate = passData.getSet();

        taskScheduler.schedule(new TrackerTask(passData, this), trackerDate);
        log.info("Scheduled tracker for " + passData.getSatelliteData().getName() + " from " + passData.getRise() + " to " + passData.getSet());

        taskScheduler.schedule(new FetcherTask(passData,this), fetcherDate);
        log.info("Scheduled fetcher at " + fetcherDate);

    }

    /**
     * Implementation of the runnable interface for tracking threads
     */
    @Slf4j
    @RequiredArgsConstructor
    static class TrackerTask implements Runnable {

        private final PassEventData passData;
        private final SchedulerService schedulerService;

        @Override
        public void run() {

            log.info("Started TrackerTask with " + passData.toString() + " on thread " + Thread.currentThread().getName());

            String riseShellCmdSubstituted = passData.getSatelliteData().getSatRiseShellCmdSubstituted();

            if (!riseShellCmdSubstituted.equals("")) {
                log.info("Would execute rise-time shell command: " + riseShellCmdSubstituted);
            }

        }
    }

    /**
     * Implementation of the runnable interface for the pass data fetching threads
     */
    @Slf4j
    @RequiredArgsConstructor
    static class FetcherTask implements Runnable {

        private final PassEventData passData;
        private final SchedulerService schedulerService;

        @Override
        public void run() {
            log.info("Started FetcherTask on thread " + Thread.currentThread().getName());

            String setShellCmdSubstituted = passData.getSatelliteData().getSatSetShellCmdSubstituted();

            if (!setShellCmdSubstituted.equals("")) {
                log.info("Would execute set-time shell command: " + setShellCmdSubstituted);
            }

            schedulerService.scheduleNextEvent();
        }
    }

}
