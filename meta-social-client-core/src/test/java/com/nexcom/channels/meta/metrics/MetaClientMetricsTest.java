package com.nexcom.channels.meta.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaClientMetricsTest {

    private MeterRegistry registry;
    private MetaClientMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new MetaClientMetrics(registry);
    }

    @Test
    void recordSendSuccess_incrementsCounter() {
        metrics.recordSendSuccess("INSTAGRAM");
        metrics.recordSendSuccess("INSTAGRAM");

        double count = registry.counter("meta.client.send.success", "channel", "INSTAGRAM").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void recordSendError_incrementsCounterWithTags() {
        metrics.recordSendError("FACEBOOK", "rate_limit");

        double count = registry.counter("meta.client.send.error", "channel", "FACEBOOK", "error_type", "rate_limit").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordWebhookReceived_incrementsCounter() {
        metrics.recordWebhookReceived("INSTAGRAM", "InboundMessage");

        double count = registry.counter("meta.webhook.received", "channel", "INSTAGRAM", "event_type", "InboundMessage").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void recordWebhookSignatureFailure_incrementsCounter() {
        metrics.recordWebhookSignatureFailure();

        double count = registry.counter("meta.webhook.signature.failure").count();
        assertThat(count).isEqualTo(1.0);
    }
}
