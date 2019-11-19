package com.ultimalabs.sattrackclient.rotctldclient.util;

import com.ultimalabs.sattrackclient.common.model.PassEventDetailsEntry;
import com.ultimalabs.sattrackclient.rotctldclient.model.AzimuthElevation;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Pass data to azimuth/elevation list converter utility class
 */
@Slf4j
public class PassDataToAzElConverter {

    private PassDataToAzElConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a pass event details list into az/el list suitable for tracking
     *
     * @param passEventDetailsEntries list of pass event detail entries
     * @return list of azimuth/elevation positions
     */
    public static List<AzimuthElevation> convert (List<PassEventDetailsEntry> passEventDetailsEntries) {

        List<AzimuthElevation> azimuthElevationList = new ArrayList<>();

        if (passEventDetailsEntries == null) {
            return azimuthElevationList;
        }

        for (PassEventDetailsEntry entry : passEventDetailsEntries) {

            AzimuthElevation azEl = new AzimuthElevation(entry.getAz(), entry.getEl());

            // TODO transform coordinates if needed

            azimuthElevationList.add(azEl);

        }

        return azimuthElevationList;

    }

}
