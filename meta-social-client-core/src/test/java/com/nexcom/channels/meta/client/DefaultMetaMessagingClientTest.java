package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.JsonNode;
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

class DefaultMetaMessagingClientTest {

    private MockWebServer mockWebServer;
    private DefaultMetaMessagingClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        // Trim trailing slash for clean URL building
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        MetaApiErrorHandler errorHandler = new MetaApiErrorHandler(objectMapper);
        WebClient webClient = WebClient.builder()
                .filter(new MetaAuthFilter())
                .build();
        MetaGraphApiClient graphApiClient = new WebClientMetaGraphApiClient(webClient, errorHandler);

        // Use a custom endpoint resolver that points to MockWebServer
        MetaEndpointResolver resolver = new MetaEndpointResolver() {
            @Override
            public String baseUrl(InstagramAuthPath authPath, MetaApiVersion version) {
                return base;
            }
        };

        MetaApiContext context = new MetaApiContext(
                "tenant_1", MetaChannel.INSTAGRAM, InstagramAuthPath.INSTAGRAM_LOGIN,
                "test_token", "app_secret", MetaApiVersion.V25_0, "page_123"
        );

        client = new DefaultMetaMessagingClient(graphApiClient, resolver, context);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void send_textMessage_postsToMessagesEndpoint() throws Exception {
        enqueueSuccess();

        SendRequest req = SendRequest.text("user_1", "Hello", MessagingType.RESPONSE);
        StepVerifier.create(client.send(req))
                .assertNext(resp -> {
                    assertThat(resp.recipientId()).isEqualTo("user_1");
                    assertThat(resp.messageId()).isEqualTo("m_sent");
                })
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getPath()).startsWith("/page_123/messages");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test_token");

        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.get("recipient").get("id").asText()).isEqualTo("user_1");
        assertThat(body.get("message").get("text").asText()).isEqualTo("Hello");
    }

    @Test
    void reply_setsReplyToMidInBody() throws Exception {
        enqueueSuccess();

        SendRequest req = SendRequest.text("user_1", "Got it!", MessagingType.RESPONSE);
        StepVerifier.create(client.reply("m_original_123", req))
                .assertNext(resp -> assertThat(resp.messageId()).isEqualTo("m_sent"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.get("message").get("reply_to").get("mid").asText()).isEqualTo("m_original_123");
    }

    @Test
    void privateReply_postsToPrivateRepliesEndpoint() throws Exception {
        enqueueSuccess();

        StepVerifier.create(client.privateReply("comment_456", "Thanks for your feedback!"))
                .assertNext(resp -> assertThat(resp.messageId()).isEqualTo("m_sent"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).startsWith("/comment_456/private_replies");

        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.get("message").asText()).isEqualTo("Thanks for your feedback!");
    }

    @Test
    void handover_passThread_postsToCorrectEndpoint() throws Exception {
        enqueueHandoverSuccess();

        ThreadControl tc = ThreadControl.passThread("user_1", "target_app_789", "Escalating to human");
        StepVerifier.create(client.handover(tc))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).startsWith("/page_123/pass_thread_control");

        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.get("recipient").get("id").asText()).isEqualTo("user_1");
        assertThat(body.get("target_app_id").asText()).isEqualTo("target_app_789");
        assertThat(body.get("metadata").asText()).isEqualTo("Escalating to human");
    }

    @Test
    void handover_takeThread_postsToCorrectEndpoint() throws Exception {
        enqueueHandoverSuccess();

        ThreadControl tc = ThreadControl.takeThread("user_1", null);
        StepVerifier.create(client.handover(tc))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).startsWith("/page_123/take_thread_control");
    }

    @Test
    void senderAction_postsToMessagesEndpoint() throws Exception {
        enqueueHandoverSuccess();

        StepVerifier.create(client.senderAction("user_1", SenderAction.TYPING_ON))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).startsWith("/page_123/messages");

        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertThat(body.get("sender_action").asText()).isEqualTo("typing_on");
    }

    @Test
    void listMessages_getsConversationMessages() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\":[{\"id\":\"msg_1\",\"message\":\"hi\",\"created_time\":\"2026-04-02T00:00:00Z\"}]}"));

        StepVerifier.create(client.listMessages("conv_123"))
                .assertNext(page -> {
                    assertThat(page.data()).hasSize(1);
                    assertThat(page.data().getFirst().id()).isEqualTo("msg_1");
                })
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).startsWith("/conv_123/messages");
    }

    @Test
    void deleteMessage_deletesCorrectEndpoint() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        StepVerifier.create(client.deleteMessage("msg_456"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("DELETE");
        assertThat(recorded.getPath()).startsWith("/msg_456");
    }

    private void enqueueSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"recipient_id\":\"user_1\",\"message_id\":\"m_sent\"}"));
    }

    private void enqueueHandoverSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));
    }
}
