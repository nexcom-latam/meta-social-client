package com.nexcom.channels.meta.api;

import com.nexcom.channels.meta.model.TokenExchangeResponse;
import reactor.core.publisher.Mono;

/**
 * Thin HTTP client for Meta's token exchange and refresh endpoints.
 * This is a client-level concern (HTTP calls to Meta), not persistence.
 * The consuming application calls these explicitly and stores results
 * however it sees fit.
 */
public interface MetaTokenExchangeClient {

    /**
     * Exchanges a short-lived token for a long-lived token (60-day).
     * <ul>
     *   <li>Instagram Login: GET /access_token?grant_type=ig_exchange_token</li>
     *   <li>Facebook Page: GET /oauth/access_token?grant_type=fb_exchange_token</li>
     * </ul>
     */
    Mono<TokenExchangeResponse> exchangeForLongLived(String shortLivedToken);

    /**
     * Refreshes a long-lived token before expiry.
     * <ul>
     *   <li>Instagram Login: GET /refresh_access_token?grant_type=ig_refresh_token</li>
     *   <li>Facebook Page: re-exchange the long-lived token (same as exchange)</li>
     * </ul>
     */
    Mono<TokenExchangeResponse> refresh(String longLivedToken);
}
