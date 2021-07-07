package com.ultimalabs.sancho.api.status.service;

import java.util.ArrayList;
import java.util.List;

import com.ultimalabs.sancho.scheduler.model.ScheduledTaskDetails;
import com.ultimalabs.sancho.scheduler.service.SchedulerService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StatusServiceImpl implements StatusService {

    private final SchedulerService schedulerService;

    @Override
    public List<String> getScheduledTasks() 
    {
        List<String> listOfTasks = new ArrayList<>();

        for (ScheduledTaskDetails taskDetails : schedulerService.getTaskList()) {
            listOfTasks.add(taskDetails.getStartTime().toString() + " - " + taskDetails.getDescription());
        }

        return listOfTasks;
        
    }    
}
