package com.circleguard.promotion.service;

import com.circleguard.promotion.model.AccessPoint;
import com.circleguard.promotion.model.Building;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.repository.jpa.AccessPointRepository;
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
class SpatialServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private AccessPointRepository accessPointRepository;

    @InjectMocks
    private SpatialService spatialService;

    private UUID buildingId;
    private UUID floorId;
    private UUID accessPointId;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        floorId = UUID.randomUUID();
        accessPointId = UUID.randomUUID();
    }

    @Test
    void createBuilding_ShouldSaveAndReturnBuilding() {
        // Arrange
        Building building = Building.builder()
                .id(buildingId)
                .name("Science Building")
                .code("SCI")
                .description("Main science building")
                .build();

        when(buildingRepository.save(any(Building.class))).thenReturn(building);

        // Act
        Building result = spatialService.createBuilding("Science Building", "SCI", "Main science building");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Science Building");
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    void addFloor_WhenBuildingExists_ShouldSaveAndReturnFloor() {
        // Arrange
        Building building = Building.builder().id(buildingId).name("Building").build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .name("First Floor")
                .build();

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
        when(floorRepository.save(any(Floor.class))).thenReturn(floor);

        // Act
        Floor result = spatialService.addFloor(buildingId, 1, "First Floor");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFloorNumber()).isEqualTo(1);
        verify(floorRepository).save(any(Floor.class));
    }

    @Test
    void addFloor_WhenBuildingNotFound_ShouldThrowException() {
        // Arrange
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.addFloor(buildingId, 1, "First Floor"));
        verify(floorRepository, never()).save(any());
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
        List<Building> result = spatialService.getAllBuildings();

        // Assert
        assertThat(result).hasSize(2);
        verify(buildingRepository).findAll();
    }

    @Test
    void getFloorsByBuilding_ShouldReturnList() {
        // Arrange
        List<Floor> floors = Arrays.asList(
                Floor.builder().id(UUID.randomUUID()).floorNumber(1).build(),
                Floor.builder().id(UUID.randomUUID()).floorNumber(2).build()
        );
        when(floorRepository.findByBuildingId(buildingId)).thenReturn(floors);

        // Act
        List<Floor> result = spatialService.getFloorsByBuilding(buildingId);

        // Assert
        assertThat(result).hasSize(2);
        verify(floorRepository).findByBuildingId(buildingId);
    }

    @Test
    void updateBuilding_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        Building building = Building.builder()
                .id(buildingId)
                .name("Old Name")
                .code("OLD")
                .description("Old description")
                .build();

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
        when(buildingRepository.save(any(Building.class))).thenReturn(building);

        // Act
        Building result = spatialService.updateBuilding(buildingId, "New Name", "NEW", "New description");

        // Assert
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCode()).isEqualTo("NEW");
        verify(buildingRepository).save(building);
    }

    @Test
    void updateBuilding_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.updateBuilding(buildingId, "Name", "CODE", "Desc"));
    }

    @Test
    void deleteBuilding_WhenNoFloors_ShouldDelete() {
        // Arrange
        when(floorRepository.findByBuildingId(buildingId)).thenReturn(Collections.emptyList());
        doNothing().when(buildingRepository).deleteById(buildingId);

        // Act
        spatialService.deleteBuilding(buildingId);

        // Assert
        verify(buildingRepository).deleteById(buildingId);
    }

    @Test
    void deleteBuilding_WhenHasFloors_ShouldThrowException() {
        // Arrange
        List<Floor> floors = Arrays.asList(Floor.builder().build());
        when(floorRepository.findByBuildingId(buildingId)).thenReturn(floors);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.deleteBuilding(buildingId));
        verify(buildingRepository, never()).deleteById(any());
    }

    @Test
    void updateFloor_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        Floor floor = Floor.builder()
                .id(floorId)
                .floorNumber(1)
                .name("Old Name")
                .build();

        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));
        when(floorRepository.save(any(Floor.class))).thenReturn(floor);

        // Act
        Floor result = spatialService.updateFloor(floorId, 2, "New Name");

        // Assert
        assertThat(result.getFloorNumber()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("New Name");
        verify(floorRepository).save(floor);
    }

    @Test
    void updateFloor_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.updateFloor(floorId, 1, "Name"));
    }

    @Test
    void deleteFloor_WhenNoAccessPoints_ShouldDelete() {
        // Arrange
        when(accessPointRepository.findByFloorId(floorId)).thenReturn(Collections.emptyList());
        doNothing().when(floorRepository).deleteById(floorId);

        // Act
        spatialService.deleteFloor(floorId);

        // Assert
        verify(floorRepository).deleteById(floorId);
    }

    @Test
    void deleteFloor_WhenHasAccessPoints_ShouldThrowException() {
        // Arrange
        List<AccessPoint> accessPoints = Arrays.asList(AccessPoint.builder().build());
        when(accessPointRepository.findByFloorId(floorId)).thenReturn(accessPoints);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.deleteFloor(floorId));
        verify(floorRepository, never()).deleteById(any());
    }

    @Test
    void registerAccessPoint_WhenFloorExists_ShouldSaveAndReturn() {
        // Arrange
        Floor floor = Floor.builder().id(floorId).build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .floor(floor)
                .macAddress("AA:BB:CC:DD:EE:FF")
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(floorRepository.findById(floorId)).thenReturn(Optional.of(floor));
        when(accessPointRepository.save(any(AccessPoint.class))).thenReturn(ap);

        // Act
        AccessPoint result = spatialService.registerAccessPoint(floorId, "AA:BB:CC:DD:EE:FF", 10.5, 20.5, "AP-1");

        // Assert
        assertThat(result.getMacAddress()).isEqualTo("AA:BB:CC:DD:EE:FF");
        verify(accessPointRepository).save(any(AccessPoint.class));
    }

    @Test
    void registerAccessPoint_WhenFloorNotFound_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.registerAccessPoint(floorId, "MAC", 0.0, 0.0, "Name"));
    }

    @Test
    void getAccessPoint_WhenExists_ShouldReturnOptional() {
        // Arrange
        AccessPoint ap = AccessPoint.builder().id(accessPointId).build();
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.of(ap));

        // Act
        Optional<AccessPoint> result = spatialService.getAccessPoint(accessPointId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(accessPointId);
    }

    @Test
    void getAccessPoint_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.empty());

        // Act
        Optional<AccessPoint> result = spatialService.getAccessPoint(accessPointId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAccessPointsByFloor_ShouldReturnList() {
        // Arrange
        List<AccessPoint> accessPoints = Arrays.asList(
                AccessPoint.builder().id(UUID.randomUUID()).build(),
                AccessPoint.builder().id(UUID.randomUUID()).build()
        );
        when(accessPointRepository.findByFloorId(floorId)).thenReturn(accessPoints);

        // Act
        List<AccessPoint> result = spatialService.getAccessPointsByFloor(floorId);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void updateAccessPoint_WhenExists_ShouldUpdateAndReturn() {
        // Arrange
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress("OLD:MAC")
                .coordinateX(0.0)
                .coordinateY(0.0)
                .name("Old Name")
                .build();

        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.of(ap));
        when(accessPointRepository.save(any(AccessPoint.class))).thenReturn(ap);

        // Act
        AccessPoint result = spatialService.updateAccessPoint(accessPointId, "NEW:MAC", 15.0, 25.0, "New Name");

        // Assert
        assertThat(result.getMacAddress()).isEqualTo("NEW:MAC");
        assertThat(result.getCoordinateX()).isEqualTo(15.0);
        assertThat(result.getName()).isEqualTo("New Name");
        verify(accessPointRepository).save(ap);
    }

    @Test
    void updateAccessPoint_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> spatialService.updateAccessPoint(accessPointId, "MAC", 0.0, 0.0, "Name"));
    }

    @Test
    void deleteAccessPoint_ShouldDelete() {
        // Arrange
        doNothing().when(accessPointRepository).deleteById(accessPointId);

        // Act
        spatialService.deleteAccessPoint(accessPointId);

        // Assert
        verify(accessPointRepository).deleteById(accessPointId);
    }
}