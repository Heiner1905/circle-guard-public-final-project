package com.circleguard.promotion.controller;

import com.circleguard.promotion.service.LocationResolutionService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationSignalController.class)
@Import(SecurityConfig.class)
class LocationSignalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationResolutionService locationResolutionService;

    @Test
    void receiveSignal_WithValidData_ShouldProcessSignal() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"deviceMac\": \"11:22:33:44:55:66\", \"rssi\": -65}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(locationResolutionService).processSignal("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66", -65.0);
    }

    @Test
    void receiveSignal_WithRssiAsInteger_ShouldProcessSignal() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"deviceMac\": \"11:22:33:44:55:66\", \"rssi\": -70}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(locationResolutionService).processSignal("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66", -70.0);
    }

    @Test
    void receiveSignal_WithRssiAsString_ShouldProcessSignal() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"deviceMac\": \"11:22:33:44:55:66\", \"rssi\": \"-75\"}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(locationResolutionService).processSignal("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66", -75.0);
    }

    @Test
    void receiveSignal_WithMissingApMac_ShouldReturnBadRequest() throws Exception {
        String json = "{\"deviceMac\": \"11:22:33:44:55:66\", \"rssi\": -65}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(locationResolutionService, never()).processSignal(any(), any(), any());
    }

    @Test
    void receiveSignal_WithMissingDeviceMac_ShouldReturnBadRequest() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"rssi\": -65}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(locationResolutionService, never()).processSignal(any(), any(), any());
    }

    @Test
    void receiveSignal_WithMissingRssi_ShouldReturnBadRequest() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"deviceMac\": \"11:22:33:44:55:66\"}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(locationResolutionService, never()).processSignal(any(), any(), any());
    }

    @Test
    void receiveSignal_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(locationResolutionService, never()).processSignal(any(), any(), any());
    }

    @Test
    void receiveSignal_WithNullRssi_ShouldReturnBadRequest() throws Exception {
        String json = "{\"apMac\": \"AA:BB:CC:DD:EE:FF\", \"deviceMac\": \"11:22:33:44:55:66\", \"rssi\": null}";

        mockMvc.perform(post("/api/v1/location/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(locationResolutionService, never()).processSignal(any(), any(), any());
    }
}