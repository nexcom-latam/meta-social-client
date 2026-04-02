package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a conversation/thread from GET /{id}/conversations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Conversation(
        String id,
        @JsonProperty("updated_time") String updatedTime,
        String snippet,
        @JsonProperty("message_count") Integer messageCount,
        @JsonProperty("can_reply") Boolean canReply,
        String link
) {}
