package com.circleguard.promotion.service;

import com.circleguard.promotion.model.AccessPoint;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.repository.jpa.AccessPointRepository;
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
class AccessPointServiceTest {

    @Mock
    private AccessPointRepository accessPointRepository;

    @Mock
    private FloorRepository floorRepository;

    @InjectMocks
    private AccessPointService accessPointService;

    private UUID floorId;
    private UUID accessPointId;

    @BeforeEach
    void setUp() {
        floorId = UUID.randomUUID();
        accessPointId = UUID.randomUUID();
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
        AccessPoint result = accessPointService.registerAccessPoint(floorId, "AA:BB:CC:DD:EE:FF", 10.5, 20.5, "AP-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMacAddress()).isEqualTo("AA:BB:CC:DD:EE:FF");
        verify(accessPointRepository).save(any(AccessPoint.class));
    }

    @Test
    void registerAccessPoint_WhenFloorNotFound_ShouldThrowException() {
        // Arrange
        when(floorRepository.findById(floorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accessPointService.registerAccessPoint(floorId, "MAC", 0.0, 0.0, "Name"));
        verify(accessPointRepository, never()).save(any());
    }

    @Test
    void getAccessPoint_WhenExists_ShouldReturnOptional() {
        // Arrange
        AccessPoint ap = AccessPoint.builder().id(accessPointId).build();
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.of(ap));

        // Act
        Optional<AccessPoint> result = accessPointService.getAccessPoint(accessPointId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(accessPointId);
    }

    @Test
    void getAccessPoint_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.empty());

        // Act
        Optional<AccessPoint> result = accessPointService.getAccessPoint(accessPointId);

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
        List<AccessPoint> result = accessPointService.getAccessPointsByFloor(floorId);

        // Assert
        assertThat(result).hasSize(2);
        verify(accessPointRepository).findByFloorId(floorId);
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
        AccessPoint result = accessPointService.updateAccessPoint(accessPointId, "NEW:MAC", 15.0, 25.0, "New Name");

        // Assert
        assertThat(result.getMacAddress()).isEqualTo("NEW:MAC");
        assertThat(result.getCoordinateX()).isEqualTo(15.0);
        assertThat(result.getCoordinateY()).isEqualTo(25.0);
        assertThat(result.getName()).isEqualTo("New Name");
        verify(accessPointRepository).save(ap);
    }

    @Test
    void updateAccessPoint_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(accessPointRepository.findById(accessPointId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accessPointService.updateAccessPoint(accessPointId, "MAC", 0.0, 0.0, "Name"));
        verify(accessPointRepository, never()).save(any());
    }

    @Test
    void deleteAccessPoint_ShouldDelete() {
        // Arrange
        doNothing().when(accessPointRepository).deleteById(accessPointId);

        // Act
        accessPointService.deleteAccessPoint(accessPointId);

        // Assert
        verify(accessPointRepository).deleteById(accessPointId);
    }
}