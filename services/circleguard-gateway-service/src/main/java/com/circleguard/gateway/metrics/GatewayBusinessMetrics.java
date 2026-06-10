package com.circleguard.gateway.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business metrics for the gateway-service: QR validations broken down by
 * result (valid / invalid). Production code calls
 * {@link #recordValidation(boolean)} on each QR check.
 */
@Component
public class GatewayBusinessMetrics {

    public static final String METRIC_NAME = "circleguard_qr_validations_total";

    private final MeterRegistry registry;

    public GatewayBusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordValidation(boolean valid) {
        registry.counter(METRIC_NAME, "result", valid ? "valid" : "invalid", "service", "gateway")
                .increment();
    }
}
