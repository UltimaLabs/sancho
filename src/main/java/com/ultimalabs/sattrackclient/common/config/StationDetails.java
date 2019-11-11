package com.ultimalabs.sattrackclient.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Station details
 */
@Data
@Validated
public class StationDetails {

    /**
     * Station name
     */
    @NotBlank(message = "Station name should not be empty")
    private final String name;

    /**
     * Station latitude, in degrees
     */
    @Min(value = -90, message = "Latitude should not be less than -90")
    @Max(value = 90, message = "Latitude should not be greater than 90")
    private final double latitude;

    /**
     * Station longitude, in degrees
     */
    @Min(value = -90, message = "Longitude should not be less than -90")
    @Max(value = 90, message = "Longitude should not be greater than 90")
    private final double longitude;

    /**
     * Station altitude, in meters
     */
    @Min(value = 0, message = "Altitude should be greater or equal to zero")
    @Max(value = Integer.MAX_VALUE, message = "Altitude value is too large")
    private final int altitude;

}
