package com.circleguard.dashboard.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = PromotionClientTest.TestApplication.class,
        properties = {
                "circleguard.promotion-service.url=http://promotion",
                "resilience4j.circuitbreaker.instances.promotionService.slidingWindowSize=10",
                "resilience4j.circuitbreaker.instances.promotionService.minimumNumberOfCalls=5",
                "resilience4j.circuitbreaker.instances.promotionService.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.promotionService.waitDurationInOpenState=10s",
                "resilience4j.circuitbreaker.instances.promotionService.permittedNumberOfCallsInHalfOpenState=3"
        }
)
class PromotionClientTest {

    @Autowired
    private PromotionClient promotionClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Reset the circuit breaker before each test
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("promotionService");
        circuitBreaker.reset();
        reset(restTemplate);
    }

    @Test
    void getHealthStats_ShouldReturnStatsWhenServiceAvailable() {
        // Arrange
        Map<String, Object> expectedStats = new HashMap<>();
        expectedStats.put("totalGreen", 1500);
        expectedStats.put("totalExposed", 45);
        expectedStats.put("totalRed", 10);

        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        )).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = promotionClient.getHealthStats();

        // Assert
        assertThat(result).isEqualTo(expectedStats);
        assertThat(result).containsEntry("totalGreen", 1500);
        assertThat(result).containsEntry("totalExposed", 45);
        verify(restTemplate, times(1)).getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        );
    }

    @Test
    void getHealthStats_ShouldReturnFallbackWhenServiceUnavailable() {
        // Arrange
        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        )).thenThrow(new RestClientException("Connection refused"));

        // Act
        Map<String, Object> result = promotionClient.getHealthStats();

        // Assert
        assertThat(result).containsEntry("error", "Service unavailable");
        assertThat(result).containsKey("timestamp");
        assertThat(result.get("timestamp")).isNotNull();
        verify(restTemplate, times(1)).getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        );
    }

    @Test
    void getHealthStatsByDepartment_ShouldReturnStatsWhenServiceAvailable() {
        // Arrange
        String department = "Computer Science";
        Map<String, Object> expectedStats = new HashMap<>();
        expectedStats.put("department", department);
        expectedStats.put("studentCount", 1200);
        expectedStats.put("healthStatus", "GREEN");

        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats/department/" + department,
                Map.class
        )).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = promotionClient.getHealthStatsByDepartment(department);

        // Assert
        assertThat(result).isEqualTo(expectedStats);
        assertThat(result).containsEntry("department", department);
        assertThat(result).containsEntry("studentCount", 1200);
        verify(restTemplate, times(1)).getForObject(
                "http://promotion/api/v1/health-status/stats/department/" + department,
                Map.class
        );
    }

    @Test
    void getHealthStatsByDepartment_ShouldReturnFallbackWhenServiceUnavailable() {
        // Arrange
        String department = "Physics";
        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats/department/" + department,
                Map.class
        )).thenThrow(new RestClientException("Service timeout"));

        // Act
        Map<String, Object> result = promotionClient.getHealthStatsByDepartment(department);

        // Assert
        assertThat(result).containsEntry("error", "Service unavailable");
        assertThat(result).containsEntry("department", department);
        assertThat(result).containsKey("timestamp");
        assertThat(result.get("timestamp")).isNotNull();
        verify(restTemplate, times(1)).getForObject(
                "http://promotion/api/v1/health-status/stats/department/" + department,
                Map.class
        );
    }

    @Test
    void opensCircuitAfterRepeatedPromotionFailures() {
        // Arrange
        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        )).thenThrow(new RestClientException("promotion down"));

        // Act - Make enough calls to open the circuit
        for (int i = 0; i < 5; i++) {
            Map<String, Object> response = promotionClient.getHealthStats();
            assertThat(response).containsEntry("error", "Service unavailable");
        }

        // Assert
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("promotionService");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void circuitBreakerShouldAllowFallbackWhenOpen() {
        // Arrange - First cause circuit to open
        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        )).thenThrow(new RestClientException("Service down"));

        for (int i = 0; i < 5; i++) {
            promotionClient.getHealthStats();
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("promotionService");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Act - Call again when circuit is open
        Map<String, Object> result = promotionClient.getHealthStats();

        // Assert - Should still return fallback
        assertThat(result).containsEntry("error", "Service unavailable");
        assertThat(result).containsKey("timestamp");
    }

    @Test
    void getHealthStats_ShouldHandleNullResponseFromService() {
        // Arrange
        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats",
                Map.class
        )).thenReturn(null);

        // Act
        Map<String, Object> result = promotionClient.getHealthStats();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void getHealthStatsByDepartment_ShouldHandleEmptyDepartmentName() {
        // Arrange
        String department = "";
        Map<String, Object> expectedStats = new HashMap<>();
        expectedStats.put("department", department);
        expectedStats.put("healthStatus", "UNKNOWN");

        when(restTemplate.getForObject(
                "http://promotion/api/v1/health-status/stats/department/" + department,
                Map.class
        )).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = promotionClient.getHealthStatsByDepartment(department);

        // Assert
        assertThat(result).isEqualTo(expectedStats);
        assertThat(result).containsEntry("department", "");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import(PromotionClient.class)
    static class TestApplication {
        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }
    }
}