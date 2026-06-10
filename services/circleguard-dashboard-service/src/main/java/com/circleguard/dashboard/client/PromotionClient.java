package com.circleguard.dashboard.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class PromotionClient {

    private final RestTemplate restTemplate;
    private final String promotionServiceUrl;

    public PromotionClient(
            RestTemplate restTemplate,
            @Value("${circleguard.promotion-service.url:http://circleguard-promotion-service:8088}") String promotionServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.promotionServiceUrl = promotionServiceUrl;
    }

    @SuppressWarnings("rawtypes")
    @CircuitBreaker(name = "promotionService", fallbackMethod = "getHealthStatsFallback")
    public Map<String, Object> getHealthStats() {
        Map response = restTemplate.getForObject(
                promotionServiceUrl + "/api/v1/health-status/stats", Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = response;
        return typed;
    }

    public Map<String, Object> getHealthStatsFallback(Throwable throwable) {
        log.warn("Circuit breaker fallback for promotion-service health stats", throwable);
        return Map.of("error", "Service unavailable", "timestamp", new Date());
    }

    @SuppressWarnings("rawtypes")
    @CircuitBreaker(name = "promotionService", fallbackMethod = "getHealthStatsByDepartmentFallback")
    public Map<String, Object> getHealthStatsByDepartment(String department) {
        Map response = restTemplate.getForObject(
                promotionServiceUrl + "/api/v1/health-status/stats/department/" + department, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = response;
        return typed;
    }

    public Map<String, Object> getHealthStatsByDepartmentFallback(String department, Throwable throwable) {
        log.warn("Circuit breaker fallback for promotion-service department {}", department, throwable);
        return Map.of("error", "Service unavailable", "department", department, "timestamp", new Date());
    }
}
