package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Outbound message send request. Immutable record that maps to the
 * Graph API POST /{id}/messages payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendRequest(
        @JsonProperty("recipient") Recipient recipient,
        @JsonProperty("messaging_type") MessagingType messagingType,
        @JsonProperty("message") Message message,
        @JsonProperty("sender_action") SenderAction senderAction,
        @JsonProperty("tag") String tag
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Recipient(@JsonProperty("id") String id) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(
            @JsonProperty("text") String text,
            @JsonProperty("attachment") Attachment attachment,
            @JsonProperty("reply_to") ReplyTo replyTo
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Attachment(
            @JsonProperty("type") String type,
            @JsonProperty("payload") Payload payload
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Payload(
            @JsonProperty("url") String url,
            @JsonProperty("attachment_id") String attachmentId
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReplyTo(@JsonProperty("mid") String mid) {}

    // ── Convenience factories ──

    /** Send a plain text message. */
    public static SendRequest text(String recipientId, String text, MessagingType messagingType) {
        return new SendRequest(
                new Recipient(recipientId),
                messagingType,
                new Message(text, null, null),
                null,
                null
        );
    }

    /** Reply to a specific message by mid with text. */
    public static SendRequest reply(String recipientId, String replyToMid, String text, MessagingType messagingType) {
        return new SendRequest(
                new Recipient(recipientId),
                messagingType,
                new Message(text, null, new ReplyTo(replyToMid)),
                null,
                null
        );
    }

    /** Send a media attachment by URL (image, video, audio, file). */
    public static SendRequest mediaByUrl(String recipientId, String type, String url, MessagingType messagingType) {
        return new SendRequest(
                new Recipient(recipientId),
                messagingType,
                new Message(null, new Attachment(type, new Payload(url, null)), null),
                null,
                null
        );
    }

    /** Send a media attachment by previously-uploaded attachment ID. */
    public static SendRequest mediaById(String recipientId, String type, String attachmentId, MessagingType messagingType) {
        return new SendRequest(
                new Recipient(recipientId),
                messagingType,
                new Message(null, new Attachment(type, new Payload(null, attachmentId)), null),
                null,
                null
        );
    }

    /** Send a sender action (typing_on, typing_off, mark_seen). */
    public static SendRequest senderAction(String recipientId, SenderAction action) {
        return new SendRequest(
                new Recipient(recipientId),
                null,
                null,
                action,
                null
        );
    }

    /** Returns a copy of this request with the given message tag added. */
    public SendRequest withTag(MessageTag tag) {
        return new SendRequest(recipient, messagingType, message, senderAction, tag.getValue());
    }

    /** Returns a copy of this request with reply_to set. */
    public SendRequest withReplyTo(String replyToMid) {
        Message updated = new Message(
                message != null ? message.text() : null,
                message != null ? message.attachment() : null,
                new ReplyTo(replyToMid)
        );
        return new SendRequest(recipient, messagingType, updated, senderAction, tag);
    }
}
