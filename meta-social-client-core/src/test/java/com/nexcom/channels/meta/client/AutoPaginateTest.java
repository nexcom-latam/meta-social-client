package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.model.Conversation;
import com.nexcom.channels.meta.model.GraphApiPage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AutoPaginateTest {

    private MockWebServer mockWebServer;
    private WebClientMetaGraphApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ObjectMapper objectMapper = new ObjectMapper();
        MetaApiErrorHandler errorHandler = new MetaApiErrorHandler(objectMapper);
        WebClient webClient = WebClient.builder().filter(new MetaAuthFilter()).build();
        client = new WebClientMetaGraphApiClient(webClient, errorHandler, objectMapper,
                MetaRetryFilter.rateLimitRetry(1, java.time.Duration.ofMillis(1)));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getAll_followsNextLinks_emitsAllElements() {
        String page2Url = mockWebServer.url("/page2").toString();
        // Page 1: has next link
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\":[{\"id\":\"c1\"},{\"id\":\"c2\"}],\"paging\":{\"next\":\"" + page2Url + "\"}}"));
        // Page 2: no next link
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\":[{\"id\":\"c3\"}]}"));

        String url = mockWebServer.url("/page1").toString();
        TypeReference<GraphApiPage<Conversation>> typeRef = new TypeReference<>() {};

        StepVerifier.create(client.getAll(url, "token", null, typeRef))
                .assertNext(c -> assertThat(c.id()).isEqualTo("c1"))
                .assertNext(c -> assertThat(c.id()).isEqualTo("c2"))
                .assertNext(c -> assertThat(c.id()).isEqualTo("c3"))
                .verifyComplete();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void getAll_singlePage_emitsAllElements() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\":[{\"id\":\"c1\"}]}"));

        String url = mockWebServer.url("/page1").toString();
        TypeReference<GraphApiPage<Conversation>> typeRef = new TypeReference<>() {};

        StepVerifier.create(client.getAll(url, "token", null, typeRef))
                .assertNext(c -> assertThat(c.id()).isEqualTo("c1"))
                .verifyComplete();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
