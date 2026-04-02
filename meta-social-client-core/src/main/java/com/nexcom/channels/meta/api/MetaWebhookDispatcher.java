package com.nexcom.channels.meta.api;

import com.nexcom.channels.meta.model.webhook.MetaWebhookEvent;
import reactor.core.publisher.Mono;

/**
 * Routes parsed webhook events to registered handlers.
 */
public interface MetaWebhookDispatcher {

    Mono<Void> dispatch(MetaWebhookEvent event);
}
