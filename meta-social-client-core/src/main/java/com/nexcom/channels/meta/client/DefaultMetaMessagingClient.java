package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nexcom.channels.meta.api.MetaMessagingClient;
import com.nexcom.channels.meta.model.*;
import reactor.core.publisher.Mono;

/**
 * Default implementation of {@link MetaMessagingClient}.
 * Uses {@link MetaEndpointResolver} to build URLs and
 * {@link MetaGraphApiClient} to execute HTTP requests.
 */
public class DefaultMetaMessagingClient implements MetaMessagingClient {

    private final MetaGraphApiClient graphApiClient;
    private final MetaEndpointResolver endpointResolver;
    private final MetaApiContext context;

    public DefaultMetaMessagingClient(MetaGraphApiClient graphApiClient,
                                      MetaEndpointResolver endpointResolver,
                                      MetaApiContext context) {
        this.graphApiClient = graphApiClient;
        this.endpointResolver = endpointResolver;
        this.context = context;
    }

    @Override
    public Mono<SendResponse> send(SendRequest request) {
        return graphApiClient.post(messagesUrl(), token(), secret(), request, SendResponse.class);
    }

    @Override
    public Mono<SendResponse> reply(String replyToMid, SendRequest request) {
        return send(request.withReplyTo(replyToMid));
    }

    @Override
    public Mono<SendResponse> privateReply(String commentOrPostId, String text) {
        String url = endpointResolver.privateReplyUrl(
                context.authPath(), context.apiVersion(), commentOrPostId);
        return graphApiClient.post(url, token(), secret(), PrivateReplyRequest.of(text), SendResponse.class);
    }

    @Override
    public Mono<Void> senderAction(String recipientId, SenderAction action) {
        SendRequest request = SendRequest.senderAction(recipientId, action);
        return graphApiClient.post(messagesUrl(), token(), secret(), request, Void.class).then();
    }

    @Override
    public Mono<Void> handover(ThreadControl threadControl) {
        String url = endpointResolver.threadControlUrl(
                context.authPath(), context.apiVersion(),
                context.scopedId(), threadControl.action());
        return graphApiClient.post(url, token(), secret(), threadControl, Void.class).then();
    }

    @Override
    public Mono<GraphApiPage<Conversation>> listConversations() {
        String url = endpointResolver.conversationsUrl(context.authPath(), context.apiVersion(), context.scopedId());
        return graphApiClient.getPaged(url, token(), secret(), new TypeReference<>() {});
    }

    @Override
    public Mono<GraphApiPage<ConversationMessage>> listMessages(String conversationId) {
        String url = endpointResolver.conversationMessagesUrl(context.authPath(), context.apiVersion(), conversationId);
        return graphApiClient.getPaged(url, token(), secret(), new TypeReference<>() {});
    }

    @Override
    public Mono<Void> deleteMessage(String messageId) {
        String url = endpointResolver.messageUrl(context.authPath(), context.apiVersion(), messageId);
        return graphApiClient.delete(url, token(), secret());
    }

    private String messagesUrl() {
        return endpointResolver.messagesUrl(context.authPath(), context.apiVersion(), context.scopedId());
    }

    private String token() { return context.accessToken(); }
    private String secret() { return context.appSecret(); }
}
