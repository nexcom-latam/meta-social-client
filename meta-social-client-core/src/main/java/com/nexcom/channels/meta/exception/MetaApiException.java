package com.nexcom.channels.meta.exception;

/**
 * Base exception for all Meta Graph API errors.
 * Carries the structured error fields from Meta's error response:
 * <pre>{"error": {"code": 190, "message": "...", "type": "OAuthException", "fbtrace_id": "..."}}</pre>
 */
public class MetaApiException extends RuntimeException {

    private final int code;
    private final String type;
    private final String fbtraceId;

    public MetaApiException(int code, String message, String type, String fbtraceId) {
        super(message);
        this.code = code;
        this.type = type;
        this.fbtraceId = fbtraceId;
    }

    public int getCode() { return code; }
    public String getType() { return type; }
    public String getFbtraceId() { return fbtraceId; }
}
