package com.circleguard.promotion.service;

import com.circleguard.promotion.model.Building;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.repository.jpa.BuildingRepository;
import com.circleguard.promotion.repository.jpa.FloorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private FloorRepository floorRepository;

    @InjectMocks
    private BuildingService buildingService;

    private UUID buildingId;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
    }

    @Test
    void createBuilding_ShouldSaveAndReturnBuilding() {
        // Arrange
        Building building = Building.builder()
                .id(buildingId)
                .name("Science Building")
                .code("SCI")
                .description("Main science building")
                .latitude(40.7128)
                .longitude(-74.0060)
                .address("123 University Ave")
                .build();

        when(buildingRepository.save(any(Building.class))).thenReturn(building);

        // Act
        Building result = buildingService.createBuilding("Science Building", "SCI", "Main science building", 40.7128, -74.0060, "123 University Ave");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Science Building");
        assertThat(result.getLatitude()).isEqualTo(40.7128);
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    void getAllBuildings_ShouldReturnList() {
        // Arrange
        List<Building> buildings = Arrays.asList(
                Building.builder().id(UUID.randomUUID()).name("Building 1").build(),
                Building.builder().id(UUID.randomUUID()).name("Building 2").build()
        );
        when(buildingRepository.findAll()).thenReturn(buildings);

        // Act
        List<Building> result = buildingService.getAllBuildings();

        // Assert
        assertThat(result).hasSize(2);
        verify(buildingRepository).findAll();
    }

    @Test
    void updateBuilding_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        Building building = Building.builder()
                .id(buildingId)
                .name("Old Name")
                .code("OLD")
                .description("Old description")
                .latitude(0.0)
                .longitude(0.0)
                .address("Old address")
                .build();

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
        when(buildingRepository.save(any(Building.class))).thenReturn(building);

        // Act
        Building result = buildingService.updateBuilding(buildingId, "New Name", "NEW", "New description", 40.7128, -74.0060, "New address");

        // Assert
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCode()).isEqualTo("NEW");
        assertThat(result.getLatitude()).isEqualTo(40.7128);
        verify(buildingRepository).save(building);
    }

    @Test
    void updateBuilding_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> buildingService.updateBuilding(buildingId, "Name", "CODE", "Desc", 0.0, 0.0, "Address"));
        verify(buildingRepository, never()).save(any());
    }

    @Test
    void deleteBuilding_WhenNoFloors_ShouldDelete() {
        // Arrange
        when(floorRepository.findByBuildingId(buildingId)).thenReturn(Collections.emptyList());
        doNothing().when(buildingRepository).deleteById(buildingId);

        // Act
        buildingService.deleteBuilding(buildingId);

        // Assert
        verify(buildingRepository).deleteById(buildingId);
    }

    @Test
    void deleteBuilding_WhenHasFloors_ShouldThrowException() {
        // Arrange
        List<Floor> floors = Arrays.asList(Floor.builder().build());
        when(floorRepository.findByBuildingId(buildingId)).thenReturn(floors);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> buildingService.deleteBuilding(buildingId));
        verify(buildingRepository, never()).deleteById(any());
    }
}