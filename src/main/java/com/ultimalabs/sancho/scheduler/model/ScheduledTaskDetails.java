package com.ultimalabs.sancho.scheduler.model;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import lombok.Data;

@Data
public class ScheduledTaskDetails {
    
    private final ScheduledFuture<?> future;
    private final String description;
    private final Date startTime;

}
