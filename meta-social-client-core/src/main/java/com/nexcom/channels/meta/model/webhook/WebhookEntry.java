package com.nexcom.channels.meta.model.webhook;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record WebhookEntry(
        String id,
        long time,
        List<WebhookMessaging> messaging,
        List<JsonNode> changes
) {}
