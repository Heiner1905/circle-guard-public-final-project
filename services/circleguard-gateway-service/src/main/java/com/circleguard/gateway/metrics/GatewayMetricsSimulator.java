package com.circleguard.gateway.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Random;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "circleguard.metrics.simulation.enabled", havingValue = "true")
public class GatewayMetricsSimulator {

    private static final Logger log = LoggerFactory.getLogger(GatewayMetricsSimulator.class);

    private final GatewayBusinessMetrics metrics;
    private final Random rng = new Random();

    public GatewayMetricsSimulator(GatewayBusinessMetrics metrics) {
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${circleguard.metrics.simulation.interval-ms:30000}")
    public void tick() {
        int valid = 5 + rng.nextInt(15);
        int invalid = rng.nextInt(3);
        for (int i = 0; i < valid; i++) {
            metrics.recordValidation(true);
        }
        for (int i = 0; i < invalid; i++) {
            metrics.recordValidation(false);
        }
        log.debug("Simulator: +{} valid / +{} invalid QR validations", valid, invalid);
    }
}
