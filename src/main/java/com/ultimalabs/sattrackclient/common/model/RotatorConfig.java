package com.ultimalabs.sattrackclient.common.model;

import lombok.Data;

/**
 * Rotator config
 */
@Data
public class RotatorConfig {

    /**
     * Rotctld host address
     */
    private final String rotctldHost;

    /**
     * Rotctld port
     */
    private final int rotctldPort;

    /**
     * How long do we wait until issuing parking command
     */
    private final int waitAfterParkingCommand;
}
