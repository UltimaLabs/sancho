package com.ultimalabs.sancho.scheduler.service;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.common.model.SatellitePass;
import com.ultimalabs.sancho.predictclient.service.PredictClientService;
import com.ultimalabs.sancho.rotctldclient.model.TrackingData;
import com.ultimalabs.sancho.rotctldclient.service.RotctldClientService;
import com.ultimalabs.sancho.rotctldclient.util.PassDataToTrackingDataConverter;
import com.ultimalabs.sancho.scheduler.runnables.FetcherTask;
import com.ultimalabs.sancho.scheduler.runnables.ShellCmdTask;
import com.ultimalabs.sancho.scheduler.runnables.TrackerTask;
import com.ultimalabs.sancho.shellexec.service.ShellExecService;
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
    private final SanchoConfig config;

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

        SatellitePass nextPass = predictClientService.getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass: {}.", nextPass.getSatelliteData().getName());
            scheduleOk = scheduleTracking(nextPass);
        }

        if (scheduleOk) {
            nextFetch = nextPass.getSetPoint().getT();
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
    private boolean scheduleTracking(SatellitePass passData) {

        Date trackerDate = passData.getRisePoint().getT();
        Date fetcherDate = passData.getSetPoint().getT();
        double maxElevation = passData.getMidPoint().getEl();
        double trackingElevationThreshold = passData.getSatelliteData().getTrackingElevationThreshold();
        boolean rotatorEnabled = passData.getSatelliteData().isRotatorEnabled();
        double stepSize = passData.getSatelliteData().getStepSize();
        String riseShellCmdSubstituted = passData.getSatelliteData().getSatRiseShellCmdSubstituted();
        String setShellCmdSubstituted = passData.getSatelliteData().getSatSetShellCmdSubstituted();

        // skip tracking and command execution if the maximum elevation
        // is below the tracking threshold
        if (maxElevation < trackingElevationThreshold) {
            log.info("Skipped tracking: maximum elevation ({}) below the tracking threshold ({}).", maxElevation, trackingElevationThreshold);
            return true;
        }

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
                        passData.getSatelliteData().getName(), trackerDate, passData.getSetPoint().getT());
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
