package com.nexcom.channels.meta.model.webhook;

public record FacebookMessageReaction(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String action,
        String emoji
) implements FacebookWebhookEvent {}
