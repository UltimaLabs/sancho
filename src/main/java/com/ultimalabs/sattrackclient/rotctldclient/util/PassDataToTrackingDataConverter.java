package com.ultimalabs.sattrackclient.rotctldclient.util;

import com.ultimalabs.sattrackclient.common.model.PassEventDataPoint;
import com.ultimalabs.sattrackclient.common.model.SatellitePass;
import com.ultimalabs.sattrackclient.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sattrackclient.rotctldclient.model.TrackingData;
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
    public static TrackingData convert(SatellitePass passData, boolean halfFlipHighElPasses, int halfFlipHighElPassesMinElevation) {

        if (passData == null || passData.getEventDetails().isEmpty()) {
            return null;
        }

        List<PassEventDataPoint> passEventDetailsEntries = passData.getEventDetails();

        String satName = passData.getSatelliteData().getName();
        long trackingStart = passData.getRisePoint().getT().getTime() / 1000;
        long midPointTime = passData.getMidPoint().getT().getTime() / 1000;
        int midPointAzimuth = AzimuthElevationUtil.normalizeAngle(passData.getMidPoint().getAz());
        int midPointElevation = AzimuthElevationUtil.normalizeAngle(passData.getMidPoint().getEl());
        long trackingEnd = passData.getSetPoint().getT().getTime() / 1000;
        Map<Long, AzimuthElevation> azElEntriesHashMap = new HashMap<>();
        boolean isFlipped = shouldFlip(passEventDetailsEntries);
        boolean halfFlip = halfFlipHighElPasses && midPointElevation >= halfFlipHighElPassesMinElevation;

        for (PassEventDataPoint entry : passEventDetailsEntries) {

            long timeStamp = entry.getT().getTime() / 1000;
            AzimuthElevation azEl = flipConversion(entry, isFlipped);

            if (halfFlip && timeStamp >= midPointTime) {
                // azEl = halfFlipConversion(azEl, midPointAzimuth, midPointElevation);
            }

            azElEntriesHashMap.put(timeStamp, azEl);

        }

        AzimuthElevation riseAzEl = flipConversion(passData.getRisePoint(), isFlipped);
        AzimuthElevation setAzEl = flipConversion(passData.getSetPoint(), isFlipped);

        if (halfFlip) {
            // setAzEl = halfFlipConversion(setAzEl, midPointAzimuth, midPointElevation);
        }

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

            if ((newAzimuth == 0 && oldAzimuth == 359) || (newAzimuth == 359 && oldAzimuth == 0)) {
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

    private static AzimuthElevation halfFlipConversion(AzimuthElevation oldAzEl, int midpointAz, int midpointEl) {

        int oldAz = oldAzEl.getAzimuth();
        int oldEl = oldAzEl.getElevation();

        int newAz = midpointAz + (midpointAz - oldAz);
        int newEl = midpointEl + (midpointEl - oldEl);

        log.info("Half-flip: ({}, {}) -> ({}, {}) -> ({}, {})", oldAz, oldEl, midpointAz, midpointEl, newAz, newEl);
        return new AzimuthElevation(newAz, newEl);
    }


}
