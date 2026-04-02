package com.nexcom.channels.meta.model.webhook;

public record FacebookHandover(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String newOwnerAppId,
        String previousOwnerAppId,
        String metadata
) implements FacebookWebhookEvent {}
