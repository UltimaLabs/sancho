package com.ultimalabs.sattrackclient.scheduler;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.config.SatelliteData;
import com.ultimalabs.sattrackclient.common.config.StationDetails;
import com.ultimalabs.sattrackclient.scheduler.model.PassEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

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

        getNextPass();

    }

    /**
     * Fetches the next pass
     *
     * @return pass data
     */
    private PassEventData getNextPass() {

        RestTemplate restTemplate = new RestTemplate();

        for (SatelliteData sat : config.getSatelliteData()) {

            String url = queryUrlBuilder(sat, config.getStation());
            String result = restTemplate.getForObject(url, String.class);
            log.info("\n\n" + result + "\n");

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
