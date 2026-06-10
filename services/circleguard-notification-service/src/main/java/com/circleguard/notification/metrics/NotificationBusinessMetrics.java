package com.circleguard.notification.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business metrics for the notification-service. The counter is tagged by
 * channel (email / sms / push) so dashboards can break down delivery by lane.
 * <p>
 * Production dispatchers should obtain the counter via
 * {@link #increment(String)} or directly via the helper meter id.
 */
@Component
public class NotificationBusinessMetrics {

    public static final String METRIC_NAME = "circleguard_notifications_sent_total";

    private final MeterRegistry registry;

    public NotificationBusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /** Increments the per-channel counter. */
    public void increment(String channel) {
        registry.counter(METRIC_NAME, "channel", channel, "service", "notification")
                .increment();
    }
}
