package com.ultimalabs.sattrackclient.rotctldclient.model;

import lombok.Data;

/**
 * Stores azimuth/elevation data pairs
 */
@Data
public class AzimuthElevation {

    /**
     * Azimuth
     */
    private final int azimuth;

    /**
     * Elevation
     */
    private final int elevation;

    /**
     * Constructor
     *
     * @param az azimuth
     * @param el elevation
     */
    public AzimuthElevation(int az, int el) {
        this.azimuth = az;
        this.elevation = el;
    }

    /**
     * Constructor for arguments of the type double
     *
     * @param az azimuth
     * @param el elevation
     */
    public AzimuthElevation(double az, double el) {
        this.azimuth = (int) Math.round(az);
        this.elevation = (int) Math.round(el);
    }

}
