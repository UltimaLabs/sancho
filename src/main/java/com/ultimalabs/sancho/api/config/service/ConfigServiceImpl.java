package com.ultimalabs.sancho.api.config.service;

import com.ultimalabs.sancho.api.config.model.ConfigUpdateResponse;
import com.ultimalabs.sancho.api.config.model.ConfigUpdateStatus;
import com.ultimalabs.sancho.common.config.SanchoConfig;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {
    
    public ConfigUpdateResponse updateConfig(SanchoConfig config) {
        log.info("Received config update: " + config.toString());
        return new ConfigUpdateResponse(ConfigUpdateStatus.OK, "Sve 5.");
    }

}
