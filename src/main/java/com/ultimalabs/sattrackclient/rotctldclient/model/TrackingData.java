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
    private final int maxElevation;
    private final Map<Long, AzimuthElevation> azimuthElevationMap;

    /**
     * Returns azimuth/elevation at the current timestamp
     * <p>
     * Timestamp is calculated as Unix epoch in seconds.
     *
     * @return azimuth/elevation
     */
    public AzimuthElevation getCurrentAzimuthElevation() {
        return getAzimuthElevation(Instant.now().toEpochMilli() / 1000);
    }

    /**
     * Returns azimuth/elevation at a given timestamp
     *
     * @param timeStamp timestamp, in seconds
     * @return azimuth/elevation or null if no data exists for the given timestamp
     */
    public AzimuthElevation getAzimuthElevation(long timeStamp) {
        return azimuthElevationMap.get(timeStamp);
    }

}
