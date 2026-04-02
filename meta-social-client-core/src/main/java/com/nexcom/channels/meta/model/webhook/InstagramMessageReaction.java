package com.nexcom.channels.meta.model.webhook;

public record InstagramMessageReaction(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String action,
        String emoji
) implements InstagramWebhookEvent {}
