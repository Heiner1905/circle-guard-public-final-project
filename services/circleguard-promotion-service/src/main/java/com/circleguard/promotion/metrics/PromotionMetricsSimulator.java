package com.circleguard.promotion.metrics;

import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates synthetic promotion / fence activity so Grafana dashboards have
 * something to plot during demos. Disabled by default; opt-in with the
 * <code>circleguard.metrics.simulation.enabled=true</code> property.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "circleguard.metrics.simulation.enabled", havingValue = "true")
public class PromotionMetricsSimulator {

    private static final Logger log = LoggerFactory.getLogger(PromotionMetricsSimulator.class);

    private final Counter promotionsCounter;
    private final AtomicInteger activeFencesGauge;
    private final Random rng = new Random();

    public PromotionMetricsSimulator(Counter promotionsCounter, AtomicInteger activeFencesGauge) {
        this.promotionsCounter = promotionsCounter;
        this.activeFencesGauge = activeFencesGauge;
    }

    @Scheduled(fixedDelayString = "${circleguard.metrics.simulation.interval-ms:45000}")
    public void tick() {
        int promotions = rng.nextInt(5) + 1;
        for (int i = 0; i < promotions; i++) {
            promotionsCounter.increment();
        }
        int fences = 10 + rng.nextInt(20);
        activeFencesGauge.set(fences);
        log.debug("Simulator: +{} promotions, active fences={}", promotions, fences);
    }
}
