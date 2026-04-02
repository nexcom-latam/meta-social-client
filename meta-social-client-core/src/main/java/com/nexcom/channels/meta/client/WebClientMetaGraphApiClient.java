package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.model.GraphApiPage;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.function.Consumer;
import java.util.Map;

/**
 * Spring WebClient implementation of {@link MetaGraphApiClient}.
 * Passes token and app secret via request attributes for {@link MetaAuthFilter}.
 * Applies retry logic for rate-limit errors.
 */
public class WebClientMetaGraphApiClient implements MetaGraphApiClient {

    private final WebClient webClient;
    private final MetaApiErrorHandler errorHandler;
    private final ObjectMapper objectMapper;
    private final Retry retry;

    public WebClientMetaGraphApiClient(WebClient webClient, MetaApiErrorHandler errorHandler) {
        this(webClient, errorHandler, new ObjectMapper(), MetaRetryFilter.defaultRetry());
    }

    public WebClientMetaGraphApiClient(WebClient webClient, MetaApiErrorHandler errorHandler,
                                        ObjectMapper objectMapper, Retry retry) {
        this.webClient = webClient;
        this.errorHandler = errorHandler;
        this.objectMapper = objectMapper;
        this.retry = retry;
    }

    @Override
    public <T> Mono<T> post(String url, String accessToken, String appSecret, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(authAttributes(accessToken, appSecret))
                .bodyValue(body)
                .exchangeToMono(response -> handleResponse(response, responseType))
                .retryWhen(retry);
    }

    @Override
    public <T> Mono<T> postMultipart(String url, String accessToken, String appSecret, MultipartBodyBuilder body, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .attributes(authAttributes(accessToken, appSecret))
                .body(BodyInserters.fromMultipartData(body.build()))
                .exchangeToMono(response -> handleResponse(response, responseType))
                .retryWhen(retry);
    }

    @Override
    public <T> Mono<T> get(String url, String accessToken, String appSecret, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .attributes(authAttributes(accessToken, appSecret))
                .exchangeToMono(response -> handleResponse(response, responseType))
                .retryWhen(retry);
    }

    @Override
    public <T> Mono<GraphApiPage<T>> getPaged(String url, String accessToken, String appSecret, TypeReference<GraphApiPage<T>> typeRef) {
        return webClient.get()
                .uri(url)
                .attributes(authAttributes(accessToken, appSecret))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        return objectMapper.readValue(body, typeRef);
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to deserialize paged response", e);
                                    }
                                });
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(
                                    errorHandler.handleErrorResponse(errorBody, response.statusCode().value())));
                })
                .retryWhen(retry);
    }

    @Override
    public Mono<Void> delete(String url, String accessToken, String appSecret) {
        return webClient.delete()
                .uri(url)
                .attributes(authAttributes(accessToken, appSecret))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.releaseBody();
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(
                                    errorHandler.handleErrorResponse(errorBody, response.statusCode().value())));
                })
                .retryWhen(retry);
    }

    private <T> Mono<T> handleResponse(ClientResponse response, Class<T> responseType) {
        if (response.statusCode().is2xxSuccessful()) {
            if (responseType == Void.class) {
                return response.releaseBody().then(Mono.empty());
            }
            return response.bodyToMono(responseType);
        }
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(
                        errorHandler.handleErrorResponse(errorBody, response.statusCode().value())));
    }

    private static Consumer<Map<String, Object>> authAttributes(String accessToken, String appSecret) {
        return attrs -> {
            attrs.put(MetaAuthFilter.TOKEN_ATTRIBUTE, accessToken);
            if (appSecret != null) {
                attrs.put(MetaAuthFilter.APP_SECRET_ATTRIBUTE, appSecret);
            }
        };
    }
}
