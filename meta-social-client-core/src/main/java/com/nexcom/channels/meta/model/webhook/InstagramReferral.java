package com.nexcom.channels.meta.model.webhook;

public record InstagramReferral(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String ref,
        String source,
        String type,
        String adId
) implements InstagramWebhookEvent {}
