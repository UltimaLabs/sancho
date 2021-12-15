package com.ultimalabs.sancho.api.status.service;

import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.RadioParams;

import java.util.List;

/**
 * Display Sancho status
 */
public interface StatusService {

    /**
     * Get a list of currently scheduled tasks
     * 
     * @return list of scheduled tasks
     */
    List<String> getScheduledTasks();

    /**
     * Get current rotator position
     *
     * @return current rotator position
     */
    AzimuthElevation getRotatorPosition();

    /**
     * Get current radio parameters
     *
     * @return current radio parameters
     */
    RadioParams getRadioParams();

}
