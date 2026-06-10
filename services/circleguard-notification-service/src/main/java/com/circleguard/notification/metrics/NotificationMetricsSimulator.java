package com.circleguard.notification.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Random;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "circleguard.metrics.simulation.enabled", havingValue = "true")
public class NotificationMetricsSimulator {

    private static final Logger log = LoggerFactory.getLogger(NotificationMetricsSimulator.class);
    private static final List<String> CHANNELS = List.of("email", "sms", "push");

    private final NotificationBusinessMetrics metrics;
    private final Random rng = new Random();

    public NotificationMetricsSimulator(NotificationBusinessMetrics metrics) {
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${circleguard.metrics.simulation.interval-ms:45000}")
    public void tick() {
        for (String channel : CHANNELS) {
            int count = rng.nextInt(4);
            for (int i = 0; i < count; i++) {
                metrics.increment(channel);
            }
            log.debug("Simulator: +{} notifications on channel {}", count, channel);
        }
    }
}
