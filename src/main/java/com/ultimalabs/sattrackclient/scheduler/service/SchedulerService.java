package com.ultimalabs.sattrackclient.scheduler.service;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.model.PassEventData;
import com.ultimalabs.sattrackclient.predictclient.service.PredictClientService;
import com.ultimalabs.sattrackclient.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sattrackclient.rotctldclient.service.RotctldClientService;
import com.ultimalabs.sattrackclient.rotctldclient.util.PassDataToAzElConverter;
import com.ultimalabs.sattrackclient.shellexec.service.ShellExecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

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

        PassEventData nextPass = predictClientService.getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass.");
            scheduleTracking(nextPass);
        }

    }

    /**
     * Schedules tracking and the next pass data fetch
     * <p>
     * Tracking starts at the next pass rise time.
     * At that event's set time next pass data is fetched.
     *
     * @param passData pass data
     */
    private void scheduleTracking(PassEventData passData) {

        Date trackerDate = passData.getRise();
        Date fetcherDate = passData.getSet();
        boolean rotatorEnabled = passData.getSatelliteData().isRotatorEnabled();
        double stepSize = passData.getSatelliteData().getStepSize();
        String riseShellCmdSubstituted = passData.getSatelliteData().getSatRiseShellCmdSubstituted();

        // schedule tracking
        if (rotatorEnabled && stepSize != 0.0) {

            // convert the azimuth/elevation list
            List<AzimuthElevation> azimuthElevationList = PassDataToAzElConverter.convert(passData.getEventDetails());

            // park the rotator in the starting position
            boolean parkOk = rotctldClientService.parkRotator(azimuthElevationList.get(0));

            if (!parkOk) {
                log.error("Tracking canceled due to parking error.");
                return;
            }

            // schedule the tracking
            int stepSizeInt = (int) Math.round(passData.getSatelliteData().getStepSize() * 1000);
            taskScheduler.schedule(new TrackerTask(rotctldClientService, azimuthElevationList, stepSizeInt),
                    trackerDate);

            log.info("Scheduled tracker: {}, {} - {}",
                    passData.getSatelliteData().getName(), trackerDate, passData.getSet());
        }

        // schedule rise-time shell cmd execution
        if (!riseShellCmdSubstituted.equals("")) {
            taskScheduler.schedule(new RiseShellCmdTask(riseShellCmdSubstituted, shellExecService), trackerDate);
        }

        // schedule fetching of the next pass data and (possibly) execute set-time shell cmd
        taskScheduler.schedule(new FetcherTask(passData, this, shellExecService), fetcherDate);
        log.info("Scheduled fetcher: {}", fetcherDate);

    }

    /**
     * Rise shell cmd execution
     */
    @Slf4j
    @RequiredArgsConstructor
    static class RiseShellCmdTask implements Runnable {

        private final String riseShellCmdSubstituted;
        private final ShellExecService shellExecService;

        @Override
        public void run() {

            log.info("Started RiseShellCmdTask on thread {}", Thread.currentThread().getName());

            if (!riseShellCmdSubstituted.equals("")) {
                shellExecService.execShellCmd(riseShellCmdSubstituted);
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
        private final List<AzimuthElevation> azimuthElevationList;
        private final int sleepDuration;

        @Override
        public void run() {

            log.info("Started TrackerTask on thread {}", Thread.currentThread().getName());
            rotctldClientService.track(azimuthElevationList, sleepDuration);

        }
    }

    /**
     * Next pass fetcher, set-time shell cmd execution
     */
    @Slf4j
    @RequiredArgsConstructor
    static class FetcherTask implements Runnable {

        private final PassEventData passData;
        private final SchedulerService schedulerService;
        private final ShellExecService shellExecService;

        @Override
        public void run() {

            log.info("Started FetcherTask on thread {}", Thread.currentThread().getName());

            String setShellCmdSubstituted = passData.getSatelliteData().getSatSetShellCmdSubstituted();

            if (!setShellCmdSubstituted.equals("")) {
                shellExecService.execShellCmd(setShellCmdSubstituted);
            }

            schedulerService.scheduleNextEvent();
        }
    }

}
