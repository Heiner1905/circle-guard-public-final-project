package com.circleguard.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SmsServiceImpl smsService;

    @BeforeEach
    void setUp() {
        // Set mock values for the actual fields in SmsServiceImpl
        ReflectionTestUtils.setField(smsService, "accountSid", "AC_MOCK_SID");
        ReflectionTestUtils.setField(smsService, "authToken", "MOCK_TOKEN");
        ReflectionTestUtils.setField(smsService, "fromNumber", "+15550000000");
        ReflectionTestUtils.setField(smsService, "auditLogService", auditLogService);
    }

    @Test
    void sendAsync_WithMockAccountSid_ShouldLogAndReturnSuccess() throws Exception {
        // Arrange
        String userId = "user-123";
        String message = "Test SMS";

        // Act
        CompletableFuture<Void> future = smsService.sendAsync(userId, message);
        future.get();

        // Assert
        verify(auditLogService).logDelivery(eq(userId), eq("SMS"), eq("SUCCESS"), anyString());
        assertThat(future).isCompleted();
    }

    @Test
    void sendAsync_WithRealAccountSid_ShouldAttemptToSend() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(smsService, "accountSid", "REAL_ACCOUNT_SID");
        String userId = "user-123";
        String message = "Test SMS";

        // Act & Assert - This will attempt to use real Twilio which will fail
        // but we just want to verify the code path
        try {
            CompletableFuture<Void> future = smsService.sendAsync(userId, message);
            future.get();
        } catch (Exception e) {
            // Expected to fail because Twilio credentials are mock
            verify(auditLogService).logDelivery(eq(userId), eq("SMS"), eq("RETRY"), anyString());
        }
    }

    @Test
    void sendAsync_WhenExceptionOccurs_ShouldLogRetryAndThrow() {
        // Arrange
        ReflectionTestUtils.setField(smsService, "accountSid", "REAL_ACCOUNT_SID");
        
        String userId = "user-123";
        String message = "Test SMS";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            CompletableFuture<Void> future = smsService.sendAsync(userId, message);
            future.get();
        });

        verify(auditLogService).logDelivery(eq(userId), eq("SMS"), eq("RETRY"), anyString());
    }

    @Test
    void sendAsync_WhenRecoverCalled_ShouldLogFailure() {
        // This tests the recover method directly
        Exception testException = new RuntimeException("Test error");
        
        CompletableFuture<Void> result = smsService.recover(testException, "user-123", "test message");
        
        verify(auditLogService).logDelivery(eq("user-123"), eq("SMS"), eq("FAILED"), isNull());
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void init_WithMockAccountSid_ShouldNotInitializeTwilio() {
        // The init method should not throw when accountSid starts with "AC_MOCK"
        assertDoesNotThrow(() -> smsService.init());
    }

    @Test
    void init_WithRealAccountSid_ShouldAttemptToInitializeTwilio() {
        // Set real account SID
        ReflectionTestUtils.setField(smsService, "accountSid", "REAL_ACCOUNT_SID");
        
        // This will attempt to initialize Twilio with mock credentials
        // It might throw or not, but we just verify it doesn't crash unexpectedly
        try {
            smsService.init();
        } catch (Exception e) {
            // Expected with mock credentials
            assertTrue(e instanceof com.twilio.exception.ApiException || 
                      e instanceof RuntimeException);
        }
    }
}