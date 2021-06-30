package com.ultimalabs.sancho.api.config.model;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties.ForwardHeadersStrategy;

import lombok.Data;

@Data
public class ServerConfig {

    private final int port;
    private final ForwardHeadersStrategy forwardHeadersStrategy;
    private final ErrorProperties error;

}
