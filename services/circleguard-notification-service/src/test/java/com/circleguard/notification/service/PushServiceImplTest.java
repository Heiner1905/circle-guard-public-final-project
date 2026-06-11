package com.circleguard.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private AuditLogService auditLogService;

    private PushServiceImpl pushService;

    @BeforeEach
    void setUp() {
        // Configure mocks BEFORE creating the service
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        // Create the service manually
        pushService = new PushServiceImpl(webClientBuilder, "http://localhost:8080");
        
        // Set fields using reflection
        ReflectionTestUtils.setField(pushService, "auditLogService", auditLogService);
        ReflectionTestUtils.setField(pushService, "gotifyToken", "MOCK_TOKEN");
        ReflectionTestUtils.setField(pushService, "gotifyUrl", "http://localhost:8080");
    }

    @Test
    void sendAsync_WithMockToken_ShouldLogAndReturnSuccess() throws Exception {
        // Arrange
        String userId = "user-123";
        String message = "Test message";

        // Act
        CompletableFuture<Void> future = pushService.sendAsync(userId, message);
        future.get();

        // Assert
        verify(auditLogService).logDelivery(eq(userId), eq("PUSH"), eq("SUCCESS"), anyString());
        assertThat(future).isCompleted();
    }

    @Test
    void sendAsync_WithMockTokenAndMetadata_ShouldLogAndReturnSuccess() throws Exception {
        // Arrange
        String userId = "user-123";
        String message = "Test message";
        Map<String, String> metadata = Map.of("key", "value");

        // Act
        CompletableFuture<Void> future = pushService.sendAsync(userId, message, metadata);
        future.get();

        // Assert
        verify(auditLogService).logDelivery(eq(userId), eq("PUSH"), eq("SUCCESS"), anyString());
        assertThat(future).isCompleted();
    }

    @Test
    void sendAsync_WithRealToken_ShouldHandleErrorGracefully() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(pushService, "gotifyToken", "REAL_TOKEN");
        
        // Make webClient.post() throw an exception (simulating network error)
        doThrow(new RuntimeException("Network error")).when(webClient).post();

        String userId = "user-123";
        String message = "Test message";

        // Act & Assert - The method should throw the exception
        assertThrows(Exception.class, () -> {
            CompletableFuture<Void> future = pushService.sendAsync(userId, message);
            future.get();
        });

        verify(auditLogService).logDelivery(eq(userId), eq("PUSH"), eq("RETRY"), anyString());
    }

    @Test
    void sendAsync_WhenRecoverCalled_ShouldLogFailure() {
        // This tests the recover method directly
        Exception testException = new RuntimeException("Test error");
        
        CompletableFuture<Void> result = pushService.recover(testException, "user-123", "test message", Map.of());
        
        verify(auditLogService).logDelivery(eq("user-123"), eq("PUSH"), eq("FAILED"), isNull());
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void sendAsync_WithNullMetadata_ShouldHandleGracefully() throws Exception {
        // Arrange
        String userId = "user-123";
        String message = "Test message";

        // Act
        CompletableFuture<Void> future = pushService.sendAsync(userId, message, null);
        future.get();

        // Assert
        verify(auditLogService).logDelivery(eq(userId), eq("PUSH"), eq("SUCCESS"), anyString());
        assertThat(future).isCompleted();
    }

    @Test
    void sendAsync_SingleArgMethod_DelegatesToThreeArgMethod() throws Exception {
        // Arrange
        String userId = "user-123";
        String message = "Test message";
        
        // Spy on the service to verify delegation
        PushServiceImpl spyService = spy(pushService);
        
        // Act
        spyService.sendAsync(userId, message);
        
        // Assert - Verify that the three-arg method was called with empty map
        verify(spyService).sendAsync(eq(userId), eq(message), eq(Map.of()));
    }
}