package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handover protocol request payload.
 * Used with POST /{id}/pass_thread_control or /{id}/take_thread_control.
 *
 * @param action      PASS_THREAD or TAKE_THREAD
 * @param recipientId the user whose thread is being transferred (PSID or IGSID)
 * @param targetAppId the app to pass control to (required for PASS_THREAD, ignored for TAKE_THREAD)
 * @param metadata    optional metadata string passed to the receiving app
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ThreadControl(
        HandoverAction action,
        @JsonProperty("recipient") SendRequest.Recipient recipient,
        @JsonProperty("target_app_id") String targetAppId,
        @JsonProperty("metadata") String metadata
) {
    public static ThreadControl passThread(String recipientId, String targetAppId, String metadata) {
        return new ThreadControl(
                HandoverAction.PASS_THREAD,
                new SendRequest.Recipient(recipientId),
                targetAppId,
                metadata
        );
    }

    public static ThreadControl takeThread(String recipientId, String metadata) {
        return new ThreadControl(
                HandoverAction.TAKE_THREAD,
                new SendRequest.Recipient(recipientId),
                null,
                metadata
        );
    }
}
