package com.nexcom.channels.meta.exception;

/**
 * Token expired, invalid, or revoked. Graph API error code 190.
 */
public class MetaAuthException extends MetaApiException {

    public MetaAuthException(String message, String fbtraceId) {
        super(190, message, "OAuthException", fbtraceId);
    }
}
