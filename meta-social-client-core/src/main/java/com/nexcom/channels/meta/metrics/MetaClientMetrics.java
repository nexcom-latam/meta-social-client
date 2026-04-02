package com.nexcom.channels.meta.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Micrometer metrics for the Meta Graph API client.
 * Tracks send success/error and webhook event counts.
 */
public class MetaClientMetrics {

    private final MeterRegistry registry;

    public MetaClientMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSendSuccess(String channel) {
        Counter.builder("meta.client.send.success")
                .tag("channel", channel)
                .register(registry)
                .increment();
    }

    public void recordSendError(String channel, String errorType) {
        Counter.builder("meta.client.send.error")
                .tag("channel", channel)
                .tag("error_type", errorType)
                .register(registry)
                .increment();
    }

    public void recordWebhookReceived(String channel, String eventType) {
        Counter.builder("meta.webhook.received")
                .tag("channel", channel)
                .tag("event_type", eventType)
                .register(registry)
                .increment();
    }

    public void recordWebhookSignatureFailure() {
        Counter.builder("meta.webhook.signature.failure")
                .register(registry)
                .increment();
    }
}
