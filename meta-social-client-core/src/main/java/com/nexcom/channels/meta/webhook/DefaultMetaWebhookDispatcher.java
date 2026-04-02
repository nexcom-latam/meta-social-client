package com.nexcom.channels.meta.webhook;

import com.nexcom.channels.meta.api.MetaWebhookDispatcher;
import com.nexcom.channels.meta.api.MetaWebhookEventHandler;
import com.nexcom.channels.meta.model.webhook.MetaWebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Routes parsed webhook events to all registered handlers.
 * One bad handler never blocks others.
 */
public class DefaultMetaWebhookDispatcher implements MetaWebhookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DefaultMetaWebhookDispatcher.class);

    private final List<MetaWebhookEventHandler<?>> handlers;

    public DefaultMetaWebhookDispatcher(List<MetaWebhookEventHandler<?>> handlers) {
        this.handlers = handlers != null ? handlers : List.of();
    }

    @Override
    public Mono<Void> dispatch(MetaWebhookEvent event) {
        List<MetaWebhookEventHandler<?>> matching = handlers.stream()
                .filter(h -> h.supports(event))
                .toList();

        if (matching.isEmpty()) {
            log.warn("No handlers registered for event type={} mid={}",
                    event.getClass().getSimpleName(), event.mid());
            return Mono.empty();
        }

        return Flux.fromIterable(matching)
                .flatMap(handler -> invokeHandler(handler, event))
                .then();
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> invokeHandler(MetaWebhookEventHandler<?> handler, MetaWebhookEvent event) {
        try {
            var typedHandler = (MetaWebhookEventHandler<MetaWebhookEvent>) handler;
            return typedHandler.handle(event)
                    .onErrorResume(err -> {
                        log.error("Handler {} failed for mid={}", handler.getClass().getSimpleName(), event.mid(), err);
                        return Mono.empty();
                    });
        } catch (ClassCastException e) {
            log.error("Type mismatch dispatching to handler {}", handler.getClass().getSimpleName(), e);
            return Mono.empty();
        }
    }
}
