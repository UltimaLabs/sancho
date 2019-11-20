package com.ultimalabs.sattrackclient.common.config;

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
     * How long do we wait after issuing parking command (seconds)
     */
    private final int waitAfterParkingCommand;

    /**
     * Sleep duration between two "set position" tracking commands (seconds)
     */
    private final double stepSize;
}
