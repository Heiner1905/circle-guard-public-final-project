package com.circleguard.dashboard.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class PromotionClient {

    private final RestTemplate restTemplate;

    @Value("${circleguard.promotion-service.url:http://localhost:8088}")
    private String promotionServiceUrl;

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "promotionService", fallbackMethod = "healthStatsFallback")
    public Map<String, Object> getHealthStats() {
        return restTemplate.getForObject(
                promotionServiceUrl + "/api/v1/health-status/stats",
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "promotionService", fallbackMethod = "departmentHealthStatsFallback")
    public Map<String, Object> getHealthStatsByDepartment(String department) {
        return restTemplate.getForObject(
                promotionServiceUrl + "/api/v1/health-status/stats/department/" + department,
                Map.class
        );
    }

    private Map<String, Object> healthStatsFallback(Throwable throwable) {
        log.error("Failed to fetch health stats from promotion-service", throwable);
        return Map.of("error", "Service unavailable", "timestamp", new Date());
    }

    private Map<String, Object> departmentHealthStatsFallback(String department, Throwable throwable) {
        log.error("Failed to fetch department stats from promotion-service", throwable);
        return Map.of("error", "Service unavailable", "department", department, "timestamp", new Date());
    }
}
