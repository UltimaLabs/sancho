package com.ultimalabs.sancho.api.config.service;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ultimalabs.sancho.api.config.model.ConfigUpdateResponse;
import com.ultimalabs.sancho.api.config.model.ConfigUpdateStatus;

import com.ultimalabs.sancho.common.config.SanchoConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private SanchoConfig sanchoConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigUpdateResponse updateConfig(SanchoConfig newSanchoConfig) {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            mapper.writeValue(new File("sancho.yml.upd"), newSanchoConfig);
        } catch (IOException e) {
            log.error("There was an error saving application config: {}", e.getMessage());
            return new ConfigUpdateResponse(ConfigUpdateStatus.ERROR, e.getMessage());
        }

        this.sanchoConfig = newSanchoConfig;
        
        // TODO restart scheduler
        
        log.info("Config file updated via API");
        return new ConfigUpdateResponse(ConfigUpdateStatus.OK, "");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SanchoConfig getSanchoConfig() {
        return sanchoConfig;
    }

}
