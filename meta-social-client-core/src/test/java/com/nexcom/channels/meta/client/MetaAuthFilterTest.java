package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.auth.HmacUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class MetaAuthFilterTest {

    private final MetaAuthFilter filter = new MetaAuthFilter();

    @Test
    void filter_withToken_addsBearerHeader() {
        ClientRequest[] captured = new ClientRequest[1];
        ExchangeFunction next = request -> {
            captured[0] = request;
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("https://graph.facebook.com/v25.0/me"))
                .attribute(MetaAuthFilter.TOKEN_ATTRIBUTE, "my_token")
                .build();

        StepVerifier.create(filter.filter(request, next)).expectNextCount(1).verifyComplete();

        assertThat(captured[0].headers().getFirst("Authorization")).isEqualTo("Bearer my_token");
    }

    @Test
    void filter_withTokenAndSecret_appendsAppSecretProof() {
        ClientRequest[] captured = new ClientRequest[1];
        ExchangeFunction next = request -> {
            captured[0] = request;
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("https://graph.facebook.com/v25.0/me"))
                .attribute(MetaAuthFilter.TOKEN_ATTRIBUTE, "my_token")
                .attribute(MetaAuthFilter.APP_SECRET_ATTRIBUTE, "my_secret")
                .build();

        StepVerifier.create(filter.filter(request, next)).expectNextCount(1).verifyComplete();

        String expectedProof = HmacUtils.appSecretProof("my_token", "my_secret");
        assertThat(captured[0].url().toString()).contains("appsecret_proof=" + expectedProof);
    }

    @Test
    void filter_noToken_passesThrough() {
        ClientRequest[] captured = new ClientRequest[1];
        ExchangeFunction next = request -> {
            captured[0] = request;
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("https://graph.facebook.com/v25.0/me"))
                .build();

        StepVerifier.create(filter.filter(request, next)).expectNextCount(1).verifyComplete();

        assertThat(captured[0].headers().getFirst("Authorization")).isNull();
        assertThat(captured[0].url().toString()).doesNotContain("appsecret_proof");
    }

    @Test
    void filter_tokenNoSecret_bearerOnlyNoProof() {
        ClientRequest[] captured = new ClientRequest[1];
        ExchangeFunction next = request -> {
            captured[0] = request;
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("https://graph.facebook.com/v25.0/me"))
                .attribute(MetaAuthFilter.TOKEN_ATTRIBUTE, "my_token")
                .build();

        StepVerifier.create(filter.filter(request, next)).expectNextCount(1).verifyComplete();

        assertThat(captured[0].headers().getFirst("Authorization")).isEqualTo("Bearer my_token");
        assertThat(captured[0].url().toString()).doesNotContain("appsecret_proof");
    }
}
