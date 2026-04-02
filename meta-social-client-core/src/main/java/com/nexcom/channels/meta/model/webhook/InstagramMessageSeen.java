package com.nexcom.channels.meta.model.webhook;

public record InstagramMessageSeen(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        long watermark
) implements InstagramWebhookEvent {}
