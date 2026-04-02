package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nexcom.channels.meta.model.GraphApiPage;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive HTTP abstraction over the Meta Graph API.
 * Implementations handle token injection, appsecret_proof, error mapping, and serialization.
 */
public interface MetaGraphApiClient {

    /**
     * POST JSON to the given Graph API URL.
     */
    <T> Mono<T> post(String url, String accessToken, String appSecret, Object body, Class<T> responseType);

    /**
     * POST multipart form data to the given Graph API URL.
     */
    <T> Mono<T> postMultipart(String url, String accessToken, String appSecret, MultipartBodyBuilder body, Class<T> responseType);

    /**
     * GET from the given Graph API URL.
     */
    <T> Mono<T> get(String url, String accessToken, String appSecret, Class<T> responseType);

    /**
     * GET a paginated response from the given Graph API URL.
     */
    <T> Mono<GraphApiPage<T>> getPaged(String url, String accessToken, String appSecret, TypeReference<GraphApiPage<T>> typeRef);

    /**
     * DELETE at the given Graph API URL.
     */
    Mono<Void> delete(String url, String accessToken, String appSecret);

    /**
     * Auto-paginate: GET all pages and emit each element as a Flux.
     * Follows {@code paging.next} links until exhausted.
     * This is additive — use {@link #getPaged} for manual single-page control.
     */
    default <T> Flux<T> getAll(String url, String accessToken, String appSecret, TypeReference<GraphApiPage<T>> typeRef) {
        return getPaged(url, accessToken, appSecret, typeRef)
                .expand(page -> {
                    if (page.hasNext()) {
                        return getPaged(page.nextUrl(), accessToken, appSecret, typeRef);
                    }
                    return Mono.empty();
                })
                .flatMapIterable(GraphApiPage::data);
    }
}
