package com.ultimalabs.sancho.api.config.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultimalabs.sancho.common.config.RadioConfig;
import com.ultimalabs.sancho.common.config.RotatorConfig;
import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.common.config.SatelliteData;
import com.ultimalabs.sancho.common.config.StationDetails;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private SanchoConfig sanchoConfigOk;
    private SanchoConfig sanchoConfigNotOk;

    public ConfigTest() {

        StationDetails stationDetails = new StationDetails("Test station", 1.0, 1.0, 1);
        RotatorConfig rotatorConfig = new RotatorConfig("127.0.0.1", 4533, 0.25);
        RadioConfig radioConfig = new RadioConfig("127.0.0.1", 4532);

        SatelliteData sat1 = new SatelliteData("25338", "NOAA15", 137.62, 10.0, 10.0, 1, false, false, "", "");
        SatelliteData sat2 = new SatelliteData("28654", "NOAA18", 137.9125, 10.0, 10.0, 1, false, false, "", "");

        List<SatelliteData> emptySatsList = new ArrayList<>();

        List<SatelliteData> satellites = new ArrayList<>();
        satellites.add(sat1);
        satellites.add(sat2);

        sanchoConfigOk = new SanchoConfig(true, 180, "https://sattrackapi.ultima.hr:8443/api/v1", stationDetails,
                rotatorConfig, radioConfig, satellites);
        sanchoConfigNotOk = new SanchoConfig(true, 180, "https://sattrackapi.ultima.hr:8443/api/v1", stationDetails,
                rotatorConfig, radioConfig, emptySatsList);

    }

    @DisplayName("Submit valid Sancho config")
    @Test
    void testUpdateConfigOk() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/config/").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sanchoConfigOk)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @DisplayName("Submit invalid Sancho config")
    @Test
    void testUpdateConfigNotOk() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/config/").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sanchoConfigNotOk)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
