package com.circleguard.promotion.controller;

import com.circleguard.promotion.dto.AccessPointDTO;
import com.circleguard.promotion.dto.FloorDTO;
import com.circleguard.promotion.model.AccessPoint;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.service.AccessPointService;
import com.circleguard.promotion.service.FloorService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FloorController.class)
@Import(SecurityConfig.class)
class FloorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FloorService floorService;

    @MockBean
    private AccessPointService accessPointService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void addAccessPoint_WithAdmin_ShouldReturnAccessPoint() throws Exception {
        UUID floorId = UUID.randomUUID();
        UUID apId = UUID.randomUUID();
        AccessPoint ap = AccessPoint.builder()
                .id(apId)
                .macAddress("AA:BB:CC:DD:EE:FF")
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointService.registerAccessPoint(eq(floorId), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(ap);

        String json = "{\"macAddress\": \"AA:BB:CC:DD:EE:FF\", \"coordinateX\": 10.5, \"coordinateY\": 20.5, \"name\": \"AP-1\"}";

        mockMvc.perform(post("/api/v1/floors/{id}/access-points", floorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(apId.toString()))
                .andExpect(jsonPath("$.macAddress").value("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void getAccessPoints_ShouldReturnList() throws Exception {
        UUID floorId = UUID.randomUUID();
        AccessPoint ap1 = AccessPoint.builder().id(UUID.randomUUID()).macAddress("AA:BB:CC:DD:EE:FF").build();
        AccessPoint ap2 = AccessPoint.builder().id(UUID.randomUUID()).macAddress("11:22:33:44:55:66").build();
        List<AccessPoint> accessPoints = Arrays.asList(ap1, ap2);

        when(accessPointService.getAccessPointsByFloor(floorId)).thenReturn(accessPoints);

        mockMvc.perform(get("/api/v1/floors/{id}/access-points", floorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateFloor_WithAdmin_ShouldReturnUpdatedFloor() throws Exception {
        UUID floorId = UUID.randomUUID();
        Floor floor = Floor.builder()
                .id(floorId)
                .floorNumber(1)
                .name("First Floor - Updated")
                .floorPlanUrl("https://example.com/floorplan.jpg")
                .build();

        when(floorService.updateFloor(eq(floorId), eq(1), eq("First Floor - Updated"), eq("https://example.com/floorplan.jpg")))
                .thenReturn(floor);

        String json = "{\"floorNumber\": 1, \"name\": \"First Floor - Updated\", \"floorPlanUrl\": \"https://example.com/floorplan.jpg\"}";

        mockMvc.perform(put("/api/v1/floors/{id}", floorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("First Floor - Updated"));
    }

    @Test
    void deleteFloor_ShouldReturnOk() throws Exception {
        UUID floorId = UUID.randomUUID();
        doNothing().when(floorService).deleteFloor(floorId);

        mockMvc.perform(delete("/api/v1/floors/{id}", floorId))
                .andExpect(status().isOk());

        verify(floorService).deleteFloor(floorId);
    }
}