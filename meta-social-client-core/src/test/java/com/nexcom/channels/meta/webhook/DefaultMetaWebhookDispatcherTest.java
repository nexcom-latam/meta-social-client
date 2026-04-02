package com.nexcom.channels.meta.webhook;

import com.nexcom.channels.meta.api.MetaWebhookEventHandler;
import com.nexcom.channels.meta.model.webhook.FacebookInboundMessage;
import com.nexcom.channels.meta.model.webhook.InstagramInboundMessage;
import com.nexcom.channels.meta.model.webhook.MetaWebhookEvent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMetaWebhookDispatcherTest {

    @Test
    void dispatch_routesToMatchingHandler() {
        AtomicInteger callCount = new AtomicInteger();
        MetaWebhookEventHandler<InstagramInboundMessage> handler = new MetaWebhookEventHandler<>() {
            @Override public boolean supports(MetaWebhookEvent event) { return event instanceof InstagramInboundMessage; }
            @Override public Mono<Void> handle(InstagramInboundMessage event) { callCount.incrementAndGet(); return Mono.empty(); }
            @Override public Class<InstagramInboundMessage> eventType() { return InstagramInboundMessage.class; }
        };

        var dispatcher = new DefaultMetaWebhookDispatcher(List.of(handler));
        var event = new InstagramInboundMessage("m_1", "s", "r", 0L, "hi", List.of(), null);

        StepVerifier.create(dispatcher.dispatch(event)).verifyComplete();
        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    void dispatch_noMatchingHandler_completesWithoutError() {
        MetaWebhookEventHandler<InstagramInboundMessage> handler = new MetaWebhookEventHandler<>() {
            @Override public boolean supports(MetaWebhookEvent event) { return event instanceof InstagramInboundMessage; }
            @Override public Mono<Void> handle(InstagramInboundMessage event) { return Mono.empty(); }
            @Override public Class<InstagramInboundMessage> eventType() { return InstagramInboundMessage.class; }
        };

        var dispatcher = new DefaultMetaWebhookDispatcher(List.of(handler));
        // Send a Facebook event — handler only supports Instagram
        var event = new FacebookInboundMessage("m_2", "s", "r", 0L, "hello", List.of(), null);

        StepVerifier.create(dispatcher.dispatch(event)).verifyComplete();
    }

    @Test
    void dispatch_emptyHandlerList_completesWithoutError() {
        var dispatcher = new DefaultMetaWebhookDispatcher(List.of());
        var event = new InstagramInboundMessage("m_3", "s", "r", 0L, "test", List.of(), null);

        StepVerifier.create(dispatcher.dispatch(event)).verifyComplete();
    }

    @Test
    void dispatch_oneHandlerFails_otherStillCalled() {
        AtomicInteger successCount = new AtomicInteger();

        MetaWebhookEventHandler<InstagramInboundMessage> failingHandler = new MetaWebhookEventHandler<>() {
            @Override public boolean supports(MetaWebhookEvent event) { return event instanceof InstagramInboundMessage; }
            @Override public Mono<Void> handle(InstagramInboundMessage event) { return Mono.error(new RuntimeException("boom")); }
            @Override public Class<InstagramInboundMessage> eventType() { return InstagramInboundMessage.class; }
        };

        MetaWebhookEventHandler<InstagramInboundMessage> successHandler = new MetaWebhookEventHandler<>() {
            @Override public boolean supports(MetaWebhookEvent event) { return event instanceof InstagramInboundMessage; }
            @Override public Mono<Void> handle(InstagramInboundMessage event) { successCount.incrementAndGet(); return Mono.empty(); }
            @Override public Class<InstagramInboundMessage> eventType() { return InstagramInboundMessage.class; }
        };

        var dispatcher = new DefaultMetaWebhookDispatcher(List.of(failingHandler, successHandler));
        var event = new InstagramInboundMessage("m_4", "s", "r", 0L, "test", List.of(), null);

        StepVerifier.create(dispatcher.dispatch(event)).verifyComplete();
        assertThat(successCount.get()).isEqualTo(1);
    }
}
