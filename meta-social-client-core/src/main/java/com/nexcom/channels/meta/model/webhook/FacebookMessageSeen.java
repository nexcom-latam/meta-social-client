package com.nexcom.channels.meta.model.webhook;

public record FacebookMessageSeen(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        long watermark
) implements FacebookWebhookEvent {}
