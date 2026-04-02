package com.nexcom.channels.meta.model.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexcom.channels.meta.model.AttachmentType;

public record WebhookAttachment(
        AttachmentType type,
        @JsonProperty("payload") AttachmentPayload payload
) {
    public record AttachmentPayload(
            String url,
            @JsonProperty("sticker_id") String stickerId
    ) {}
}
