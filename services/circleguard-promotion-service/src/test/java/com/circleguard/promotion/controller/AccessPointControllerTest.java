package com.circleguard.promotion.controller;

import com.circleguard.promotion.dto.AccessPointDTO;
import com.circleguard.promotion.model.AccessPoint;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.service.AccessPointService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccessPointController.class)
@Import(SecurityConfig.class)
class AccessPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessPointService accessPointService;

    @Test
    void getAccessPoint_WhenExists_ReturnsAccessPoint() throws Exception {
        UUID id = UUID.randomUUID();
        Floor floor = Floor.builder().id(UUID.randomUUID()).build();
        AccessPoint ap = AccessPoint.builder()
                .id(id)
                .macAddress("AA:BB:CC:DD:EE:FF")
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointService.getAccessPoint(id)).thenReturn(Optional.of(ap));

        mockMvc.perform(get("/api/v1/access-points/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.macAddress").value("AA:BB:CC:DD:EE:FF"))
                .andExpect(jsonPath("$.coordinateX").value(10.5))
                .andExpect(jsonPath("$.name").value("AP-1"));
    }

    @Test
    void getAccessPoint_WhenNotExists_ReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(accessPointService.getAccessPoint(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/access-points/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateAccessPoint_WithAdminRole_UpdatesAndReturns() throws Exception {
        UUID id = UUID.randomUUID();
        Floor floor = Floor.builder().id(UUID.randomUUID()).build();
        AccessPoint ap = AccessPoint.builder()
                .id(id)
                .macAddress("AA:BB:CC:DD:EE:FF")
                .floor(floor)
                .coordinateX(15.0)
                .coordinateY(25.0)
                .name("Updated AP")
                .build();

        when(accessPointService.updateAccessPoint(eq(id), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(ap);

        String json = "{\"macAddress\": \"AA:BB:CC:DD:EE:FF\", \"coordinateX\": 15.0, \"coordinateY\": 25.0, \"name\": \"Updated AP\"}";

        mockMvc.perform(put("/api/v1/access-points/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Updated AP"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteAccessPoint_WithAdminRole_DeletesAndReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(accessPointService).deleteAccessPoint(id);

        mockMvc.perform(delete("/api/v1/access-points/{id}", id))
                .andExpect(status().isOk());

        verify(accessPointService).deleteAccessPoint(id);
    }

    @Test
    @WithMockUser(authorities = "STUDENT")
    void updateAccessPoint_WithoutAdminRole_ReturnsForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        String json = "{\"macAddress\": \"AA:BB:CC:DD:EE:FF\"}";

        mockMvc.perform(put("/api/v1/access-points/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}