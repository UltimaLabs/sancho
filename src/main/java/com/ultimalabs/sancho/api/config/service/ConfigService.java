package com.ultimalabs.sancho.api.config.service;

import com.ultimalabs.sancho.api.config.model.ConfigUpdateResponse;
import com.ultimalabs.sancho.common.config.SanchoConfig;

/**
 * Sancho config management via API
 */
public interface ConfigService {

    /**
     * Get the current Sancho config
     * 
     * @return current config
     */
    SanchoConfig getSanchoConfig();

    /**
     * Update Sancho config
     * 
     * @param config new Sancho config
     * @return config update status
     */
    ConfigUpdateResponse updateConfig(SanchoConfig config);

}