package com.ultimalabs.sancho.api.status.service;

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

}
