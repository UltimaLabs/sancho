package com.ultimalabs.sancho.rotctldclient.util;

import com.ultimalabs.sancho.common.model.PassEventDataPoint;
import com.ultimalabs.sancho.common.model.SatellitePass;
import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.TrackingData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pass data to tracking data converter utility class
 */
@Slf4j
public class PassDataToTrackingDataConverter {

    private PassDataToTrackingDataConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a pass event data into tracking data format
     *
     * @param passData pass event data
     * @return tracking data
     */
    public static TrackingData convert(SatellitePass passData) {

        if (passData == null || passData.getEventDetails().isEmpty()) {
            return null;
        }

        List<PassEventDataPoint> passEventDetailsEntries = passData.getEventDetails();

        String satName = passData.getSatelliteData().getName();
        long trackingStart = passData.getRisePoint().getT().getTime() / 1000;
        long trackingEnd = passData.getSetPoint().getT().getTime() / 1000;
        Map<Long, AzimuthElevation> azElEntriesHashMap = new HashMap<>();
        boolean isFlipped = shouldFlip(passEventDetailsEntries);

        for (PassEventDataPoint entry : passEventDetailsEntries) {

            long timeStamp = entry.getT().getTime() / 1000;
            AzimuthElevation azEl = flipConversion(entry, isFlipped);

            azElEntriesHashMap.put(timeStamp, azEl);

        }

        AzimuthElevation riseAzEl = flipConversion(passData.getRisePoint(), isFlipped);
        AzimuthElevation setAzEl = flipConversion(passData.getSetPoint(), isFlipped);

        return new TrackingData(satName,
                trackingStart,
                trackingEnd,
                riseAzEl,
                setAzEl,
                AzimuthElevationUtil.normalizeAngle(passData.getMidPoint().getEl()),
                azElEntriesHashMap
        );

    }

    /**
     * Should the tracking data be flipped?
     * <p>
     * Azimuth/elevation needs to be flipped if the satellite passes
     * through azimuth 0, either west or east.
     *
     * @param passEventDetailsEntries list of pass event detail entries
     * @return true if there's a pass through azimuth 0
     */
    private static boolean shouldFlip(List<PassEventDataPoint> passEventDetailsEntries) {

        int oldAzimuth = getAzimuth(passEventDetailsEntries.get(0));
        int newAzimuth;

        for (PassEventDataPoint entry : passEventDetailsEntries) {

            newAzimuth = getAzimuth(entry);

            if (Math.abs(newAzimuth - oldAzimuth) > 300) {
                log.info("Flipped pass.");
                return true;
            }

            oldAzimuth = newAzimuth;

        }

        return false;

    }

    /**
     * Extracts normalized azimuth from a pass event detail entry
     *
     * @param entry pass event detail entry
     * @return normalized azimuth
     */
    private static int getAzimuth(PassEventDataPoint entry) {
        return AzimuthElevationUtil.normalizeAngle((int) Math.round(entry.getAz()));
    }

    private static AzimuthElevation flipConversion(PassEventDataPoint dataPoint, boolean flip) {

        if (flip) {
            log.info("Flip: ({}, {}) -> ({}, {})",
                    dataPoint.getAz(),
                    dataPoint.getEl(),
                    dataPoint.getAz() + 180,
                    180 - dataPoint.getEl()
            );
            return new AzimuthElevation(dataPoint.getAz() + 180, 180 - dataPoint.getEl());
        }

        return new AzimuthElevation(dataPoint.getAz(), dataPoint.getEl());

    }

}
