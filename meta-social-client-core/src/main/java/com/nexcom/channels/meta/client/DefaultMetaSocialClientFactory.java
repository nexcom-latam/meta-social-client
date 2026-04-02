package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.api.MetaMediaClient;
import com.nexcom.channels.meta.api.MetaMessagingClient;
import com.nexcom.channels.meta.api.MetaSocialClientFactory;
import com.nexcom.channels.meta.api.MetaWebhookSubscriptionClient;
import com.nexcom.channels.meta.model.MetaApiContext;

/**
 * Default factory implementation. Creates bound client instances
 * using the shared {@link MetaGraphApiClient} and {@link MetaEndpointResolver}.
 */
public class DefaultMetaSocialClientFactory implements MetaSocialClientFactory {

    private final MetaGraphApiClient graphApiClient;
    private final MetaEndpointResolver endpointResolver;

    public DefaultMetaSocialClientFactory(MetaGraphApiClient graphApiClient,
                                          MetaEndpointResolver endpointResolver) {
        this.graphApiClient = graphApiClient;
        this.endpointResolver = endpointResolver;
    }

    @Override
    public MetaMessagingClient create(MetaApiContext context) {
        return new DefaultMetaMessagingClient(graphApiClient, endpointResolver, context);
    }

    @Override
    public MetaMediaClient createMediaClient(MetaApiContext context) {
        return new DefaultMetaMediaClient(graphApiClient, endpointResolver, context);
    }

    @Override
    public MetaWebhookSubscriptionClient createWebhookSubscriptionClient(MetaApiContext context) {
        return new DefaultMetaWebhookSubscriptionClient(graphApiClient, endpointResolver, context);
    }
}
