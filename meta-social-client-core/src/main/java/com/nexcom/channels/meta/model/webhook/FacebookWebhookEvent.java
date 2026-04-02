package com.nexcom.channels.meta.model.webhook;

public sealed interface FacebookWebhookEvent extends MetaWebhookEvent
        permits FacebookInboundMessage, FacebookMessageEcho, FacebookMessageReaction,
                FacebookMessageSeen, FacebookPostback, FacebookOptin,
                FacebookReferral, FacebookHandover, FacebookStandby {
}
