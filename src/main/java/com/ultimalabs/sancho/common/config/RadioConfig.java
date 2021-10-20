package com.ultimalabs.sancho.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Radio transceiver control config
 */
@Data
@Validated
public class RadioConfig {

    /**
     * Rigctld host address
     *
     * Use empty string if rigctld is not used
     */
    @NotNull
    private final String rigctldHost;

    /**
     * Rigctld port
     *
     * Use 0 if rigctld is not used
     */
    @Min(value = 0, message = "Invalid rigctldPort - minimum value is 0")
    @Max(value = 65535, message = "Invalid rigctldPort - maximum value is 65535")
    private final int rigctldPort;

    /**
     * Constructor used for deserialization from JSON to RadioConfig object
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RadioConfig(
            @JsonProperty("rigctldHost") String rigctldHost, 
            @JsonProperty("rigctldPort") int rigctldPort
        ) {

        this.rigctldHost = rigctldHost;
        this.rigctldPort = rigctldPort;
    }
}
