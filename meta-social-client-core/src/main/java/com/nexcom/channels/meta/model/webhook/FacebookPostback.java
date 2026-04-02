package com.nexcom.channels.meta.model.webhook;

public record FacebookPostback(
        String mid,
        String senderId,
        String recipientId,
        long timestamp,
        String title,
        String payload,
        String referral
) implements FacebookWebhookEvent {}
