package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for POST /{comment-id}/private_replies.
 * Sends a private message in response to a public comment or post.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrivateReplyRequest(
        @JsonProperty("message") String message
) {
    public static PrivateReplyRequest of(String text) {
        return new PrivateReplyRequest(text);
    }
}
