package com.ultimalabs.sancho.api.config.service;

import com.ultimalabs.sancho.api.config.model.ConfigUpdateResponse;
import com.ultimalabs.sancho.common.config.SanchoConfig;

public interface ConfigService {

    ConfigUpdateResponse updateConfig(SanchoConfig config);

}