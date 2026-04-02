package com.nexcom.channels.meta.exception;

import java.time.Duration;

/**
 * Rate limit exceeded. Graph API error codes 4, 32, 613.
 */
public class MetaRateLimitException extends MetaApiException {

    private final Duration retryAfter;

    public MetaRateLimitException(int code, String message, String fbtraceId, Duration retryAfter) {
        super(code, message, "OAuthException", fbtraceId);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() { return retryAfter; }
}
