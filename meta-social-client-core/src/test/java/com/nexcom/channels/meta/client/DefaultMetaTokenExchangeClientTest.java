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

class DefaultMetaTokenExchangeClientTest {

    private MockWebServer mockWebServer;

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private DefaultMetaTokenExchangeClient createClient(InstagramAuthPath authPath) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String base = mockWebServer.url("").toString().replaceAll("/$", "");
        ObjectMapper objectMapper = new ObjectMapper();
        MetaApiErrorHandler errorHandler = new MetaApiErrorHandler(objectMapper);
        WebClient webClient = WebClient.builder().filter(new MetaAuthFilter()).build();
        MetaGraphApiClient graphApiClient = new WebClientMetaGraphApiClient(webClient, errorHandler);

        MetaEndpointResolver resolver = new MetaEndpointResolver() {
            @Override
            public String baseUrl(InstagramAuthPath ap, MetaApiVersion version) {
                return base;
            }
        };

        MetaApiContext context = new MetaApiContext(
                "tenant_1", MetaChannel.INSTAGRAM, authPath,
                "current_token", "app_secret_123", MetaApiVersion.V25_0, "ig_user_1"
        );

        return new DefaultMetaTokenExchangeClient(graphApiClient, resolver, context);
    }

    @Test
    void exchangeForLongLived_instagramLogin_callsCorrectEndpoint() throws Exception {
        DefaultMetaTokenExchangeClient client = createClient(InstagramAuthPath.INSTAGRAM_LOGIN);
        enqueueTokenResponse();

        StepVerifier.create(client.exchangeForLongLived("short_lived_abc"))
                .assertNext(resp -> {
                    assertThat(resp.accessToken()).isEqualTo("long_lived_xyz");
                    assertThat(resp.expiresIn()).isEqualTo(5184000);
                })
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("GET");
        assertThat(recorded.getPath()).contains("/access_token");
        assertThat(recorded.getPath()).contains("grant_type=ig_exchange_token");
        assertThat(recorded.getPath()).contains("client_secret=app_secret_123");
        assertThat(recorded.getPath()).contains("access_token=short_lived_abc");
    }

    @Test
    void exchangeForLongLived_facebookPage_callsOAuthEndpoint() throws Exception {
        DefaultMetaTokenExchangeClient client = createClient(InstagramAuthPath.FACEBOOK_PAGE);
        enqueueTokenResponse();

        StepVerifier.create(client.exchangeForLongLived("short_lived_abc"))
                .assertNext(resp -> assertThat(resp.accessToken()).isEqualTo("long_lived_xyz"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).contains("/oauth/access_token");
        assertThat(recorded.getPath()).contains("grant_type=fb_exchange_token");
        assertThat(recorded.getPath()).contains("fb_exchange_token=short_lived_abc");
    }

    @Test
    void refresh_instagramLogin_callsRefreshEndpoint() throws Exception {
        DefaultMetaTokenExchangeClient client = createClient(InstagramAuthPath.INSTAGRAM_LOGIN);
        enqueueTokenResponse();

        StepVerifier.create(client.refresh("long_lived_token"))
                .assertNext(resp -> assertThat(resp.accessToken()).isEqualTo("long_lived_xyz"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).contains("/refresh_access_token");
        assertThat(recorded.getPath()).contains("grant_type=ig_refresh_token");
    }

    @Test
    void refresh_facebookPage_reExchanges() throws Exception {
        DefaultMetaTokenExchangeClient client = createClient(InstagramAuthPath.FACEBOOK_PAGE);
        enqueueTokenResponse();

        StepVerifier.create(client.refresh("long_lived_token"))
                .assertNext(resp -> assertThat(resp.accessToken()).isEqualTo("long_lived_xyz"))
                .verifyComplete();

        RecordedRequest recorded = mockWebServer.takeRequest();
        // Facebook refresh is a re-exchange
        assertThat(recorded.getPath()).contains("/oauth/access_token");
        assertThat(recorded.getPath()).contains("grant_type=fb_exchange_token");
    }

    private void enqueueTokenResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"long_lived_xyz\",\"token_type\":\"bearer\",\"expires_in\":5184000}"));
    }
}
