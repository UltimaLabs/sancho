package com.ultimalabs.sattrackclient.rotctldclient.model;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Stores pass event data used for tracking
 */
@Data
public class TrackingData {

    private final long trackingStart;
    private final long trackingEnd;
    private final AzimuthElevation riseAzimuthElevation;
    private final AzimuthElevation setAzimuthElevation;
    private final Map<Long, AzimuthElevation> azimuthElevationMap;

    /**
     * Returns azimuth/elevation at the current timestamp
     * <p>
     * Timestamp is calculated as Unix epoch in seconds.
     *
     * @return azimuth/elevation
     */
    public AzimuthElevation getCurrentAzimuthElevation() {
        return azimuthElevationMap.get(Instant.now().toEpochMilli() / 1000);
    }

}
