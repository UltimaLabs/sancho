package com.ultimalabs.sattrackclient.scheduler.service;

import com.ultimalabs.sattrackclient.common.model.PassEventData;
import com.ultimalabs.sattrackclient.predictclient.service.PredictClientService;
import com.ultimalabs.sattrackclient.rotctldclient.model.TrackingData;
import com.ultimalabs.sattrackclient.rotctldclient.service.RotctldClientService;
import com.ultimalabs.sattrackclient.rotctldclient.util.PassDataToTrackingDataConverter;
import com.ultimalabs.sattrackclient.shellexec.service.ShellExecService;
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
     * Predict client service
     */
    private final PredictClientService predictClientService;

    /**
     * Shell exec service
     */
    private final ShellExecService shellExecService;

    /**
     * Rotctld client service
     */
    private final RotctldClientService rotctldClientService;

    /**
     * Reference to a task scheduler
     */
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Schedules handling of the next event
     */
    @PostConstruct
    private void scheduleNextEvent() {

        boolean scheduleOk = false;
        PassEventData nextPass = predictClientService.getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass - {}.", nextPass.getSatelliteData().getName());
            scheduleOk = scheduleTracking(nextPass);
        }

        if (!scheduleOk) {
            // TODO schedule next fetch
        }

    }

    /**
     * Schedules tracking and the next pass data fetch
     * <p>
     * Tracking starts at the next pass rise time.
     * At that event's set time next pass data is fetched.
     *
     * @param passData pass data
     * @return true if tracking was scheduled successfully
     */
    private boolean scheduleTracking(PassEventData passData) {

        Date trackerDate = passData.getRise();
        Date fetcherDate = passData.getSet();
        boolean rotatorEnabled = passData.getSatelliteData().isRotatorEnabled();
        double stepSize = passData.getSatelliteData().getStepSize();
        String riseShellCmdSubstituted = passData.getSatelliteData().getSatRiseShellCmdSubstituted();
        String setShellCmdSubstituted = passData.getSatelliteData().getSatSetShellCmdSubstituted();

        // schedule tracking
        if (rotatorEnabled && stepSize != 0.0) {

            // convert the pass data into tracking format
            TrackingData trackingData = PassDataToTrackingDataConverter.convert(passData);

            // park the rotator in the starting position
            boolean parkOk = rotctldClientService.parkRotator(trackingData.getRiseAzimuthElevation());

            if (parkOk) {
                // schedule tracker task
                taskScheduler.schedule(new TrackerTask(rotctldClientService, trackingData), trackerDate);
                log.info("Scheduled tracking: {}, {} - {}",
                        passData.getSatelliteData().getName(), trackerDate, passData.getSet());
            } else {
                log.error("Tracking canceled due to parking error.");
                return false;
            }
        }

        // schedule rise-time shell cmd execution
        if (!riseShellCmdSubstituted.equals("")) {
            taskScheduler.schedule(new ShellCmdTask(riseShellCmdSubstituted, shellExecService), trackerDate);
            log.info("Scheduled rise-time cmd exec: {} at {}", riseShellCmdSubstituted, trackerDate);
        }

        // schedule set-time shell cmd execution
        if (!setShellCmdSubstituted.equals("")) {
            taskScheduler.schedule(new ShellCmdTask(setShellCmdSubstituted, shellExecService), fetcherDate);
            log.info("Scheduled set-time cmd exec: {} at {}", setShellCmdSubstituted, fetcherDate);
        }

        // schedule fetching of the next pass data
        taskScheduler.schedule(new FetcherTask(this), fetcherDate);
        log.info("Scheduled fetcher: {}", fetcherDate);

        return true;

    }

    /**
     * Shell command execution
     */
    @Slf4j
    @RequiredArgsConstructor
    static class ShellCmdTask implements Runnable {

        private final String shellCommand;
        private final ShellExecService shellExecService;

        @Override
        public void run() {

            log.info("Started ShellCmdTask on thread {}", Thread.currentThread().getName());

            if (!shellCommand.equals("")) {
                shellExecService.execShellCmd(shellCommand);
            }

        }
    }

    /**
     * Satellite tracking
     */
    @Slf4j
    @RequiredArgsConstructor
    static class TrackerTask implements Runnable {

        private final RotctldClientService rotctldClientService;
        private final TrackingData trackingData;

        @Override
        public void run() {

            log.info("Started TrackerTask on thread {}", Thread.currentThread().getName());
            rotctldClientService.track(trackingData);

        }
    }

    /**
     * Next pass fetcher
     */
    @Slf4j
    @RequiredArgsConstructor
    static class FetcherTask implements Runnable {

        private final SchedulerService schedulerService;

        @Override
        public void run() {
            log.info("Started FetcherTask on thread {}", Thread.currentThread().getName());
            schedulerService.scheduleNextEvent();
        }
    }

}
