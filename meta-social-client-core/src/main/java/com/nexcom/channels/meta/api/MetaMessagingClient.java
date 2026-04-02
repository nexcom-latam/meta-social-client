package com.nexcom.channels.meta.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nexcom.channels.meta.model.*;
import reactor.core.publisher.Mono;

/**
 * Reactive client for sending messages and performing actions on
 * Instagram DM and Facebook Messenger conversations.
 */
public interface MetaMessagingClient {

    /**
     * Sends a message (text, media, or template).
     * Maps to POST /{page-or-ig-user-id}/messages
     */
    Mono<SendResponse> send(SendRequest request);

    /**
     * Replies to a specific message by mid.
     * Convenience that sets reply_to.mid in the send payload.
     */
    Mono<SendResponse> reply(String replyToMid, SendRequest request);

    /**
     * Sends a private reply to a public comment or post.
     * Maps to POST /{comment-id}/private_replies
     */
    Mono<SendResponse> privateReply(String commentOrPostId, String text);

    /**
     * Sends a sender action (typing_on, typing_off, mark_seen).
     */
    Mono<Void> senderAction(String recipientId, SenderAction action);

    /**
     * Executes a handover protocol action (pass/take thread control).
     * Maps to POST /{id}/pass_thread_control or /{id}/take_thread_control
     */
    Mono<Void> handover(ThreadControl threadControl);

    /**
     * Lists conversations/threads for the current page or IG user.
     * Maps to GET /{id}/conversations
     */
    Mono<GraphApiPage<Conversation>> listConversations();

    /**
     * Lists messages in a conversation.
     * Maps to GET /{conversation-id}/messages
     */
    Mono<GraphApiPage<ConversationMessage>> listMessages(String conversationId);

    /**
     * Deletes a message.
     * Maps to DELETE /{message-id}
     */
    Mono<Void> deleteMessage(String messageId);
}
