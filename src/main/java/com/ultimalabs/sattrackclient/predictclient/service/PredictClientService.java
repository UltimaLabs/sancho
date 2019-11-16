package com.ultimalabs.sattrackclient.predictclient.service;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.common.config.SatelliteData;
import com.ultimalabs.sattrackclient.common.config.StationDetails;
import com.ultimalabs.sattrackclient.common.model.PassEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Fetches tracking data from the remote SatTrackAPI server
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PredictClientService {

    /**
     * Config object
     */
    private final SatTrackClientConfig config;

    /**
     * Fetches the next pass data
     *
     * @return pass data
     */
    public PassEventData getNextPass() {

        List<PassEventData> passDataList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (SatelliteData sat : config.getSatelliteData()) {
            String url = queryUrlBuilder(sat, config.getStation());
            PassEventData pass;
            try {
                pass = restTemplate.getForObject(url, PassEventData.class);
                if (pass != null) {
                    pass.setSatelliteData(sat);
                    substituteShellVariables(pass);
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
     * Builds an URL for calling SatTrackAPI service
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

    /**
     * Substitutes rise/set shell cmd templates with pass/satellite data
     * @param pass pass data
     */
    private void substituteShellVariables(PassEventData pass) {

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("satId", pass.getSatelliteData().getId());
        valuesMap.put("satName", pass.getSatelliteData().getName());
        valuesMap.put("radioFrequency", Double.toString(pass.getSatelliteData().getRadioFrequency()));
        valuesMap.put("duration", Double.toString(pass.getDuration()));

        String riseTemplateString = pass.getSatelliteData().getSatRiseShellCmdTemplate();
        String setTemplateString = pass.getSatelliteData().getSatSetShellCmdTemplate();

        StringSubstitutor subRise = new StringSubstitutor(valuesMap);
        StringSubstitutor subSet = new StringSubstitutor(valuesMap);

        String resolvedRiseString = subRise.replace(riseTemplateString);
        String resolvedSetString = subSet.replace(setTemplateString);

        pass.getSatelliteData().setSatRiseShellCmdSubstituted(resolvedRiseString);
        pass.getSatelliteData().setSatSetShellCmdSubstituted(resolvedSetString);

    }

}
