package com.exchange.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    public void incrementCurrencyUpdate() {
        meterRegistry.counter("currency_update_total").increment();
    }
}
