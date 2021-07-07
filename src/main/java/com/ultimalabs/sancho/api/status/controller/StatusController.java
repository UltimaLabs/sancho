package com.ultimalabs.sancho.api.status.controller;

import java.util.List;

import com.ultimalabs.sancho.api.status.service.StatusService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

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

}
