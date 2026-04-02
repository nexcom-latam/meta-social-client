package com.nexcom.channels.meta.api;

import com.nexcom.channels.meta.model.webhook.MetaWebhookEvent;
import reactor.core.publisher.Mono;

/**
 * SPI for handling specific webhook event types.
 * Register as a Spring bean; the dispatcher routes matching events.
 *
 * @param <T> the specific MetaWebhookEvent subtype this handler supports
 */
public interface MetaWebhookEventHandler<T extends MetaWebhookEvent> {

    boolean supports(MetaWebhookEvent event);

    Mono<Void> handle(T event);

    Class<T> eventType();
}
