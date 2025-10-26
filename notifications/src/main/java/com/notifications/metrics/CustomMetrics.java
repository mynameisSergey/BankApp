package com.notifications.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    public void incrementFailureNotifications(String username) {
        meterRegistry.counter("notifications_failure_total", "username", username).increment();
    }
}
