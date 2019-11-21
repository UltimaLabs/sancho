package com.ultimalabs.sattrackclient.rotctldclient.model;

import lombok.Data;

/**
 * Stores azimuth/elevation data pairs
 *
 * The angles are normalized in 0 to 360 range,
 * with 360 not included.
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
        this.azimuth = normalizeAngle(az);
        this.elevation = normalizeAngle(el);
    }

    /**
     * Constructor for arguments of the type double
     *
     * @param az azimuth
     * @param el elevation
     */
    public AzimuthElevation(double az, double el) {
        this.azimuth = normalizeAngle((int) Math.round(az));
        this.elevation = normalizeAngle((int) Math.round(el));
    }

    /**
     * Normalizes an angle to an absolute angle.
     * The normalized angle will be in the range from 0 to 360, where 360
     * itself is not included.
     *
     * @param angle the angle to normalize
     * @return the normalized angle that will be in the range of [0,360]
     */
    private int normalizeAngle(int angle) {

        angle %= 360;
        return angle >= 0 ? angle : (angle + 360);
    }

}
