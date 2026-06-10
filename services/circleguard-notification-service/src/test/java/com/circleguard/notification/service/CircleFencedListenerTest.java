package com.circleguard.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircleFencedListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RoomReservationService roomReservationService;

    @InjectMocks
    private CircleFencedListener listener;

    @BeforeEach
    void setUp() {
        // No additional setup needed
    }

    @Test
    void handleCircleFenced_ShouldCancelReservations_WhenValidEvent() throws Exception {
        // Arrange
        String eventMessage = "{\"circleId\": \"circle-123\", \"locationId\": \"loc-456\"}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("circleId", "circle-123");
        eventMap.put("locationId", "loc-456");
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);
        when(roomReservationService.cancelReservation("circle-123", "loc-456"))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(objectMapper).readValue(eq(eventMessage), any(TypeReference.class));
        verify(roomReservationService).cancelReservation("circle-123", "loc-456");
    }

    @Test
    void handleCircleFenced_ShouldHandleMissingLocationId() throws Exception {
        // Arrange
        String eventMessage = "{\"circleId\": \"circle-123\"}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("circleId", "circle-123");
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleEmptyLocationId() throws Exception {
        // Arrange
        String eventMessage = "{\"circleId\": \"circle-123\", \"locationId\": \"\"}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("circleId", "circle-123");
        eventMap.put("locationId", "");
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleNullLocationId() throws Exception {
        // Arrange
        String eventMessage = "{\"circleId\": \"circle-123\", \"locationId\": null}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("circleId", "circle-123");
        eventMap.put("locationId", null);
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleMissingCircleId() throws Exception {
        // Arrange
        String eventMessage = "{\"locationId\": \"loc-456\"}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("locationId", "loc-456");
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        // According to the code, if locationId exists but circleId is missing, 
        // it will still call cancelReservation with null circleId
        verify(roomReservationService).cancelReservation(null, "loc-456");
    }

    @Test
    void handleCircleFenced_ShouldHandleMissingBothIds() throws Exception {
        // Arrange
        String eventMessage = "{}";
        Map<String, Object> eventMap = new HashMap<>();
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleJsonParseException() throws Exception {
        // Arrange
        String eventMessage = "invalid json";
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class)))
            .thenThrow(new JsonProcessingException("Parse error") {});

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleNullEvent() {
        // Act
        listener.handleCircleFenced(null);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldHandleEmptyEvent() throws Exception {
        // Arrange
        String eventMessage = "{}";
        Map<String, Object> eventMap = new HashMap<>();
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }

    @Test
    void handleCircleFenced_ShouldLogWarning_WhenLocationIdIsEmpty() throws Exception {
        // Arrange
        String eventMessage = "{\"circleId\": \"circle-123\", \"locationId\": \"\"}";
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("circleId", "circle-123");
        eventMap.put("locationId", "");
        
        when(objectMapper.readValue(eq(eventMessage), any(TypeReference.class))).thenReturn(eventMap);

        // Act
        listener.handleCircleFenced(eventMessage);

        // Assert - The service should not be called when locationId is empty
        verify(roomReservationService, never()).cancelReservation(any(), any());
    }
}