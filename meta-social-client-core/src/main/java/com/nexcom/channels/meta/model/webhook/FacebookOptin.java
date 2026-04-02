package com.nexcom.channels.meta.model.webhook;

public record FacebookOptin(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String ref,
        String userRef,
        String type
) implements FacebookWebhookEvent {}
