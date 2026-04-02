package com.nexcom.channels.meta.model.webhook;

public sealed interface InstagramWebhookEvent extends MetaWebhookEvent
        permits InstagramInboundMessage, InstagramMessageEcho, InstagramMessageReaction,
                InstagramMessageSeen, InstagramReferral, InstagramMention,
                InstagramHandover, InstagramStandby {
}
