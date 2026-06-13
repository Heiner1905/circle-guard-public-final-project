package com.circleguard.notification.service;

import com.circleguard.notification.NotificationApplication;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.circleguard.notification.service.AuditLogService;
import com.circleguard.notification.service.TemplateService;


@SpringBootTest(
        classes = NotificationApplication.class,
        properties = {
                "auth.api.url=http://auth",
                "resilience4j.circuitbreaker.instances.authService.slidingWindowSize=10",
                "resilience4j.circuitbreaker.instances.authService.minimumNumberOfCalls=5",
                "resilience4j.circuitbreaker.instances.authService.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.authService.waitDurationInOpenState=10s",
                "resilience4j.circuitbreaker.instances.authService.permittedNumberOfCallsInHalfOpenState=3"
        }
)
class AuthUserClientCircuitBreakerTest {

    @Autowired
    private AuthUserClient authUserClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private TemplateService templateService;

    @Test
    void opensCircuitAfterRepeatedAuthFailures() {
        when(restTemplate.getForObject(
                "http://auth/api/v1/users/permissions/alert:receive_priority",
                List.class))
                .thenThrow(new IllegalStateException("auth down"));

        for (int i = 0; i < 5; i++) {
            List<?> admins = authUserClient.findUsersWithPermission("alert:receive_priority");
            assertThat(admins).isEmpty();
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("authService");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}