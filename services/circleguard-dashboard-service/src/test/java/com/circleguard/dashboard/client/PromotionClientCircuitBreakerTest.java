package com.circleguard.dashboard.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = PromotionClientCircuitBreakerTest.TestApplication.class,
        properties = {
                "circleguard.promotion-service.url=http://promotion",
                "resilience4j.circuitbreaker.instances.promotionService.slidingWindowSize=10",
                "resilience4j.circuitbreaker.instances.promotionService.minimumNumberOfCalls=5",
                "resilience4j.circuitbreaker.instances.promotionService.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.promotionService.waitDurationInOpenState=10s",
                "resilience4j.circuitbreaker.instances.promotionService.permittedNumberOfCallsInHalfOpenState=3"
        }
)
class PromotionClientCircuitBreakerTest {

    @Autowired
    private PromotionClient promotionClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void opensCircuitAfterRepeatedPromotionFailures() {
        when(restTemplate.getForObject("http://promotion/api/v1/health-status/stats", Map.class))
                .thenThrow(new IllegalStateException("promotion down"));

        for (int i = 0; i < 5; i++) {
            Map<String, Object> response = promotionClient.getHealthStats();
            assertThat(response).containsEntry("error", "Service unavailable");
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("promotionService");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
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
