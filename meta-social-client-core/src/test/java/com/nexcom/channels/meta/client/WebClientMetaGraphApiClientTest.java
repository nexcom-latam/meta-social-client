package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.exception.MetaApiException;
import com.nexcom.channels.meta.exception.MetaAuthException;
import com.nexcom.channels.meta.exception.MetaRateLimitException;
import com.nexcom.channels.meta.model.SendResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientMetaGraphApiClientTest {

    private MockWebServer mockWebServer;
    private WebClientMetaGraphApiClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        MetaApiErrorHandler errorHandler = new MetaApiErrorHandler(objectMapper);
        WebClient webClient = WebClient.builder()
                .filter(new MetaAuthFilter())
                .build();
        // Use fast retry for tests (no real backoff)
        Retry testRetry = MetaRetryFilter.rateLimitRetry(2, Duration.ofMillis(10));
        client = new WebClientMetaGraphApiClient(webClient, errorHandler, objectMapper, testRetry);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void post_success_returnsParsedResponse() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"recipient_id\":\"123\",\"message_id\":\"m_abc\"}"));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "test_token", null, "{}", SendResponse.class))
                .assertNext(resp -> {
                    assertThat(resp.recipientId()).isEqualTo("123");
                    assertThat(resp.messageId()).isEqualTo("m_abc");
                })
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test_token");
    }

    @Test
    void post_withAppSecret_includesAppSecretProof() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"recipient_id\":\"123\",\"message_id\":\"m_abc\"}"));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "test_token", "my_secret", "{}", SendResponse.class))
                .assertNext(resp -> assertThat(resp.recipientId()).isEqualTo("123"))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertThat(path).contains("appsecret_proof=");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test_token");
    }

    @Test
    void post_authError_throwsMetaAuthException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"message\":\"Invalid token\",\"type\":\"OAuthException\",\"code\":190,\"fbtrace_id\":\"trace1\"}}"));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "bad_token", null, "{}", SendResponse.class))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(MetaAuthException.class);
                    MetaAuthException authErr = (MetaAuthException) err;
                    assertThat(authErr.getCode()).isEqualTo(190);
                    assertThat(authErr.getFbtraceId()).isEqualTo("trace1");
                })
                .verify();
    }

    @Test
    void post_genericError_throwsMetaApiException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"message\":\"Bad request\",\"type\":\"GraphMethodException\",\"code\":100,\"fbtrace_id\":\"trace2\"}}"));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "token", null, "{}", SendResponse.class))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(MetaApiException.class);
                    MetaApiException apiErr = (MetaApiException) err;
                    assertThat(apiErr.getCode()).isEqualTo(100);
                })
                .verify();
    }

    @Test
    void post_rateLimitThenSuccess_retriesAndReturns() throws InterruptedException {
        // First call: rate limit
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\":{\"message\":\"Rate limit\",\"type\":\"OAuthException\",\"code\":4,\"fbtrace_id\":\"t1\"}}"));
        // Second call: success
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"recipient_id\":\"123\",\"message_id\":\"m_retry\"}"));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "token", null, "{}", SendResponse.class))
                .assertNext(resp -> assertThat(resp.messageId()).isEqualTo("m_retry"))
                .verifyComplete();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void post_rateLimitExhausted_throwsRateLimitException() {
        String errorBody = "{\"error\":{\"message\":\"Rate limit\",\"type\":\"OAuthException\",\"code\":4,\"fbtrace_id\":\"t1\"}}";
        // 3 failures (1 initial + 2 retries)
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Content-Type", "application/json").setBody(errorBody));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Content-Type", "application/json").setBody(errorBody));
        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setHeader("Content-Type", "application/json").setBody(errorBody));

        String url = mockWebServer.url("/v25.0/page123/messages").toString();

        StepVerifier.create(client.post(url, "token", null, "{}", SendResponse.class))
                .expectErrorSatisfies(err -> assertThat(err).isInstanceOf(MetaRateLimitException.class))
                .verify();
    }

    @Test
    void get_success_returnsParsedResponse() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"recipient_id\":\"456\",\"message_id\":\"m_get\"}"));

        String url = mockWebServer.url("/v25.0/att_123").toString();

        StepVerifier.create(client.get(url, "token", null, SendResponse.class))
                .assertNext(resp -> assertThat(resp.messageId()).isEqualTo("m_get"))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void delete_success_completesEmpty() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        String url = mockWebServer.url("/v25.0/att_123").toString();

        StepVerifier.create(client.delete(url, "token", null))
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("DELETE");
    }
}
