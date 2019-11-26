package com.ultimalabs.sancho.rotctldclient.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzimuthElevationTest {

    @DisplayName("Test normalization")
    @Test
    void normalization()
    {
        AzimuthElevation newAzEl = new AzimuthElevation(-30, -180);
        AzimuthElevation newAzEl2 = new AzimuthElevation(720, 450);

        assertEquals(330, newAzEl.getAzimuth());
        assertEquals(180, newAzEl.getElevation());

        assertEquals(0, newAzEl2.getAzimuth());
        assertEquals(90, newAzEl2.getElevation());
    }

    @DisplayName("Test rounding")
    @Test
    void rounding()
    {
        AzimuthElevation newAzEl = new AzimuthElevation(-30.1, -180.51);
        AzimuthElevation newAzEl2 = new AzimuthElevation(723.49, 450.0002);

        assertEquals(330, newAzEl.getAzimuth());
        assertEquals(179, newAzEl.getElevation());

        assertEquals(3, newAzEl2.getAzimuth());
        assertEquals(90, newAzEl2.getElevation());
    }

}