package com.circleguard.promotion.controller;

import com.circleguard.promotion.service.HealthStatusService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthStatusController.class)
@Import(SecurityConfig.class)
class HealthStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthStatusService statusService;

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void confirmPositive_WithPermission_CallsUpdateStatus() throws Exception {
        String json = "{\"anonymousId\": \"user-1\"}";

        mockMvc.perform(post("/api/v1/health/confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", "CONFIRMED");
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void resolve_WithPermission_CallsResolveStatus() throws Exception {
        String json = "{\"anonymousId\": \"user-1\"}";

        mockMvc.perform(post("/api/v1/health/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).resolveStatus("user-1", false);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void resolve_WithAdminOverride_CallsResolveStatusWithTrue() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"adminOverride\": true}";

        mockMvc.perform(post("/api/v1/health/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).resolveStatus("user-1", true);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void recover_WithPermission_CallsPromoteToRecovered() throws Exception {
        String userId = "user-123";

        mockMvc.perform(post("/api/v1/health/recovery/{id}", userId))
                .andExpect(status().isOk());

        verify(statusService).promoteToRecovered(userId);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatus_WithCompleteData_CallsUpdateStatusWithOverride() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"status\": \"CONFIRMED\", \"adminOverride\": true}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", "CONFIRMED", true);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatus_WithoutAdminOverride_CallsUpdateStatusWithFalse() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"status\": \"SUSPECT\"}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", "SUSPECT", false);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatus_WithAdminOverrideFalse_ShouldPassFalse() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"status\": \"PROBABLE\", \"adminOverride\": false}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", "PROBABLE", false);
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void reportStatus_WithoutPermission_Returns403() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"status\": \"CONFIRMED\"}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(statusService, never()).updateStatus(any(), any(), anyBoolean());
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void recover_WithoutPermission_Returns403() throws Exception {
        String userId = "user-123";

        mockMvc.perform(post("/api/v1/health/recovery/{id}", userId))
                .andExpect(status().isForbidden());

        verify(statusService, never()).promoteToRecovered(any());
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void confirmPositive_WithoutPermission_Returns403() throws Exception {
        String json = "{\"anonymousId\": \"user-1\"}";

        mockMvc.perform(post("/api/v1/health/confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(statusService, never()).updateStatus(any(), anyString());
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatus_WithMissingAnonymousId_ShouldStillCallService() throws Exception {
        String json = "{\"status\": \"CONFIRMED\"}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Service will receive null anonymousId - that's the service's responsibility to handle
        verify(statusService).updateStatus(null, "CONFIRMED", false);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void reportStatus_WithMissingStatus_ShouldStillCallService() throws Exception {
        String json = "{\"anonymousId\": \"user-1\"}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus("user-1", null, false);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void confirmPositive_WithEmptyJson_ShouldStillCallService() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/v1/health/confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(statusService).updateStatus(null, "CONFIRMED");
    }

    @Test
    void reportStatus_Unauthenticated_Returns403() throws Exception {
        String json = "{\"anonymousId\": \"user-1\", \"status\": \"CONFIRMED\"}";

        mockMvc.perform(post("/api/v1/health/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(statusService, never()).updateStatus(any(), any(), anyBoolean());
    }

    @Test
    void recover_Unauthenticated_Returns403() throws Exception {
        String userId = "user-123";

        mockMvc.perform(post("/api/v1/health/recovery/{id}", userId))
                .andExpect(status().isForbidden());

        verify(statusService, never()).promoteToRecovered(any());
    }

    @Test
    void confirmPositive_Unauthenticated_Returns403() throws Exception {
        String json = "{\"anonymousId\": \"user-1\"}";

        mockMvc.perform(post("/api/v1/health/confirmed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(statusService, never()).updateStatus(any(), anyString());
    }
}