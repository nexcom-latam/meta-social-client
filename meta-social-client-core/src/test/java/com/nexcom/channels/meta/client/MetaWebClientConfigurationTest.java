package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class MetaWebClientConfigurationTest {

    @Test
    void createWebClient_returnsNonNull() {
        var config = new MetaWebClientConfiguration(new ObjectMapper());
        WebClient client = config.createWebClient();
        assertThat(client).isNotNull();
    }

    @Test
    void createErrorHandler_returnsNonNull() {
        var config = new MetaWebClientConfiguration(new ObjectMapper());
        MetaApiErrorHandler handler = config.createErrorHandler();
        assertThat(handler).isNotNull();
    }

    @Test
    void createGraphApiClient_returnsNonNull() {
        var config = new MetaWebClientConfiguration(new ObjectMapper());
        WebClientMetaGraphApiClient client = config.createGraphApiClient();
        assertThat(client).isNotNull();
    }
}
