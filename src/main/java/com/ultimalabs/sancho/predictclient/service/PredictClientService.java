package com.ultimalabs.sancho.predictclient.service;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.common.config.SatelliteData;
import com.ultimalabs.sancho.common.config.StationDetails;
import com.ultimalabs.sancho.common.model.SatellitePass;
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
    private final SanchoConfig config;

    /**
     * Fetches the next pass data
     *
     * @return pass data
     */
    public SatellitePass getNextPass() {

        List<SatellitePass> passDataList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (SatelliteData sat : config.getSatelliteData()) {
            String url = queryUrlBuilder(config.getSatTrackApiUrl(), sat, config.getStation());
            SatellitePass pass;
            try {
                pass = restTemplate.getForObject(url, SatellitePass.class);
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
     * Builds an URL for calling the SatTrackAPI service
     *
     * @param sat     satellite data
     * @param station station details
     * @return build service URL
     */
    private String queryUrlBuilder(String satTrackApiUrl, SatelliteData sat, StationDetails station) {

        String url = satTrackApiUrl + "/passes/" + sat.getId() + "/lat/" + station.getLatitude() +
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
    private void substituteShellVariables(SatellitePass pass) {

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("tle", pass.getTle());
        valuesMap.put("satId", pass.getSatelliteData().getId());
        valuesMap.put("satName", pass.getSatelliteData().getName());
        valuesMap.put("radioFrequency", Double.toString(pass.getSatelliteData().getRadioFrequency()));
        valuesMap.put("predictTime", Long.toString(pass.getNow().getTime() / 1000));
        valuesMap.put("rise", Long.toString(pass.getRisePoint().getT().getTime() / 1000));
        valuesMap.put("set", Long.toString(pass.getSetPoint().getT().getTime() / 1000));
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
