package com.nexcom.channels.meta.webhook;

import com.nexcom.channels.meta.auth.HmacUtils;
import com.nexcom.channels.meta.exception.MetaWebhookSignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaWebhookSignatureValidatorTest {

    private static final String APP_SECRET = "test_app_secret";
    private MetaWebhookSignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MetaWebhookSignatureValidator(APP_SECRET);
    }

    @Test
    void valid_doesNotThrow() {
        String body = "{\"object\":\"page\"}";
        String hash = HmacUtils.hmacSha256Hex(APP_SECRET, body);

        assertThatCode(() -> validator.validate(body, "sha256=" + hash))
                .doesNotThrowAnyException();
    }

    @Test
    void invalidSignature_throws() {
        assertThatThrownBy(() -> validator.validate("{}", "sha256=bad"))
                .isInstanceOf(MetaWebhookSignatureException.class)
                .hasMessageContaining("Invalid webhook signature");
    }

    @Test
    void nullHeader_throws() {
        assertThatThrownBy(() -> validator.validate("{}", null))
                .isInstanceOf(MetaWebhookSignatureException.class)
                .hasMessageContaining("Missing");
    }

    @Test
    void blankHeader_throws() {
        assertThatThrownBy(() -> validator.validate("{}", "   "))
                .isInstanceOf(MetaWebhookSignatureException.class)
                .hasMessageContaining("Missing");
    }
}
