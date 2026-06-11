package com.circleguard.promotion.controller;

import com.circleguard.promotion.model.jpa.SystemSettings;
import com.circleguard.promotion.repository.jpa.SystemSettingsRepository;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemSettingsRepository settingsRepository;

    @Test
    void getSettings_WhenExists_ReturnsSettings() throws Exception {
        SystemSettings settings = SystemSettings.builder()
                .unconfirmedFencingEnabled(true)
                .autoThresholdSeconds(3600L)
                .mandatoryFenceDays(14)
                .encounterWindowDays(14)
                .build();

        when(settingsRepository.getSettings()).thenReturn(Optional.of(settings));

        mockMvc.perform(get("/api/v1/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unconfirmedFencingEnabled").value(true))
                .andExpect(jsonPath("$.autoThresholdSeconds").value(3600))
                .andExpect(jsonPath("$.mandatoryFenceDays").value(14));
    }

    @Test
    void getSettings_WhenNotExists_ReturnsDefaultSettings() throws Exception {
        when(settingsRepository.getSettings()).thenReturn(Optional.empty());
        
        SystemSettings defaultSettings = SystemSettings.builder()
                .unconfirmedFencingEnabled(true)
                .autoThresholdSeconds(3600L)
                .mandatoryFenceDays(14)
                .encounterWindowDays(14)
                .build();
        
        when(settingsRepository.save(any(SystemSettings.class))).thenReturn(defaultSettings);

        mockMvc.perform(get("/api/v1/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unconfirmedFencingEnabled").value(true));
    }

    @Test
    void updateSettings_UpdatesAndReturnsSettings() throws Exception {
        SystemSettings existingSettings = SystemSettings.builder()
                .unconfirmedFencingEnabled(true)
                .autoThresholdSeconds(3600L)
                .mandatoryFenceDays(14)
                .encounterWindowDays(14)
                .build();

        when(settingsRepository.getSettings()).thenReturn(Optional.of(existingSettings));
        when(settingsRepository.save(any(SystemSettings.class))).thenReturn(existingSettings);

        String json = "{\"unconfirmedFencingEnabled\": false, \"autoThresholdSeconds\": 7200}";

        mockMvc.perform(post("/api/v1/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unconfirmedFencingEnabled").value(false))
                .andExpect(jsonPath("$.autoThresholdSeconds").value(7200));
    }

    @Test
    void toggleUnconfirmedFencing_TogglesAndReturns() throws Exception {
        SystemSettings settings = SystemSettings.builder()
                .unconfirmedFencingEnabled(false)
                .autoThresholdSeconds(3600L)
                .mandatoryFenceDays(14)
                .encounterWindowDays(14)
                .build();

        when(settingsRepository.getSettings()).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(SystemSettings.class))).thenReturn(settings);

        mockMvc.perform(post("/api/v1/admin/settings/toggle-unconfirmed-fencing")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unconfirmedFencingEnabled").value(true));
    }
}