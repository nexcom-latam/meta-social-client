package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.model.HandoverAction;
import com.nexcom.channels.meta.model.InstagramAuthPath;
import com.nexcom.channels.meta.model.MetaApiVersion;

/**
 * Resolves Graph API base URLs based on channel + auth path.
 * <ul>
 *   <li>Instagram Login → graph.instagram.com</li>
 *   <li>Facebook Page / Messenger → graph.facebook.com</li>
 * </ul>
 */
public class MetaEndpointResolver {

    private static final String INSTAGRAM_BASE = "https://graph.instagram.com";
    private static final String FACEBOOK_BASE = "https://graph.facebook.com";

    /**
     * Returns the versioned base URL for the given auth path.
     * Example: "https://graph.instagram.com/v25.0"
     */
    public String baseUrl(InstagramAuthPath authPath, MetaApiVersion version) {
        String base = switch (authPath) {
            case INSTAGRAM_LOGIN -> INSTAGRAM_BASE;
            case FACEBOOK_PAGE -> FACEBOOK_BASE;
        };
        return base + "/" + version.getValue();
    }

    /**
     * Returns the full messages endpoint URL.
     * Example: "https://graph.instagram.com/v25.0/{scopedId}/messages"
     */
    public String messagesUrl(InstagramAuthPath authPath, MetaApiVersion version, String scopedId) {
        return baseUrl(authPath, version) + "/" + scopedId + "/messages";
    }

    /**
     * Returns the private reply endpoint URL.
     * Example: "https://graph.facebook.com/v25.0/{commentId}/private_replies"
     */
    public String privateReplyUrl(InstagramAuthPath authPath, MetaApiVersion version, String commentId) {
        return baseUrl(authPath, version) + "/" + commentId + "/private_replies";
    }

    /**
     * Returns the thread control endpoint URL for handover protocol.
     * Example: "https://graph.facebook.com/v25.0/{scopedId}/pass_thread_control"
     */
    public String threadControlUrl(InstagramAuthPath authPath, MetaApiVersion version,
                                   String scopedId, HandoverAction action) {
        return baseUrl(authPath, version) + "/" + scopedId + "/" + action.getEndpoint();
    }

    /**
     * Returns the conversations endpoint URL.
     * Example: "https://graph.facebook.com/v25.0/{scopedId}/conversations"
     */
    public String conversationsUrl(InstagramAuthPath authPath, MetaApiVersion version, String scopedId) {
        return baseUrl(authPath, version) + "/" + scopedId + "/conversations";
    }

    /**
     * Returns the messages endpoint URL for a specific conversation.
     * Example: "https://graph.facebook.com/v25.0/{conversationId}/messages"
     */
    public String conversationMessagesUrl(InstagramAuthPath authPath, MetaApiVersion version, String conversationId) {
        return baseUrl(authPath, version) + "/" + conversationId + "/messages";
    }

    /**
     * Returns a single message URL for retrieve or delete.
     * Example: "https://graph.facebook.com/v25.0/{messageId}"
     */
    public String messageUrl(InstagramAuthPath authPath, MetaApiVersion version, String messageId) {
        return baseUrl(authPath, version) + "/" + messageId;
    }

    /**
     * Returns the media attachment upload endpoint URL.
     * Example: "https://graph.facebook.com/v25.0/{scopedId}/message_attachments"
     */
    public String mediaAttachmentUrl(InstagramAuthPath authPath, MetaApiVersion version, String scopedId) {
        return baseUrl(authPath, version) + "/" + scopedId + "/message_attachments";
    }

    /**
     * Returns the media URL for retrieve or delete operations.
     * Example: "https://graph.facebook.com/v25.0/{attachmentId}"
     */
    public String mediaUrl(InstagramAuthPath authPath, MetaApiVersion version, String attachmentId) {
        return baseUrl(authPath, version) + "/" + attachmentId;
    }

    /**
     * Returns the subscribed_apps endpoint URL for webhook subscription management.
     * Example: "https://graph.facebook.com/v25.0/{scopedId}/subscribed_apps"
     */
    public String subscribedAppsUrl(InstagramAuthPath authPath, MetaApiVersion version, String scopedId) {
        return baseUrl(authPath, version) + "/" + scopedId + "/subscribed_apps";
    }

    /**
     * Returns the token exchange base URL.
     */
    public String tokenExchangeBaseUrl(InstagramAuthPath authPath, MetaApiVersion version) {
        return baseUrl(authPath, version);
    }
}
