package com.nexcom.channels.meta.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HmacUtilsTest {

    @Test
    void hmacSha256Hex_knownVector() {
        // Verified: echo -n "hello" | openssl dgst -sha256 -hmac "test_secret"
        String expected = "25402e8a53eb60d19c604883f51d221f73db93e083682cb8dfd3c36d49224f6e";
        assertThat(HmacUtils.hmacSha256Hex("test_secret", "hello")).isEqualTo(expected);
    }

    @Test
    void appSecretProof_generatesCorrectProof() {
        String proof = HmacUtils.appSecretProof("my_token", "my_secret");
        assertThat(proof).isNotBlank();
        // Must be deterministic
        assertThat(proof).isEqualTo(HmacUtils.appSecretProof("my_token", "my_secret"));
    }

    @Test
    void verifyWebhookSignature_valid() {
        String body = "{\"test\":\"payload\"}";
        String secret = "app_secret_123";
        String hash = HmacUtils.hmacSha256Hex(secret, body);
        String header = "sha256=" + hash;

        assertThat(HmacUtils.verifyWebhookSignature(body, header, secret)).isTrue();
    }

    @Test
    void verifyWebhookSignature_tampered() {
        String body = "{\"test\":\"payload\"}";
        String secret = "app_secret_123";
        String header = "sha256=0000000000000000000000000000000000000000000000000000000000000000";

        assertThat(HmacUtils.verifyWebhookSignature(body, header, secret)).isFalse();
    }

    @Test
    void verifyWebhookSignature_nullHeader() {
        assertThat(HmacUtils.verifyWebhookSignature("body", null, "secret")).isFalse();
    }

    @Test
    void verifyWebhookSignature_blankHeader() {
        assertThat(HmacUtils.verifyWebhookSignature("body", "  ", "secret")).isFalse();
    }

    @Test
    void verifyWebhookSignature_malformedHeader_noPrefx() {
        assertThat(HmacUtils.verifyWebhookSignature("body", "noprefixhere", "secret")).isFalse();
    }
}
