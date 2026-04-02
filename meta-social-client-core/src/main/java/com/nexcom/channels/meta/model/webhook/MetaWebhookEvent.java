package com.nexcom.channels.meta.model.webhook;

/**
 * Root of all webhook events. Sealed to the two channel interfaces.
 * <p>
 * Deserialization uses a programmatic discriminator (field-presence in
 * the messaging[] entry + top-level "object" field) rather than
 * Jackson @JsonTypeInfo, because Meta's webhook format does not include
 * an explicit type discriminator.
 */
public sealed interface MetaWebhookEvent
        permits InstagramWebhookEvent, FacebookWebhookEvent {

    /** Message mid — used as deduplication key. Synthetic UUID for events without a native mid. */
    String mid();

    /** Sender's platform-scoped ID (IGSID or PSID). */
    String senderId();

    /** Recipient's platform-scoped ID. */
    String recipientId();

    /** Event timestamp (epoch millis). */
    long timestamp();

    /**
     * Returns a deduplication key for this event.
     * Prefers mid; falls back to senderId:timestamp.
     */
    default String deduplicationKey() {
        return mid() != null ? mid() : senderId() + ":" + timestamp();
    }
}
