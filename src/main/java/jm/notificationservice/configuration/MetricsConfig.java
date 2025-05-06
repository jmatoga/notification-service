package jm.notificationservice.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter notificationSentCounter(MeterRegistry meterRegistry) {
        return meterRegistry.counter("notifications_tries_total");
    }

    @Bean
    public Counter notificationCreatedCounter(MeterRegistry meterRegistry) {
        return meterRegistry.counter("notifications_amount_total");
    }

    @Bean
    public Counter notificationFailedCounter(MeterRegistry meterRegistry) {
        return meterRegistry.counter("notifications_failed_total");
    }

    @Bean
    public Timer notificationProcessingTimer(MeterRegistry meterRegistry) {
        return meterRegistry.timer("notifications_processing_duration_seconds");
    }
}
