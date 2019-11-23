package com.ultimalabs.sattrackclient.scheduler.service;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.model.PassEventData;
import com.ultimalabs.sattrackclient.predictclient.service.PredictClientService;
import com.ultimalabs.sattrackclient.rotctldclient.model.TrackingData;
import com.ultimalabs.sattrackclient.rotctldclient.service.RotctldClientService;
import com.ultimalabs.sattrackclient.rotctldclient.util.PassDataToTrackingDataConverter;
import com.ultimalabs.sattrackclient.scheduler.runnables.FetcherTask;
import com.ultimalabs.sattrackclient.scheduler.runnables.ShellCmdTask;
import com.ultimalabs.sattrackclient.scheduler.runnables.TrackerTask;
import com.ultimalabs.sattrackclient.shellexec.service.ShellExecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;

/**
 * Fetches next pass data and schedules its tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

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
     * Scheduling autostart
     */
    @PostConstruct
    public void autoStartScheduler() {

        if (config.isSchedulerAutoStartDisabled()) {
            return;
        }

        scheduleNextEvent();

    }

    /**
     * Schedules handling of the next event
     */
    public void scheduleNextEvent() {

        Date nextFetch;
        boolean scheduleOk = false;

        PassEventData nextPass = predictClientService.getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass: {}.", nextPass.getSatelliteData().getName());
            scheduleOk = scheduleTracking(nextPass);
        }

        if (scheduleOk) {
            nextFetch = nextPass.getSet();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, config.getSchedulerErrorWait());
            nextFetch = calendar.getTime();
        }

        // schedule fetching of next pass
        taskScheduler.schedule(new FetcherTask(this), nextFetch);
        log.info("Scheduled fetcher: {}", nextFetch);

    }

    /**
     * Schedules tracking
     * <p>
     * Tracking starts at the next pass rise time.
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

        return true;

    }


}
