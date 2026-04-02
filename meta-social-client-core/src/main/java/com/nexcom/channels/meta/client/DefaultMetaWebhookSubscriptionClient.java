package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.api.MetaWebhookSubscriptionClient;
import com.nexcom.channels.meta.model.MetaApiContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link MetaWebhookSubscriptionClient}.
 */
public class DefaultMetaWebhookSubscriptionClient implements MetaWebhookSubscriptionClient {

    private final MetaGraphApiClient graphApiClient;
    private final MetaEndpointResolver endpointResolver;
    private final MetaApiContext context;

    public DefaultMetaWebhookSubscriptionClient(MetaGraphApiClient graphApiClient,
                                                 MetaEndpointResolver endpointResolver,
                                                 MetaApiContext context) {
        this.graphApiClient = graphApiClient;
        this.endpointResolver = endpointResolver;
        this.context = context;
    }

    @Override
    public Mono<Void> subscribe(List<String> fields) {
        String url = endpointResolver.subscribedAppsUrl(
                context.authPath(), context.apiVersion(), context.scopedId());
        Map<String, Object> body = Map.of("subscribed_fields", String.join(",", fields));
        return graphApiClient.post(url, context.accessToken(), context.appSecret(), body, Void.class).then();
    }

    @Override
    public Mono<Void> unsubscribe() {
        String url = endpointResolver.subscribedAppsUrl(
                context.authPath(), context.apiVersion(), context.scopedId());
        return graphApiClient.delete(url, context.accessToken(), context.appSecret());
    }
}
