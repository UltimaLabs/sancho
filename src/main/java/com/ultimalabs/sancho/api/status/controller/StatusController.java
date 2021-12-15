package com.ultimalabs.sancho.api.status.controller;

import com.ultimalabs.sancho.api.status.service.StatusService;
import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.RadioParams;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Status REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private final StatusService statusService;

    /**
     * Get a list of scheduled tasks
     *
     * @return list of scheduled tasks
     */
    @GetMapping(value = "/scheduledTasks", produces = "application/json")
    public List<String> getScheduledTasks() {
        return statusService.getScheduledTasks();
    }

    /**
     * Get the current rotator position
     *
     * @return current rotator position
     */
    @GetMapping(value = "/rotatorPosition", produces = "application/json")
    public AzimuthElevation getRotatorPosition() {
        return statusService.getRotatorPosition();
    }

    /**
     * Get the current radio frequency, in Hz
     *
     * @return current radio frequency
     */
    @GetMapping(value = "/radioParams", produces = "application/json")
    public RadioParams getRadioParams() {
        return statusService.getRadioParams();
    }


}
