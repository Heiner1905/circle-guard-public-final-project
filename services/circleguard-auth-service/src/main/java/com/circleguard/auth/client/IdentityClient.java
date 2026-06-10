package com.circleguard.auth.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class IdentityClient {

    private final RestTemplate restTemplate;
    private final String identityServiceUrl;

    public IdentityClient(
            RestTemplate restTemplate,
            @Value("${circleguard.identity-service.url:http://circleguard-identity-service:8083}") String identityServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.identityServiceUrl = identityServiceUrl;
    }

    @CircuitBreaker(name = "identityService", fallbackMethod = "getAnonymousIdFallback")
    public UUID getAnonymousId(String realIdentity) {
        Map<String, String> request = Map.of("realIdentity", realIdentity);
        @SuppressWarnings("rawtypes")
        Map response = restTemplate.postForObject(
                identityServiceUrl + "/api/v1/identities/map", request, Map.class);
        return UUID.fromString(response.get("anonymousId").toString());
    }

    public UUID getAnonymousIdFallback(String realIdentity, Throwable throwable) {
        throw new IdentityServiceUnavailableException(
                "Identity service unavailable for identity '" + realIdentity + "'", throwable);
    }
}
