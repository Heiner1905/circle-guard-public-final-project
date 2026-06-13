package com.circleguard.promotion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MacSessionRegistryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MacSessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        // No configuramos stubbings aquí para evitar UnnecessaryStubbingException
        sessionRegistry = new MacSessionRegistry(redisTemplate);
    }

    @Test
    void registerSession_ShouldStoreMappingWithPrefixedKey() {
        // Arrange
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";
        String anonymousId = "user-123";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        sessionRegistry.registerSession(macAddress, anonymousId);

        // Assert
        verify(valueOperations).set(eq(expectedKey), eq(anonymousId), any(Duration.class));
    }

    @Test
    void registerSession_WithLowercaseMac_ShouldStoreMapping() {
        // Arrange
        String macAddress = "aa:bb:cc:dd:ee:ff";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";
        String anonymousId = "user-123";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        sessionRegistry.registerSession(macAddress, anonymousId);

        // Assert
        verify(valueOperations).set(eq(expectedKey), eq(anonymousId), any(Duration.class));
    }

    @Test
    void getAnonymousId_WhenExists_ShouldReturnId() {
        // Arrange
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";
        String expectedId = "user-123";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(expectedId);

        // Act
        String result = sessionRegistry.getAnonymousId(macAddress);

        // Assert
        assertThat(result).isEqualTo(expectedId);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void getAnonymousId_WhenNotExists_ShouldReturnNull() {
        // Arrange
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // Act
        String result = sessionRegistry.getAnonymousId(macAddress);

        // Assert
        assertThat(result).isNull();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void getAnonymousId_WithLowercaseMac_ShouldReturnId() {
        // Arrange
        String macAddress = "aa:bb:cc:dd:ee:ff";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";
        String expectedId = "user-123";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(expectedId);

        // Act
        String result = sessionRegistry.getAnonymousId(macAddress);

        // Assert
        assertThat(result).isEqualTo(expectedId);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void closeSession_ShouldDeletePrefixedKey() {
        // Arrange
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";

        // Act
        sessionRegistry.closeSession(macAddress);

        // Assert
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void closeSession_WithLowercaseMac_ShouldDeletePrefixedKey() {
        // Arrange
        String macAddress = "aa:bb:cc:dd:ee:ff";
        String expectedKey = "session:mac:aa:bb:cc:dd:ee:ff";

        // Act
        sessionRegistry.closeSession(macAddress);

        // Assert
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void registerSession_ShouldUse8HourTTL() {
        // Arrange
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String anonymousId = "user-123";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        sessionRegistry.registerSession(macAddress, anonymousId);

        // Assert
        verify(valueOperations).set(anyString(), eq(anonymousId), argThat(duration -> 
            duration.equals(Duration.ofHours(8))
        ));
    }
}