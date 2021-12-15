package com.ultimalabs.sancho.api.status.service;

import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.RadioParams;
import com.ultimalabs.sancho.rotctldclient.service.HamlibClientService;
import com.ultimalabs.sancho.scheduler.model.ScheduledTaskDetails;
import com.ultimalabs.sancho.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatusServiceImpl implements StatusService {

    private final SchedulerService schedulerService;
    private final HamlibClientService hamlibClientService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getScheduledTasks() {
        List<String> listOfTasks = new ArrayList<>();

        for (ScheduledTaskDetails taskDetails : schedulerService.getScheduledTasks()) {
            listOfTasks.add(taskDetails.getStartTime().toString() + " - " + taskDetails.getDescription());
        }

        return listOfTasks;

    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public AzimuthElevation getRotatorPosition() {
        return hamlibClientService.getAzEl();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public RadioParams getRadioParams() {
        return hamlibClientService.getRadioParams();
    }
}
