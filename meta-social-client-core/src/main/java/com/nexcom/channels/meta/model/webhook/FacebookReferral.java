package com.nexcom.channels.meta.model.webhook;

public record FacebookReferral(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String ref,
        String source,
        String type,
        String adId
) implements FacebookWebhookEvent {}
