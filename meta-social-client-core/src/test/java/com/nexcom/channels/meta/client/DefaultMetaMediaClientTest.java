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

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMetaMediaClientTest {

    private MockWebServer mockWebServer;
    private DefaultMetaMediaClient client;

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
                "test_token", "app_secret", MetaApiVersion.V25_0, "page_123"
        );

        client = new DefaultMetaMediaClient(graphApiClient, resolver, context);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void upload_postsMultipartToAttachmentEndpoint() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"attachment_id\":\"att_12345\"}"));

        byte[] imageData = "fake image bytes".getBytes();

        StepVerifier.create(client.upload(imageData, "photo.jpg", "image/jpeg"))
                .assertNext(resp -> assertThat(resp.attachmentId()).isEqualTo("att_12345"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).startsWith("/page_123/message_attachments");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test_token");
        assertThat(recorded.getHeader("Content-Type")).startsWith("multipart/form-data");
    }

    @Test
    void retrieveUrl_getsMediaMetadata() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"url\":\"https://cdn.example.com/media.jpg\",\"mime_type\":\"image/jpeg\",\"size\":12345}"));

        StepVerifier.create(client.retrieveUrl("att_12345"))
                .assertNext(resp -> {
                    assertThat(resp.url()).isEqualTo("https://cdn.example.com/media.jpg");
                    assertThat(resp.mimeType()).isEqualTo("image/jpeg");
                    assertThat(resp.size()).isEqualTo(12345L);
                })
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).startsWith("/att_12345");
    }

    @Test
    void delete_deletesMedia() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        StepVerifier.create(client.delete("att_12345"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("DELETE");
        assertThat(recorded.getPath()).startsWith("/att_12345");
    }
}
