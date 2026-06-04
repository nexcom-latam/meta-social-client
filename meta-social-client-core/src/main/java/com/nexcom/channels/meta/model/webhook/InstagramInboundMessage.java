package com.nexcom.channels.meta.model.webhook;

import java.util.List;

public record InstagramInboundMessage(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String text,
        List<WebhookAttachment> attachments,
        ReplyContext replyContext
) implements InstagramWebhookEvent {

    // Back-compat constructor: callers that only know the legacy mid form still compile.
    public InstagramInboundMessage(String mid, String senderId, String recipientId, long timestamp,
                                   String text, List<WebhookAttachment> attachments, String replyToMid) {
        this(mid, senderId, recipientId, timestamp, text, attachments,
                replyToMid != null ? ReplyContext.mid(replyToMid) : null);
    }

    // Back-compat accessor for the most common legacy use (DM quote-reply mid).
    public String replyToMid() {
        return replyContext != null ? replyContext.mid() : null;
    }
}
