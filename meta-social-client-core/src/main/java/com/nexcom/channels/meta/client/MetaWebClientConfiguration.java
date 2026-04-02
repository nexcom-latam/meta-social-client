package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Factory for building the WebClient used by {@link WebClientMetaGraphApiClient}.
 * In Phase 1 this is a simple builder. Phase 3 adds metrics filters, retry logic, etc.
 */
public class MetaWebClientConfiguration {

    private final ObjectMapper objectMapper;

    public MetaWebClientConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public WebClient createWebClient() {
        return WebClient.builder()
                .filter(new MetaAuthFilter())
                .build();
    }

    public MetaApiErrorHandler createErrorHandler() {
        return new MetaApiErrorHandler(objectMapper);
    }

    public WebClientMetaGraphApiClient createGraphApiClient() {
        return new WebClientMetaGraphApiClient(createWebClient(), createErrorHandler());
    }
}
