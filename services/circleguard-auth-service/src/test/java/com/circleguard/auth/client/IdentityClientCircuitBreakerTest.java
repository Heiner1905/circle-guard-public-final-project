package com.circleguard.auth.client;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = IdentityClientCircuitBreakerTest.TestApplication.class,
        properties = {
                "circleguard.identity-service.url=http://identity",
                "resilience4j.circuitbreaker.instances.identityService.slidingWindowSize=10",
                "resilience4j.circuitbreaker.instances.identityService.minimumNumberOfCalls=5",
                "resilience4j.circuitbreaker.instances.identityService.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.identityService.waitDurationInOpenState=10s",
                "resilience4j.circuitbreaker.instances.identityService.permittedNumberOfCallsInHalfOpenState=3"
        }
)
class IdentityClientCircuitBreakerTest {

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void opensCircuitAfterRepeatedIdentityFailures() {
        when(restTemplate.postForObject(eq("http://identity/api/v1/identities/map"), any(), eq(java.util.Map.class)))
                .thenThrow(new IllegalStateException("identity down"));

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> identityClient.getAnonymousId("karold@university.edu"))
                    .isInstanceOf(IdentityServiceUnavailableException.class);
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("identityService");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import(IdentityClient.class)
    static class TestApplication {
        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }
    }
}
