package com.circleguard.auth.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IdentityClient identityClient;

    @Test
    void getAnonymousId_Success_ReturnsUUID() {
        // Arrange
        UUID expectedUuid = UUID.randomUUID();
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)
        )).thenReturn(Map.of("anonymousId", expectedUuid.toString()));

        // Act
        UUID result = identityClient.getAnonymousId("john@example.com");

        // Assert
        assertThat(result).isEqualTo(expectedUuid);
    }

    @Test
    void getAnonymousId_RestTemplateThrowsException_ThrowsIdentityServiceUnavailableException() {
        // Arrange
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> identityClient.getAnonymousId("john@example.com"))
                .isInstanceOf(RuntimeException.class);  // Espera RuntimeException, no el fallback
    }
}