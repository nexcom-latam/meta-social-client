package com.nexcom.channels.meta.webhook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.auth.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

class MetaWebhookControllerTest {

    private static final String APP_SECRET = "controller_test_secret";
    private static final String VERIFY_TOKEN = "my_verify_token";

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MetaWebhookSignatureValidator validator = new MetaWebhookSignatureValidator(APP_SECRET);
        MetaWebhookParser parser = new MetaWebhookParser(mapper);
        DefaultMetaWebhookDispatcher dispatcher = new DefaultMetaWebhookDispatcher(List.of());
        MetaWebhookController controller = new MetaWebhookController(validator, parser, dispatcher, VERIFY_TOKEN);

        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void hubChallenge_validToken_returns200WithChallenge() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/webhooks/meta")
                        .queryParam("hub.mode", "subscribe")
                        .queryParam("hub.verify_token", VERIFY_TOKEN)
                        .queryParam("hub.challenge", "challenge_123")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("challenge_123");
    }

    @Test
    void hubChallenge_invalidToken_returns403() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/webhooks/meta")
                        .queryParam("hub.mode", "subscribe")
                        .queryParam("hub.verify_token", "wrong_token")
                        .queryParam("hub.challenge", "challenge_123")
                        .build())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void receive_validSignature_returns200() {
        String body = "{\"object\":\"page\",\"entry\":[{\"id\":\"1\",\"time\":0,\"messaging\":[{\"sender\":{\"id\":\"s\"},\"recipient\":{\"id\":\"r\"},\"timestamp\":0,\"message\":{\"mid\":\"m_1\",\"text\":\"hi\"}}]}]}";
        String sig = "sha256=" + HmacUtils.hmacSha256Hex(APP_SECRET, body);

        webTestClient.post()
                .uri("/webhooks/meta")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Hub-Signature-256", sig)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void receive_invalidSignature_returns403() {
        webTestClient.post()
                .uri("/webhooks/meta")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Hub-Signature-256", "sha256=invalid")
                .bodyValue("{\"object\":\"page\",\"entry\":[]}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void receive_missingSignature_returns403() {
        webTestClient.post()
                .uri("/webhooks/meta")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"object\":\"page\",\"entry\":[]}")
                .exchange()
                .expectStatus().isForbidden();
    }
}
