package com.circleguard.promotion.controller;

import com.circleguard.promotion.dto.BuildingDTO;
import com.circleguard.promotion.dto.FloorDTO;
import com.circleguard.promotion.model.Building;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.service.BuildingService;
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

@WebMvcTest(BuildingController.class)
@Import(SecurityConfig.class)
class BuildingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuildingService buildingService;

    @MockBean
    private FloorService floorService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createBuilding_WithAdminRole_CreatesAndReturns() throws Exception {
        UUID id = UUID.randomUUID();
        Building building = Building.builder()
                .id(id)
                .name("Science Building")
                .code("SCI")
                .description("Science Building Description")
                .latitude(40.7128)
                .longitude(-74.0060)
                .address("123 Science Ave")
                .build();

        when(buildingService.createBuilding(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(building);

        String json = "{\"name\": \"Science Building\", \"code\": \"SCI\", \"description\": \"Science Building Description\", \"latitude\": 40.7128, \"longitude\": -74.0060, \"address\": \"123 Science Ave\"}";

        mockMvc.perform(post("/api/v1/buildings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Science Building"));
    }

    @Test
    void listBuildings_ReturnsAllBuildings() throws Exception {
        Building building1 = Building.builder().id(UUID.randomUUID()).name("Building 1").code("B1").build();
        Building building2 = Building.builder().id(UUID.randomUUID()).name("Building 2").code("B2").build();
        List<Building> buildings = Arrays.asList(building1, building2);

        when(buildingService.getAllBuildings()).thenReturn(buildings);

        mockMvc.perform(get("/api/v1/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Building 1"))
                .andExpect(jsonPath("$[1].name").value("Building 2"));
    }

    @Test
    void getFloors_ReturnsFloorsForBuilding() throws Exception {
        UUID buildingId = UUID.randomUUID();
        Floor floor1 = Floor.builder().id(UUID.randomUUID()).floorNumber(1).name("First Floor").build();
        Floor floor2 = Floor.builder().id(UUID.randomUUID()).floorNumber(2).name("Second Floor").build();
        List<Floor> floors = Arrays.asList(floor1, floor2);

        when(floorService.getFloorsByBuilding(buildingId)).thenReturn(floors);

        mockMvc.perform(get("/api/v1/buildings/{id}/floors", buildingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].floorNumber").value(1))
                .andExpect(jsonPath("$[1].floorNumber").value(2));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void addFloor_WithAdminRole_AddsAndReturns() throws Exception {
        UUID buildingId = UUID.randomUUID();
        UUID floorId = UUID.randomUUID();
        Floor floor = Floor.builder()
                .id(floorId)
                .floorNumber(3)
                .name("Third Floor")
                .build();

        when(floorService.addFloor(eq(buildingId), anyInt(), anyString())).thenReturn(floor);

        String json = "{\"floorNumber\": 3, \"name\": \"Third Floor\"}";

        mockMvc.perform(post("/api/v1/buildings/{id}/floors", buildingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(floorId.toString()))
                .andExpect(jsonPath("$.floorNumber").value(3));
    }

    @Test
    void deleteBuilding_DeletesAndReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(buildingService).deleteBuilding(id);

        mockMvc.perform(delete("/api/v1/buildings/{id}", id))
                .andExpect(status().isOk());

        verify(buildingService).deleteBuilding(id);
    }
}