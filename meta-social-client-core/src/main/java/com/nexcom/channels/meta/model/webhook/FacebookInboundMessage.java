package com.nexcom.channels.meta.model.webhook;

import java.util.List;

public record FacebookInboundMessage(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String text,
        List<WebhookAttachment> attachments,
        ReplyContext replyContext
) implements FacebookWebhookEvent {

    // Back-compat constructor for legacy mid-only callers.
    public FacebookInboundMessage(String mid, String senderId, String recipientId, long timestamp,
                                  String text, List<WebhookAttachment> attachments, String replyToMid) {
        this(mid, senderId, recipientId, timestamp, text, attachments,
                replyToMid != null ? ReplyContext.mid(replyToMid) : null);
    }

    public String replyToMid() {
        return replyContext != null ? replyContext.mid() : null;
    }
}
