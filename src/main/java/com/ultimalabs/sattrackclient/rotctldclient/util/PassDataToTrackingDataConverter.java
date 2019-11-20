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
        AzimuthElevation riseAzEl;
        AzimuthElevation setAzEl;
        Map<Long, AzimuthElevation> azElEntriesHashMap = new HashMap<>();

        if (passData == null || passData.getEventDetails().isEmpty()) {
            return null;
        }

        List<PassEventDetailsEntry> passEventDetailsEntries = passData.getEventDetails();

        PassEventDetailsEntry firstEntry = passEventDetailsEntries.get(0);
        PassEventDetailsEntry lastEntry = passEventDetailsEntries.get(passEventDetailsEntries.size() - 1);
        riseAzEl = new AzimuthElevation(firstEntry.getAz(), firstEntry.getEl());
        setAzEl = new AzimuthElevation(lastEntry.getAz(), lastEntry.getEl());
        trackingStart = firstEntry.getT().getTime() / 1000;
        trackingEnd = lastEntry.getT().getTime() / 1000;

        for (PassEventDetailsEntry entry : passEventDetailsEntries) {

            long timeStamp = entry.getT().getTime() / 1000;
            AzimuthElevation azEl = new AzimuthElevation(entry.getAz(), entry.getEl());

            // TODO convert coordinates if needed

            log.info("Conv: {} {}", azEl.getAzimuth(), azEl.getElevation() );

            azElEntriesHashMap.put(timeStamp, azEl);

        }

        return new TrackingData(trackingStart, trackingEnd, riseAzEl, setAzEl, azElEntriesHashMap);

    }

}
