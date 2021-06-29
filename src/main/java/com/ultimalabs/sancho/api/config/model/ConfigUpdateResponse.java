package com.ultimalabs.sancho.api.config.model;

import lombok.Data;

@Data
public class ConfigUpdateResponse {

    private final ConfigUpdateStatus status;
    private final String comment;
    
}
