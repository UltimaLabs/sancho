package com.ultimalabs.sattrackclient.scheduler;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.config.SatelliteData;
import com.ultimalabs.sattrackclient.common.config.StationDetails;
import com.ultimalabs.sattrackclient.scheduler.model.PassEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scheduler service
 * <p>
 * Fetches next pass data and schedules tracking
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

    /**
     * Config object
     */
    private final SatTrackClientConfig config;

    /**
     * Initial scheduling, executes when the app is run
     */
    @PostConstruct
    private void initScheduling() {

        PassEventData nextPass = getNextPass();

        if (nextPass != null) {
            log.info("Fetched next pass (startup): " + nextPass.toString());
        }

    }

    /**
     * Fetches the next pass data
     *
     * @return pass data
     */
    private PassEventData getNextPass() {

        List<PassEventData> passDataList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (SatelliteData sat : config.getSatelliteData()) {
            String url = queryUrlBuilder(sat, config.getStation());
            PassEventData pass;
            try {
                pass = restTemplate.getForObject(url, PassEventData.class);
                if (pass != null) {
                    pass.setSatelliteData(sat);
                    passDataList.add(pass);
                }
            } catch (RestClientException e) {
                log.error(e.getMessage());
            }

        }

        if (!passDataList.isEmpty()) {

            Collections.sort(passDataList);

            // the first item has the highest priority - earliest set time or
            // longer satellite visibility in case of two overlapping passes
            return passDataList.get(0);
        }

        return null;
    }

    /**
     * Builds a SatTrackAPI service call URL
     *
     * @param sat     satellite data
     * @param station station details
     * @return build service URL
     */
    private String queryUrlBuilder(SatelliteData sat, StationDetails station) {

        if (config == null) {
            log.error("Null config.");
            return "";
        }

        String url = config.getSatTrackApiUrl() + "/passes/" + sat.getId() + "/lat/" + station.getLatitude() +
                "/lon/" + station.getLongitude() + "/alt/" + station.getAltitude() + "/minEl/" +
                sat.getMinElevation();

        if (sat.isRotatorEnabled() && sat.getMinElevation() > 0) {
            return url + "/step/" + sat.getStepSize();
        }

        return url;

    }


}
