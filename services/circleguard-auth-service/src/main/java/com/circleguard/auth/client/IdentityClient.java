package com.circleguard.auth.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class IdentityClient {
    private final RestTemplate restTemplate;

    @Value("${circleguard.identity-service.url:http://localhost:8083}")
    private String identityServiceUrl;

    @CircuitBreaker(name = "identityService", fallbackMethod = "identityFallback")
    public UUID getAnonymousId(String realIdentity) {
        Map<String, String> request = Map.of("realIdentity", realIdentity);
        Map response = restTemplate.postForObject(identityServiceUrl + "/api/v1/identities/map", request, Map.class);
        return UUID.fromString(response.get("anonymousId").toString());
    }

    private UUID identityFallback(String realIdentity, Throwable throwable) {
        throw new IdentityServiceUnavailableException("identity-service is unavailable", throwable);
    }
}
