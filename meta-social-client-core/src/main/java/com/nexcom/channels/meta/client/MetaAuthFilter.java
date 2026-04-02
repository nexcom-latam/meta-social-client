package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.auth.HmacUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * WebClient ExchangeFilterFunction that injects:
 * <ul>
 *   <li>Authorization: Bearer {token}</li>
 *   <li>appsecret_proof query parameter (when app secret is provided)</li>
 * </ul>
 */
public class MetaAuthFilter implements ExchangeFilterFunction {

    /** Request attribute key for the access token. */
    public static final String TOKEN_ATTRIBUTE = "meta.accessToken";

    /** Request attribute key for the app secret (used to compute appsecret_proof). */
    public static final String APP_SECRET_ATTRIBUTE = "meta.appSecret";

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String token = (String) request.attributes().get(TOKEN_ATTRIBUTE);
        if (token == null || token.isBlank()) {
            return next.exchange(request);
        }

        String appSecret = (String) request.attributes().get(APP_SECRET_ATTRIBUTE);
        URI uri = request.url();

        // Append appsecret_proof if app secret is available
        if (appSecret != null && !appSecret.isBlank()) {
            String proof = HmacUtils.appSecretProof(token, appSecret);
            String original = uri.toString();
            String separator = original.contains("?") ? "&" : "?";
            uri = URI.create(original + separator + "appsecret_proof=" + proof);
        }

        ClientRequest authenticated = ClientRequest.from(request)
                .url(uri)
                .headers(h -> h.setBearerAuth(token))
                .build();
        return next.exchange(authenticated);
    }
}
