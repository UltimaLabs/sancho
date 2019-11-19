package com.ultimalabs.sattrackclient.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

/**
 * Satellite details
 */
@Data
@Validated
public class SatelliteData {

    /**
     * Satellite id
     */
    @Size(min = 5, max = 11, message = "Satellite identifier must be between 5 and 11 characters long")
    private final String id;

    /**
     * Satellite name
     */
    @NotBlank(message = "Satellite name should not be blank")
    private final String name;

    /**
     * Satellite transponder's radio frequency
     */
    private final double radioFrequency;

    /**
     * Elevation threshold for passes
     */
    @Min(value = 0, message = "Elevation should not be less than 0")
    @Max(value = 90, message = "Elevation should not be greater than 90")
    private final double minElevation;

    /**
     * Tracking step size (duration n seconds), for use with rotators
     * <p>
     * Use zero to fetch only the basic pass data, without azimuth/elevation details.
     */
    private final double stepSize;

    /**
     * Whether the rotator is used with this satellite
     */
    @NotNull(message = "Please specify useRotator as a boolean value")
    private final boolean rotatorEnabled;

    /**
     * Template for the shell command executed at satellite rise time
     */
    @NotNull(message = "Satellite rise shell command template must be specified (it can be an empty string)")
    private final String satRiseShellCmdTemplate;

    /**
     * Template for the shell command executed at satellite set time
     */
    @NotNull(message = "Satellite set shell command template must be specified (it can be an empty string)")
    private final String satSetShellCmdTemplate;

    /**
     * Substituted shell command executed at satellite rise time
     */
    private String satRiseShellCmdSubstituted;

    /**
     * Substituted shell command executed at satellite set time
     */
    private String satSetShellCmdSubstituted;

}
