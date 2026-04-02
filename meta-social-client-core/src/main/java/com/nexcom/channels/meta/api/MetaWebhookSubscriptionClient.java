package com.nexcom.channels.meta.api;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Client for managing webhook subscriptions via the Graph API.
 * Maps to POST/DELETE /{id}/subscribed_apps.
 */
public interface MetaWebhookSubscriptionClient {

    /**
     * Subscribes the app to receive webhooks for the given fields.
     * Maps to POST /{page-or-ig-user-id}/subscribed_apps
     *
     * @param fields webhook fields to subscribe to (e.g., "messages", "messaging_seen")
     */
    Mono<Void> subscribe(List<String> fields);

    /**
     * Unsubscribes the app from webhooks.
     * Maps to DELETE /{page-or-ig-user-id}/subscribed_apps
     */
    Mono<Void> unsubscribe();
}
