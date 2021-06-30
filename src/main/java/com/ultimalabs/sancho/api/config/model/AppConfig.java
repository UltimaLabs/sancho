package com.ultimalabs.sancho.api.config.model;

import com.ultimalabs.sancho.common.config.SanchoConfig;

import lombok.Data;

@Data
public class AppConfig {

    private final ServerConfig server;
    private final SanchoConfig sancho;
    
}
