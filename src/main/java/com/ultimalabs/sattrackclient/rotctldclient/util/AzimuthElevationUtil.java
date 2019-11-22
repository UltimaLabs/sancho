package com.ultimalabs.sattrackclient.rotctldclient.util;

/**
 * Azimuth/elevation utility class
 */
public class AzimuthElevationUtil {

    private AzimuthElevationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Normalizes an angle to an absolute angle.
     * The normalized angle will be in the range from 0 to 360, where 360
     * itself is not included.
     *
     * @param angle the angle to normalize
     * @return the normalized angle that will be in the range of [0,360]
     */
    public static int normalizeAngle(int angle) {
        angle %= 360;
        return angle >= 0 ? angle : (angle + 360);
    }

    /**
     * Normalizes an angle to an absolute angle.
     * The normalized angle will be in the range from 0 to 360, where 360
     * itself is not included.
     *
     * @param angle the angle to normalize
     * @return the normalized angle that will be in the range of [0,360]
     */
    public static int normalizeAngle(double angle) {
        int intAngle = (int) Math.round(angle);
        intAngle %= 360;
        return intAngle >= 0 ? intAngle : (intAngle + 360);
    }

}
