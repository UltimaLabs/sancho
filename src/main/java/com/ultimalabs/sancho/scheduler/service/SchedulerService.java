package com.ultimalabs.sancho.scheduler.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.common.model.SatellitePass;
import com.ultimalabs.sancho.predictclient.service.PredictClientService;
import com.ultimalabs.sancho.hamlibclient.model.TrackingData;
import com.ultimalabs.sancho.hamlibclient.service.HamlibClientService;
import com.ultimalabs.sancho.hamlibclient.util.PassDataToTrackingDataConverter;
import com.ultimalabs.sancho.scheduler.model.ScheduledTaskDetails;
import com.ultimalabs.sancho.scheduler.runnables.FetcherTask;
import com.ultimalabs.sancho.scheduler.runnables.ScheduledTasksCleanupTask;
import com.ultimalabs.sancho.scheduler.runnables.ShellCmdTask;
import com.ultimalabs.sancho.scheduler.runnables.ShutdownTask;
import com.ultimalabs.sancho.scheduler.runnables.TrackerTask;
import com.ultimalabs.sancho.shellexec.service.ShellExecService;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracking scheduler
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
    private final HamlibClientService hamlibClientService;

    /**
     * Reference to the task scheduler
     */
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * List of scheduled tasks
     */
    @Getter
    private List<ScheduledTaskDetails> scheduledTasks;

    /**
     * Scheduling autostart
     */
    @PostConstruct
    private void autoStartScheduler() {

        scheduledTasks = new ArrayList<>();
        taskScheduler.execute(new ScheduledTasksCleanupTask(this));

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
        scheduleTask(new FetcherTask(this), nextFetch, "Next pass fetcher");
        log.info("Scheduled fetcher: {}", nextFetch);

    }

    /**
     * Schedules tracking
     * 
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
            log.info("Skipped tracking: maximum elevation ({}) below the tracking threshold ({}).", maxElevation,
                    trackingElevationThreshold);
            return true;
        }

        // schedule tracking
        if (rotatorEnabled && stepSize != 0.0) {

            // convert the pass data into tracking format
            TrackingData trackingData = PassDataToTrackingDataConverter.convert(passData);

            // park the rotator in the starting position
            boolean parkOk = hamlibClientService.parkRotator(trackingData.getRiseAzimuthElevation());

            if (parkOk) {
                // schedule tracker task
                scheduleTask(new TrackerTask(hamlibClientService, trackingData, passData.getSatelliteData()), trackerDate,
                        "Tracking " + passData.getSatelliteData().getName() + " until " + fetcherDate);
                log.info("Scheduled tracking: {}, {} - {}", passData.getSatelliteData().getName(), trackerDate,
                        passData.getSetPoint().getT());
            } else {
                log.error("Tracking canceled due to parking error.");
                return false;
            }
        }

        // schedule rise-time shell cmd execution
        if (!riseShellCmdSubstituted.equals("")) {
            scheduleTask(new ShellCmdTask(riseShellCmdSubstituted, shellExecService), trackerDate,
                    "Rise-time command execution: " + riseShellCmdSubstituted);
            log.info("Scheduled rise-time cmd exec: {} at {}", riseShellCmdSubstituted, trackerDate);
        }

        // schedule set-time shell cmd execution
        if (!setShellCmdSubstituted.equals("")) {
            scheduleTask(new ShellCmdTask(setShellCmdSubstituted, shellExecService), fetcherDate,
                    "Set-time command execution: " + setShellCmdSubstituted);
            log.info("Scheduled set-time cmd exec: {} at {}", setShellCmdSubstituted, fetcherDate);
        }

        return true;

    }

    /**
     * Shutdown - normally after the config update
     */
    public void shutdown() {
        taskScheduler.execute(new ShutdownTask(this));
    }

    /**
     * Schedule a task for execution and add it to the task list
     * 
     * @param task        a task scheduled for execution
     * @param startTime   task start time
     * @param description task description
     */
    private void scheduleTask(Runnable task, Date startTime, String description) {
        ScheduledFuture<?> future = taskScheduler.schedule(task, startTime);
        ScheduledTaskDetails taskDetails = new ScheduledTaskDetails(future, description, startTime);
        scheduledTasks.add(taskDetails);
    }

}
