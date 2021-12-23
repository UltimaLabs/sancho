package com.ultimalabs.sancho.hamlibclient.model;

import com.ultimalabs.sancho.hamlibclient.util.AzimuthElevationUtil;
import lombok.*;

/**
 * Stores azimuth/elevation data pairs
 *
 * The angles are normalized in 0 to 360 range,
 * with 360 not included.
 */
@Getter
@ToString
@EqualsAndHashCode
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
        this.azimuth = AzimuthElevationUtil.normalizeAngle(az);
        this.elevation = AzimuthElevationUtil.normalizeAngle(el);
    }

    /**
     * Constructor for arguments of the type double
     *
     * @param az azimuth
     * @param el elevation
     */
    public AzimuthElevation(double az, double el) {
        this.azimuth = AzimuthElevationUtil.normalizeAngle((int) Math.round(az));
        this.elevation = AzimuthElevationUtil.normalizeAngle((int) Math.round(el));
    }

}
