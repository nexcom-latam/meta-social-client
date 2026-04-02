package com.nexcom.channels.meta.model.webhook;

public record InstagramMention(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String mediaId,
        String mediaUrl,
        String cdnUrl
) implements InstagramWebhookEvent {}
