package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.api.MetaTokenExchangeClient;
import com.nexcom.channels.meta.model.InstagramAuthPath;
import com.nexcom.channels.meta.model.MetaApiContext;
import com.nexcom.channels.meta.model.TokenExchangeResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Default implementation of {@link MetaTokenExchangeClient}.
 * Makes HTTP GET calls to Meta's token exchange / refresh endpoints.
 */
public class DefaultMetaTokenExchangeClient implements MetaTokenExchangeClient {

    private final MetaGraphApiClient graphApiClient;
    private final MetaEndpointResolver endpointResolver;
    private final MetaApiContext context;

    public DefaultMetaTokenExchangeClient(MetaGraphApiClient graphApiClient,
                                           MetaEndpointResolver endpointResolver,
                                           MetaApiContext context) {
        this.graphApiClient = graphApiClient;
        this.endpointResolver = endpointResolver;
        this.context = context;
    }

    @Override
    public Mono<TokenExchangeResponse> exchangeForLongLived(String shortLivedToken) {
        String baseUrl = endpointResolver.tokenExchangeBaseUrl(context.authPath(), context.apiVersion());

        String url;
        if (context.authPath() == InstagramAuthPath.INSTAGRAM_LOGIN) {
            url = UriComponentsBuilder.fromUriString(baseUrl + "/access_token")
                    .queryParam("grant_type", "ig_exchange_token")
                    .queryParam("client_secret", context.appSecret())
                    .queryParam("access_token", shortLivedToken)
                    .toUriString();
        } else {
            url = UriComponentsBuilder.fromUriString(baseUrl + "/oauth/access_token")
                    .queryParam("grant_type", "fb_exchange_token")
                    .queryParam("client_secret", context.appSecret())
                    .queryParam("fb_exchange_token", shortLivedToken)
                    .toUriString();
        }

        return graphApiClient.get(url, shortLivedToken, context.appSecret(), TokenExchangeResponse.class);
    }

    @Override
    public Mono<TokenExchangeResponse> refresh(String longLivedToken) {
        String baseUrl = endpointResolver.tokenExchangeBaseUrl(context.authPath(), context.apiVersion());

        if (context.authPath() == InstagramAuthPath.INSTAGRAM_LOGIN) {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/refresh_access_token")
                    .queryParam("grant_type", "ig_refresh_token")
                    .queryParam("access_token", longLivedToken)
                    .toUriString();
            return graphApiClient.get(url, longLivedToken, context.appSecret(), TokenExchangeResponse.class);
        } else {
            // Facebook: re-exchange the long-lived token
            return exchangeForLongLived(longLivedToken);
        }
    }
}
