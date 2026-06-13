package com.circleguard.promotion.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Business metrics exposed via Micrometer / Prometheus for the promotion-service.
 * Production code increments the counter on every status promotion and updates
 * the gauge when fences are created/destroyed.
 */
@Configuration
public class PromotionBusinessMetrics {

    @Bean
    public Counter promotionsCounter(MeterRegistry registry) {
        return Counter.builder("circleguard_promotions_total")
                .description("Total health-status promotions executed")
                .tag("service", "promotion")
                .register(registry);
    }

    @Bean
    public AtomicInteger activeFencesGauge(MeterRegistry registry) {
        AtomicInteger holder = new AtomicInteger(0);
        registry.gauge(
                "circleguard_active_fences",
                List.of(Tag.of("service", "promotion")),
                holder,
                AtomicInteger::doubleValue);
        return holder;
    }
}
