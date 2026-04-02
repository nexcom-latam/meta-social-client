package com.nexcom.channels.meta.model.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A single messaging event within a webhook entry.
 * Meta includes exactly one of the nullable JsonNode fields —
 * the non-null field determines the event type.
 */
public record WebhookMessaging(
        WebhookSender sender,
        WebhookSender recipient,
        long timestamp,
        JsonNode message,
        JsonNode postback,
        JsonNode read,
        JsonNode reaction,
        JsonNode optin,
        JsonNode referral,
        @JsonProperty("pass_thread_control") JsonNode passThreadControl,
        @JsonProperty("take_thread_control") JsonNode takeThreadControl,
        JsonNode standby
) {}
