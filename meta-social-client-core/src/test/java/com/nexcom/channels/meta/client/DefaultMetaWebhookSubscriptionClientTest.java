package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.model.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMetaWebhookSubscriptionClientTest {

    private MockWebServer mockWebServer;
    private DefaultMetaWebhookSubscriptionClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String base = mockWebServer.url("").toString().replaceAll("/$", "");
        ObjectMapper objectMapper = new ObjectMapper();
        MetaApiErrorHandler errorHandler = new MetaApiErrorHandler(objectMapper);
        WebClient webClient = WebClient.builder().filter(new MetaAuthFilter()).build();
        MetaGraphApiClient graphApiClient = new WebClientMetaGraphApiClient(webClient, errorHandler);

        MetaEndpointResolver resolver = new MetaEndpointResolver() {
            @Override
            public String baseUrl(InstagramAuthPath authPath, MetaApiVersion version) {
                return base;
            }
        };

        MetaApiContext context = new MetaApiContext(
                "tenant_1", MetaChannel.FACEBOOK, InstagramAuthPath.FACEBOOK_PAGE,
                "test_token", "app_secret", MetaApiVersion.DEFAULT, "page_123"
        );

        client = new DefaultMetaWebhookSubscriptionClient(graphApiClient, resolver, context);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void subscribe_postsToSubscribedApps() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        StepVerifier.create(client.subscribe(List.of("messages", "messaging_seen")))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).startsWith("/page_123/subscribed_apps");
        String body = recorded.getBody().readUtf8();
        assertThat(body).contains("messages,messaging_seen");
    }

    @Test
    void unsubscribe_deletesSubscribedApps() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        StepVerifier.create(client.unsubscribe())
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("DELETE");
        assertThat(recorded.getPath()).startsWith("/page_123/subscribed_apps");
    }
}
