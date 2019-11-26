package com.ultimalabs.sancho.rotctldclient.model;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Stores pass event data used for tracking
 */
@Data
public class TrackingData {

    /**
     * Satellite name
     */
    private final String satName;

    /**
     * Timestamp (epoch, seconds) of tracking start
     */
    private final long trackingStart;

    /**
     * Timestamp (epoch, seconds) of tracking end
     */
    private final long trackingEnd;

    /**
     * Rise azimuth and elevation
     */
    private final AzimuthElevation riseAzimuthElevation;

    /**
     * Set azimuth and elevation
     */
    private final AzimuthElevation setAzimuthElevation;

    /**
     * Maximum elevation, non-flipped
     */
    private final int maxElevation;

    /**
     * Map of maximum/elevation entries
     */
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
