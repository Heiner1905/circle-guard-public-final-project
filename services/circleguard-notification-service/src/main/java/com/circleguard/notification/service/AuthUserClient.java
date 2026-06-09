package com.circleguard.notification.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthUserClient {
    private final RestTemplate restTemplate;
    private final String authApiUrl;

    public AuthUserClient(
            RestTemplate restTemplate,
            @Value("${auth.api.url:http://circleguard-auth-service:8180}") String authApiUrl
    ) {
        this.restTemplate = restTemplate;
        this.authApiUrl = authApiUrl;
    }

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "authService", fallbackMethod = "findUsersWithPermissionFallback")
    public List<Map<String, String>> findUsersWithPermission(String permission) {
        String url = authApiUrl + "/api/v1/users/permissions/" + permission;
        return restTemplate.getForObject(url, List.class);
    }

    public List<Map<String, String>> findUsersWithPermissionFallback(String permission, Throwable throwable) {
        log.warn("Circuit breaker fallback for auth-service permission lookup: {}", permission, throwable);
        return List.of();
    }
}
