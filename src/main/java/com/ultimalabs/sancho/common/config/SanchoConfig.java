package com.ultimalabs.sancho.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration loaded from sancho.yml
 */
@Data
@Validated
@ConstructorBinding
@ConfigurationProperties
public class SanchoConfig {

    /**
     * Enable/disable automatic scheduling
     */
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
     * Radio transceiver control config
     */
    @NotNull(message = "Radio transceiver control config should not be empty")
    private final RadioConfig radio;

    /**
     * List of satellites we're tracking
     */
    @NotNull(message = "Satellite details should not be null")
    @NotEmpty(message = "Satellite details should not be empty")
    private final List<SatelliteData> satelliteData;

    /**
     * Constructor used for deserialization from JSON to SanchoConfig object
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SanchoConfig(
            @JsonProperty("schedulerAutoStartDisabled") boolean schedulerAutoStartDisabled, 
            @JsonProperty("schedulerErrorWait") int schedulerErrorWait, 
            @JsonProperty("satTrackApiUrl") String satTrackApiUrl,
            @JsonProperty("station") StationDetails station,
            @JsonProperty("rotator") RotatorConfig rotator,
            @JsonProperty("radio") RadioConfig radio,
            @JsonProperty("satelliteData") List<SatelliteData> satelliteData
        ) {

        this.schedulerAutoStartDisabled = schedulerAutoStartDisabled;
        this.schedulerErrorWait = schedulerErrorWait;
        this.satTrackApiUrl = satTrackApiUrl;
        this.station = station;
        this.rotator = rotator;
        this.radio = radio;
        this.satelliteData = satelliteData;
    }

}
