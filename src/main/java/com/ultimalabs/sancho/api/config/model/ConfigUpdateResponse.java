package com.ultimalabs.sancho.api.config.model;

import lombok.Data;

@Data
public class ConfigUpdateResponse {

    /**
     * Status of config update
     */
    public enum UpdateStatus {

        OK, ERROR

    }

    private final UpdateStatus status;
    private final String comment;

}
