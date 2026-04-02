package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single message within a conversation, from GET /{conversation-id}/messages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationMessage(
        String id,
        String message,
        MessageParticipant from,
        MessageParticipant to,
        @JsonProperty("created_time") String createdTime
) {
    public record MessageParticipant(
            String name,
            String email,
            String id
    ) {}
}
