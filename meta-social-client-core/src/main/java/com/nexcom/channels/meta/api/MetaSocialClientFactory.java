package com.nexcom.channels.meta.api;

import com.nexcom.channels.meta.model.MetaApiContext;

/**
 * Factory for creating channel-specific clients.
 * Inspired by WhatsappApiFactory — encapsulates HTTP client setup,
 * token injection, and version resolution behind a single entry point.
 */
public interface MetaSocialClientFactory {

    /**
     * Creates a messaging client bound to a specific tenant and channel.
     */
    MetaMessagingClient create(MetaApiContext context);

    /**
     * Creates a media client bound to a specific tenant and channel.
     */
    MetaMediaClient createMediaClient(MetaApiContext context);

    /**
     * Creates a webhook subscription client bound to a specific tenant and channel.
     */
    MetaWebhookSubscriptionClient createWebhookSubscriptionClient(MetaApiContext context);
}
