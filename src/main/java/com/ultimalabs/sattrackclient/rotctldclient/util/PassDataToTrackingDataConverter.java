package com.ultimalabs.sattrackclient.rotctldclient.util;

import com.ultimalabs.sattrackclient.common.model.PassEventData;
import com.ultimalabs.sattrackclient.common.model.PassEventDetailsEntry;
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
    public static TrackingData convert(PassEventData passData) {

        long trackingStart;
        long trackingEnd;
        int maxElevation;
        AzimuthElevation riseAzEl;
        AzimuthElevation setAzEl;
        Map<Long, AzimuthElevation> azElEntriesHashMap = new HashMap<>();
        boolean isFlipped;

        if (passData == null || passData.getEventDetails().isEmpty()) {
            return null;
        }

        List<PassEventDetailsEntry> passEventDetailsEntries = passData.getEventDetails();

        PassEventDetailsEntry firstEntry = passEventDetailsEntries.get(0);
        PassEventDetailsEntry lastEntry = passEventDetailsEntries.get(passEventDetailsEntries.size() - 1);
        maxElevation = 0;
        trackingStart = firstEntry.getT().getTime() / 1000;
        trackingEnd = lastEntry.getT().getTime() / 1000;

        isFlipped = shouldFlip(passEventDetailsEntries);

        for (PassEventDetailsEntry entry : passEventDetailsEntries) {

            long timeStamp = entry.getT().getTime() / 1000;

            AzimuthElevation azEl;

            if (isFlipped) {
                azEl = new AzimuthElevation(entry.getAz() + 180, 180 - entry.getEl());
            } else {
                azEl = new AzimuthElevation(entry.getAz(), entry.getEl());
            }

            if (AzimuthElevationUtil.normalizeAngle(entry.getEl()) > maxElevation) {
                maxElevation = AzimuthElevationUtil.normalizeAngle(entry.getEl());
            }

            azElEntriesHashMap.put(timeStamp, azEl);

        }

        if (isFlipped) {
            riseAzEl = new AzimuthElevation(firstEntry.getAz() + 180, 180 - firstEntry.getEl());
            setAzEl = new AzimuthElevation(lastEntry.getAz() + 180, 180 - lastEntry.getEl());
        } else {
            riseAzEl = new AzimuthElevation(firstEntry.getAz(), firstEntry.getEl());
            setAzEl = new AzimuthElevation(lastEntry.getAz(), lastEntry.getEl());
        }

        return new TrackingData(trackingStart, trackingEnd, riseAzEl, setAzEl, maxElevation, azElEntriesHashMap);

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
    private static boolean shouldFlip(List<PassEventDetailsEntry> passEventDetailsEntries) {

        int oldAzimuth = getAzimuth(passEventDetailsEntries.get(0));
        int newAzimuth;

        for (PassEventDetailsEntry entry : passEventDetailsEntries) {

            newAzimuth = getAzimuth(entry);

            if ((newAzimuth == 0 && oldAzimuth == 359) || (newAzimuth == 359 && oldAzimuth == 0)) {
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
    private static int getAzimuth(PassEventDetailsEntry entry) {
        return AzimuthElevationUtil.normalizeAngle((int) Math.round(entry.getAz()));
    }


}
