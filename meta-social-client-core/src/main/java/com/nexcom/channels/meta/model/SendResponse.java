package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from Graph API after a successful message send.
 */
public record SendResponse(
        @JsonProperty("recipient_id") String recipientId,
        @JsonProperty("message_id") String messageId
) {}
