package com.ultimalabs.sancho.api.config.controller;

import javax.validation.Valid;

import com.ultimalabs.sancho.api.config.model.ConfigUpdateResponse;
import com.ultimalabs.sancho.api.config.service.ConfigService;
import com.ultimalabs.sancho.common.config.SanchoConfig;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Config REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final ConfigService configService;

    /**
     * Update config
     * 
     * @param config new Sancho config
     * @return config update response
     */
    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ConfigUpdateResponse updateConfig(@Valid @RequestBody SanchoConfig config) {
        return configService.updateConfig(config);
    }

}
