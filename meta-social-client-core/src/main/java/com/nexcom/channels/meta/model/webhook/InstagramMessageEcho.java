package com.nexcom.channels.meta.model.webhook;

import java.util.List;

public record InstagramMessageEcho(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String appId,
        String text,
        List<WebhookAttachment> attachments
) implements InstagramWebhookEvent {}
