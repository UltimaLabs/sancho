package com.ultimalabs.sancho.rotctldclient.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzimuthElevationUtilTest {

    @DisplayName("Test normalizeAngle()")
    @Test
    void normalizeAngle() {

        assertEquals(180, AzimuthElevationUtil.normalizeAngle(180));
        assertEquals(44, AzimuthElevationUtil.normalizeAngle(404));
        assertEquals(180, AzimuthElevationUtil.normalizeAngle(-180));
        assertEquals(0, AzimuthElevationUtil.normalizeAngle(360));

        assertEquals(0, AzimuthElevationUtil.normalizeAngle(0.4));
        assertEquals(21, AzimuthElevationUtil.normalizeAngle(20.51));
        assertEquals(340, AzimuthElevationUtil.normalizeAngle(-19.7));
        assertEquals(180, AzimuthElevationUtil.normalizeAngle(-540.0));

    }

}