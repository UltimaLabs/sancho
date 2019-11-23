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
     * Sleep duration between two "set position" tracking commands (seconds)
     */
    private final double stepSize;

}
