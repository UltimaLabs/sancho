package com.ultimalabs.sancho.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Rotator config
 */
@Data
@Validated
public class RotatorConfig {

    /**
     * Rotctld host address
     *
     * Use empty string if rotctld is not used
     */
    @NotNull
    private final String rotctldHost;

    /**
     * Rotctld port
     *
     * Use 0 if rotctld is not used
     */
    @Min(value = 0, message = "Invalid rotctldPort - minimum value is 0")
    @Max(value = 65535, message = "Invalid rotctldPort - maximum value is 65535")
    private final int rotctldPort;

    /**
     * Sleep duration between two "set position" tracking commands (seconds)
     */
    @DecimalMin(value = "0.01", message = "Step size should not be less than 0.01")
    private final double stepSize;

    /**
     * Should the second half of passes through or near zenith be flipped
     */
    private final boolean halfFlipHighElPasses;

    /**
     * Minimum elevation for half-flip
     */
    @Min(value = 70, message = "Invalid halfFlipHighElPassesMinElevation - minimum value is 70")
    @Max(value = 90, message = "Invalid halfFlipHighElPassesMinElevation - maximum value is 90")
    private final int halfFlipHighElPassesMinElevation;

}
