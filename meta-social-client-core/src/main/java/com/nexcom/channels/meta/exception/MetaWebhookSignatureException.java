package com.nexcom.channels.meta.exception;

/**
 * Invalid HMAC-SHA256 webhook signature.
 */
public class MetaWebhookSignatureException extends MetaApiException {

    public MetaWebhookSignatureException(String message) {
        super(401, message, "WebhookSignatureError", null);
    }
}
