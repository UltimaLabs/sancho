package com.ultimalabs.sancho.hamlibclient.util;

import com.ultimalabs.sancho.common.model.PassEventDataPoint;
import com.ultimalabs.sancho.common.model.SatellitePass;
import com.ultimalabs.sancho.hamlibclient.model.AzimuthElevation;
import com.ultimalabs.sancho.hamlibclient.model.AzimuthElevationDoppler;
import com.ultimalabs.sancho.hamlibclient.model.TrackingData;
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
        Map<Long, AzimuthElevationDoppler> azElDopEntriesHashMap = new HashMap<>();
        boolean isFlipped = shouldFlip(passEventDetailsEntries);

        for (PassEventDataPoint entry : passEventDetailsEntries) {

            long timeStamp = entry.getT().getTime() / 1000;
            AzimuthElevationDoppler azEl = flipConversion(entry, isFlipped);

            azElDopEntriesHashMap.put(timeStamp, azEl);

        }

        AzimuthElevation riseAzEl = flipConversion(passData.getRisePoint(), isFlipped);
        AzimuthElevation setAzEl = flipConversion(passData.getSetPoint(), isFlipped);

        return new TrackingData(satName,
                trackingStart,
                trackingEnd,
                riseAzEl,
                setAzEl,
                AzimuthElevationUtil.normalizeAngle(passData.getMidPoint().getEl()),
                azElDopEntriesHashMap
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

    /**
     * Azimuth/elevation flip
     * <p>
     * If the flip is enabled, rotates azimuth by 180 degrees and sets the elevation
     * at 180 - original elevation.
     *
     * @param dataPoint pass event data point
     * @param flip      should we perform the flip
     * @return azimuth/elevation object converted from pass event data point
     */
    private static AzimuthElevationDoppler flipConversion(PassEventDataPoint dataPoint, boolean flip) {

        if (flip) {
            return new AzimuthElevationDoppler(dataPoint.getAz() + 180, 180 - dataPoint.getEl(), dataPoint.getDop());
        }

        return new AzimuthElevationDoppler(dataPoint.getAz(), dataPoint.getEl(), dataPoint.getDop());

    }

}
