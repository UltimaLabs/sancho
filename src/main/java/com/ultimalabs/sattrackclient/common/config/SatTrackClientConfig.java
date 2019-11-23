package com.ultimalabs.sattrackclient.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Configuration loaded from appplication.yml
 */
@Data
@Validated
@ConstructorBinding
@ConfigurationProperties("sattrackclient")
public class SatTrackClientConfig {

    /**
     * Enable/disable automatic scheduling
     */
    @NotNull(message = "schedulerAutoStartDisabled should not be empty")
    private final boolean schedulerAutoStartDisabled;

    /**
     * How long do we wait until retry after scheduler error (seconds)
     */
    @Min(value = 1, message = "schedulerErrorWait should not be less than 1")
    private final int schedulerErrorWait;

    /**
     * SatTrackAPI base URL
     */
    @NotBlank(message = "SatTrackAPI base URL should not be empty")
    private final String satTrackApiUrl;

    /**
     * Station details
     */
    @NotNull(message = "Station details should not be empty")
    private final StationDetails station;

    /**
     * Rotator config
     */
    @NotNull(message = "Rotator config should not be empty")
    private final RotatorConfig rotator;

    /**
     * List of satellites we're tracking
     */
    @NotNull(message = "Satellite details should not be empty")
    private final List<SatelliteData> satelliteData;

}
