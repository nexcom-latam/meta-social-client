package com.nexcom.channels.meta.webhook;

import com.nexcom.channels.meta.auth.HmacUtils;
import com.nexcom.channels.meta.exception.MetaWebhookSignatureException;

/**
 * Validates the X-Hub-Signature-256 header on incoming webhook POSTs.
 */
public class MetaWebhookSignatureValidator {

    private final String appSecret;

    public MetaWebhookSignatureValidator(String appSecret) {
        this.appSecret = appSecret;
    }

    /**
     * Validates the signature. Throws on failure or missing/blank header.
     */
    public void validate(String rawBody, String xHubSignature256) {
        if (xHubSignature256 == null || xHubSignature256.isBlank()) {
            throw new MetaWebhookSignatureException("Missing X-Hub-Signature-256 header");
        }
        if (!HmacUtils.verifyWebhookSignature(rawBody, xHubSignature256, appSecret)) {
            throw new MetaWebhookSignatureException("Invalid webhook signature");
        }
    }
}
