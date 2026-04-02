package com.nexcom.channels.meta.model.webhook;

import java.util.List;

public record InstagramStandby(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String text,
        List<WebhookAttachment> attachments
) implements InstagramWebhookEvent {}
