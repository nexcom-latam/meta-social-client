package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.exception.MetaApiException;
import com.nexcom.channels.meta.exception.MetaAuthException;
import com.nexcom.channels.meta.exception.MetaRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;

/**
 * Maps Graph API error JSON responses to typed exceptions.
 * <p>
 * Graph API error format:
 * <pre>{"error": {"message": "...", "type": "...", "code": 190, "fbtrace_id": "..."}}</pre>
 */
public class MetaApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(MetaApiErrorHandler.class);
    private static final Set<Integer> RATE_LIMIT_CODES = Set.of(4, 32, 613);

    private final ObjectMapper objectMapper;

    public MetaApiErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a Graph API error response body and throws the appropriate typed exception.
     */
    public MetaApiException handleErrorResponse(String responseBody, int httpStatus) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode error = root.get("error");
            if (error == null) {
                return new MetaApiException(httpStatus, responseBody, "Unknown", null);
            }

            int code = error.has("code") ? error.get("code").asInt() : httpStatus;
            String message = error.has("message") ? error.get("message").asText() : "Unknown error";
            String type = error.has("type") ? error.get("type").asText() : "Unknown";
            String fbtraceId = error.has("fbtrace_id") ? error.get("fbtrace_id").asText() : null;

            if (code == 190) {
                return new MetaAuthException(message, fbtraceId);
            }
            if (RATE_LIMIT_CODES.contains(code)) {
                return new MetaRateLimitException(code, message, fbtraceId, Duration.ofMinutes(1));
            }

            return new MetaApiException(code, message, type, fbtraceId);
        } catch (Exception e) {
            log.error("Failed to parse Graph API error response", e);
            return new MetaApiException(httpStatus, responseBody, "ParseError", null);
        }
    }
}
