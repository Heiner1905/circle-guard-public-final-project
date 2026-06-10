package com.circleguard.promotion.service;

import com.circleguard.promotion.model.AccessPoint;
import com.circleguard.promotion.model.Building;
import com.circleguard.promotion.model.Floor;
import com.circleguard.promotion.repository.jpa.AccessPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationResolutionServiceTest {

    @Mock
    private AccessPointRepository accessPointRepository;

    @Mock
    private MacSessionRegistry sessionRegistry;

    @Mock
    private GraphService graphService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private LocationResolutionService locationResolutionService;

    private UUID buildingId;
    private UUID floorId;
    private UUID accessPointId;

    @BeforeEach
    void setUp() {
        locationResolutionService = new LocationResolutionService(
            accessPointRepository, sessionRegistry, graphService, kafkaTemplate, redisTemplate
        );
        
        buildingId = UUID.randomUUID();
        floorId = UUID.randomUUID();
        accessPointId = UUID.randomUUID();
    }

    @Test
    void processSignal_WithValidData_ShouldProcessSuccessfully() {
        // Arrange
        String apMac = "AA:BB:CC:DD:EE:FF";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;
        String anonymousId = "user-123";

        Building building = Building.builder().id(buildingId).build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress(apMac)
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(deviceMac)).thenReturn(anonymousId);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(Set.of());

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert
        verify(accessPointRepository).findByMacAddress(apMac);
        verify(sessionRegistry).getAnonymousId(deviceMac);
        verify(kafkaTemplate).send(eq("proximity.detected"), eq(anonymousId), anyMap());
        verify(setOperations).add(anyString(), eq(anonymousId));
        verify(redisTemplate).expire(anyString(), any(java.time.Duration.class));
        verify(graphService).detectAndFormCircles(anyString());
    }

    @Test
    void processSignal_WithUnknownAccessPoint_ShouldLogAndReturn() {
        // Arrange
        String apMac = "UNKNOWN:MAC";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.empty());

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert
        verify(accessPointRepository).findByMacAddress(apMac);
        verify(sessionRegistry, never()).getAnonymousId(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(graphService, never()).recordEncounter(any(), any(), any());
    }

    @Test
    void processSignal_WithUnmappedDeviceMac_ShouldLogAndReturn() {
        // Arrange
        String apMac = "AA:BB:CC:DD:EE:FF";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;

        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress(apMac)
                .build();

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(deviceMac)).thenReturn(null);

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert
        verify(accessPointRepository).findByMacAddress(apMac);
        verify(sessionRegistry).getAnonymousId(deviceMac);
        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(graphService, never()).recordEncounter(any(), any(), any());
    }

    @Test
    void processSignal_WithOtherUsersAtLocation_ShouldRecordEncounters() {
        // Arrange
        String apMac = "AA:BB:CC:DD:EE:FF";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;
        String anonymousId = "user-123";
        String otherUser1 = "user-456";
        String otherUser2 = "user-789";

        Building building = Building.builder().id(buildingId).build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress(apMac)
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(deviceMac)).thenReturn(anonymousId);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(Set.of(otherUser1, otherUser2));

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert
        verify(graphService).recordEncounter(anonymousId, otherUser1, accessPointId.toString());
        verify(graphService).recordEncounter(anonymousId, otherUser2, accessPointId.toString());
        verify(setOperations).add(anyString(), eq(anonymousId));
        verify(redisTemplate).expire(anyString(), any(java.time.Duration.class));
        verify(graphService).detectAndFormCircles(accessPointId.toString());
    }

    @Test
    void processSignal_WithSelfInLocation_ShouldNotRecordSelfEncounter() {
        // Arrange
        String apMac = "AA:BB:CC:DD:EE:FF";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;
        String anonymousId = "user-123";

        Building building = Building.builder().id(buildingId).build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress(apMac)
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(deviceMac)).thenReturn(anonymousId);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(Set.of(anonymousId, "other-user"));

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert
        verify(graphService, never()).recordEncounter(eq(anonymousId), eq(anonymousId), anyString());
        verify(graphService).recordEncounter(anonymousId, "other-user", accessPointId.toString());
    }

    @Test
    void processSignal_WithNullOthers_ShouldNotThrowException() {
        // Arrange
        String apMac = "AA:BB:CC:DD:EE:FF";
        String deviceMac = "11:22:33:44:55:66";
        Double rssi = -65.0;
        String anonymousId = "user-123";

        Building building = Building.builder().id(buildingId).build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress(apMac)
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointRepository.findByMacAddress(apMac)).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(deviceMac)).thenReturn(anonymousId);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(null);

        // Act
        locationResolutionService.processSignal(apMac, deviceMac, rssi);

        // Assert - Should not throw NullPointerException
        verify(setOperations).add(anyString(), eq(anonymousId));
        verify(redisTemplate).expire(anyString(), any(java.time.Duration.class));
        verify(graphService).detectAndFormCircles(accessPointId.toString());
        verify(graphService, never()).recordEncounter(any(), any(), any());
    }

    @Test
    void updateGraph_ShouldAddUserToLocationSet() {
        // Arrange
        String locationId = accessPointId.toString();
        String anonymousId = "user-123";

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(Set.of());

        // Act - Use reflection to call private method or test via processSignal
        // Since updateGraph is private, we test it through processSignal
        Building building = Building.builder().id(buildingId).build();
        Floor floor = Floor.builder()
                .id(floorId)
                .building(building)
                .floorNumber(1)
                .build();
        AccessPoint ap = AccessPoint.builder()
                .id(accessPointId)
                .macAddress("AA:BB:CC:DD:EE:FF")
                .floor(floor)
                .coordinateX(10.5)
                .coordinateY(20.5)
                .name("AP-1")
                .build();

        when(accessPointRepository.findByMacAddress(anyString())).thenReturn(Optional.of(ap));
        when(sessionRegistry.getAnonymousId(anyString())).thenReturn(anonymousId);

        // Act
        locationResolutionService.processSignal("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66", -65.0);

        // Assert
        verify(setOperations).add(eq("spatial:location:" + locationId), eq(anonymousId));
        verify(redisTemplate).expire(eq("spatial:location:" + locationId), any(java.time.Duration.class));
    }
}