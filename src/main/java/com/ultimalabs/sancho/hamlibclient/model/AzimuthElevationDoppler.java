package com.ultimalabs.sancho.hamlibclient.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Stores azimuth/elevation data pairs
 *
 * The angles are normalized in 0 to 360 range,
 * with 360 not included.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper=true)
public class AzimuthElevationDoppler extends AzimuthElevation{

    private final double dopplerShift;

    public AzimuthElevationDoppler(int az, int el, double dop) {
        super(az, el);
        this.dopplerShift = dop;
    }

    public AzimuthElevationDoppler(double az, double el, double dop) {
        super(az, el);
        this.dopplerShift = dop;
    }

}
