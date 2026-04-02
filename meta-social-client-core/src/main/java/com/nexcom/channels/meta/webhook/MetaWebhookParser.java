package com.nexcom.channels.meta.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.model.AttachmentType;
import com.nexcom.channels.meta.model.webhook.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Two-phase webhook parser:
 * <ol>
 *   <li>Deserialize raw JSON into {@link MetaWebhookPayload} (envelope)</li>
 *   <li>For each messaging[] entry, inspect field presence + top-level object
 *       to build the correct concrete event record</li>
 * </ol>
 * Also handles entry.changes[] for Instagram @mentions via Page subscriptions.
 */
public class MetaWebhookParser {

    private static final Logger log = LoggerFactory.getLogger(MetaWebhookParser.class);

    private final ObjectMapper objectMapper;

    public MetaWebhookParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<MetaWebhookEvent> parse(String rawJson) {
        MetaWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawJson, MetaWebhookPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload", e);
            return List.of();
        }

        if (payload == null || payload.entry() == null) {
            return List.of();
        }

        boolean isInstagram = "instagram".equals(payload.object());
        List<MetaWebhookEvent> events = new ArrayList<>();

        for (WebhookEntry entry : payload.entry()) {
            // Standard messaging[] events
            if (entry.messaging() != null) {
                for (WebhookMessaging msg : entry.messaging()) {
                    parseMessaging(msg, isInstagram).ifPresent(events::add);
                }
            }

            // Instagram @mentions arrive via changes[] (Page subscription path)
            if (isInstagram && entry.changes() != null) {
                for (JsonNode change : entry.changes()) {
                    parseMentionFromChanges(change, entry.id(), entry.time()).ifPresent(events::add);
                }
            }
        }

        return Collections.unmodifiableList(events);
    }

    private Optional<MetaWebhookEvent> parseMessaging(WebhookMessaging msg, boolean isInstagram) {
        String senderId = msg.sender() != null ? msg.sender().id() : null;
        String recipientId = msg.recipient() != null ? msg.recipient().id() : null;
        long timestamp = msg.timestamp();

        // Field-presence dispatch (priority order from architecture spec)
        if (msg.message() != null) {
            return parseMessage(msg.message(), senderId, recipientId, timestamp, isInstagram);
        }
        if (msg.reaction() != null) {
            return parseReaction(msg.reaction(), senderId, recipientId, timestamp, isInstagram);
        }
        if (msg.read() != null) {
            return parseRead(msg.read(), senderId, recipientId, timestamp, isInstagram);
        }
        if (msg.postback() != null && !isInstagram) {
            return parsePostback(msg.postback(), senderId, recipientId, timestamp);
        }
        if (msg.optin() != null && !isInstagram) {
            return parseOptin(msg.optin(), senderId, recipientId, timestamp);
        }
        if (msg.referral() != null) {
            return parseReferral(msg.referral(), senderId, recipientId, timestamp, isInstagram);
        }
        if (msg.passThreadControl() != null) {
            return parseHandover(msg.passThreadControl(), senderId, recipientId, timestamp, isInstagram, true);
        }
        if (msg.takeThreadControl() != null) {
            return parseHandover(msg.takeThreadControl(), senderId, recipientId, timestamp, isInstagram, false);
        }
        if (msg.standby() != null) {
            return parseStandby(msg.standby(), senderId, recipientId, timestamp, isInstagram);
        }

        log.warn("Unknown webhook messaging structure for sender={}", senderId);
        return Optional.empty();
    }

    // ── message (inbound + echo + mention via story_mention attachment) ──

    private Optional<MetaWebhookEvent> parseMessage(JsonNode messageNode, String senderId,
                                                     String recipientId, long timestamp, boolean isInstagram) {
        String mid = textOrNull(messageNode, "mid");
        String text = textOrNull(messageNode, "text");
        List<WebhookAttachment> attachments = parseAttachments(messageNode);

        // Echo detection
        if (messageNode.has("is_echo") && messageNode.get("is_echo").asBoolean()) {
            String appId = textOrNull(messageNode, "app_id");
            if (isInstagram) {
                return Optional.of(new InstagramMessageEcho(mid, senderId, recipientId, timestamp, appId, text, attachments));
            } else {
                return Optional.of(new FacebookMessageEcho(mid, senderId, recipientId, timestamp, appId, text, attachments));
            }
        }

        // Instagram story_mention detection (arrives as attachment type)
        if (isInstagram && hasAttachmentType(attachments, AttachmentType.STORY_MENTION)) {
            WebhookAttachment mention = attachments.stream()
                    .filter(a -> a.type() == AttachmentType.STORY_MENTION)
                    .findFirst().orElse(null);
            String mediaUrl = mention != null && mention.payload() != null ? mention.payload().url() : null;
            return Optional.of(new InstagramMention(mid, senderId, recipientId, timestamp, null, mediaUrl, mediaUrl));
        }

        String replyToMid = null;
        if (messageNode.has("reply_to") && messageNode.get("reply_to").has("mid")) {
            replyToMid = messageNode.get("reply_to").get("mid").asText();
        }

        if (isInstagram) {
            return Optional.of(new InstagramInboundMessage(mid, senderId, recipientId, timestamp, text, attachments, replyToMid));
        } else {
            return Optional.of(new FacebookInboundMessage(mid, senderId, recipientId, timestamp, text, attachments, replyToMid));
        }
    }

    // ── reaction ──

    private Optional<MetaWebhookEvent> parseReaction(JsonNode node, String senderId,
                                                      String recipientId, long timestamp, boolean isInstagram) {
        String mid = textOrNull(node, "mid");
        String action = textOrNull(node, "action");
        String emoji = textOrNull(node, "emoji");

        if (isInstagram) {
            return Optional.of(new InstagramMessageReaction(mid, senderId, recipientId, timestamp, action, emoji));
        } else {
            return Optional.of(new FacebookMessageReaction(mid, senderId, recipientId, timestamp, action, emoji));
        }
    }

    // ── read ──

    private Optional<MetaWebhookEvent> parseRead(JsonNode readNode, String senderId,
                                                   String recipientId, long timestamp, boolean isInstagram) {
        long watermark = readNode.has("watermark") ? readNode.get("watermark").asLong() : 0;
        String syntheticMid = UUID.randomUUID().toString();

        if (isInstagram) {
            return Optional.of(new InstagramMessageSeen(syntheticMid, senderId, recipientId, timestamp, watermark));
        } else {
            return Optional.of(new FacebookMessageSeen(syntheticMid, senderId, recipientId, timestamp, watermark));
        }
    }

    // ── postback (Facebook only) ──

    private Optional<MetaWebhookEvent> parsePostback(JsonNode node, String senderId,
                                                      String recipientId, long timestamp) {
        String mid = textOrNull(node, "mid");
        String title = textOrNull(node, "title");
        String payload = textOrNull(node, "payload");
        String referral = null;
        if (node.has("referral") && node.get("referral").has("ref")) {
            referral = node.get("referral").get("ref").asText();
        }
        return Optional.of(new FacebookPostback(mid, senderId, recipientId, timestamp, title, payload, referral));
    }

    // ── optin (Facebook only) ──

    private Optional<MetaWebhookEvent> parseOptin(JsonNode node, String senderId,
                                                    String recipientId, long timestamp) {
        String syntheticMid = UUID.randomUUID().toString();
        String ref = textOrNull(node, "ref");
        String userRef = textOrNull(node, "user_ref");
        String type = textOrNull(node, "type");
        return Optional.of(new FacebookOptin(syntheticMid, senderId, recipientId, timestamp, ref, userRef, type));
    }

    // ── referral ──

    private Optional<MetaWebhookEvent> parseReferral(JsonNode node, String senderId,
                                                      String recipientId, long timestamp, boolean isInstagram) {
        String syntheticMid = UUID.randomUUID().toString();
        String ref = textOrNull(node, "ref");
        String source = textOrNull(node, "source");
        String type = textOrNull(node, "type");
        String adId = textOrNull(node, "ad_id");

        if (isInstagram) {
            return Optional.of(new InstagramReferral(syntheticMid, senderId, recipientId, timestamp, ref, source, type, adId));
        } else {
            return Optional.of(new FacebookReferral(syntheticMid, senderId, recipientId, timestamp, ref, source, type, adId));
        }
    }

    // ── handover (pass_thread_control / take_thread_control) ──

    private Optional<MetaWebhookEvent> parseHandover(JsonNode node, String senderId,
                                                      String recipientId, long timestamp,
                                                      boolean isInstagram, boolean isPass) {
        String syntheticMid = UUID.randomUUID().toString();
        String newOwnerAppId = isPass ? textOrNull(node, "new_owner_app_id") : null;
        String previousOwnerAppId = !isPass ? textOrNull(node, "previous_owner_app_id") : null;
        String metadata = textOrNull(node, "metadata");

        if (isInstagram) {
            return Optional.of(new InstagramHandover(syntheticMid, senderId, recipientId, timestamp,
                    newOwnerAppId, previousOwnerAppId, metadata));
        } else {
            return Optional.of(new FacebookHandover(syntheticMid, senderId, recipientId, timestamp,
                    newOwnerAppId, previousOwnerAppId, metadata));
        }
    }

    // ── standby (passthrough messages while not primary receiver) ──

    private Optional<MetaWebhookEvent> parseStandby(JsonNode node, String senderId,
                                                     String recipientId, long timestamp, boolean isInstagram) {
        // Standby payload can be an object with message fields or an array.
        // We extract text and attachments from the first message-like structure.
        String syntheticMid = UUID.randomUUID().toString();
        String text = null;
        List<WebhookAttachment> attachments = List.of();

        if (node.isObject()) {
            text = textOrNull(node, "text");
            attachments = parseAttachments(node);
        } else if (node.isArray() && !node.isEmpty()) {
            JsonNode first = node.get(0);
            text = textOrNull(first, "text");
            attachments = parseAttachments(first);
        }

        if (isInstagram) {
            return Optional.of(new InstagramStandby(syntheticMid, senderId, recipientId, timestamp, text, attachments));
        } else {
            return Optional.of(new FacebookStandby(syntheticMid, senderId, recipientId, timestamp, text, attachments));
        }
    }

    // ── Instagram @mentions via entry.changes[] ──

    private Optional<MetaWebhookEvent> parseMentionFromChanges(JsonNode change, String entryId, long entryTime) {
        String field = textOrNull(change, "field");
        if (!"mentions".equals(field)) {
            return Optional.empty();
        }

        JsonNode value = change.get("value");
        if (value == null) return Optional.empty();

        String syntheticMid = UUID.randomUUID().toString();
        String mediaId = textOrNull(value, "media_id");
        String mediaUrl = textOrNull(value, "media");
        String cdnUrl = textOrNull(value, "cdn_url");
        String senderId = textOrNull(value, "sender_id");

        return Optional.of(new InstagramMention(syntheticMid,
                senderId != null ? senderId : "unknown",
                entryId, entryTime, mediaId, mediaUrl, cdnUrl));
    }

    // ── Attachment helpers ──

    private List<WebhookAttachment> parseAttachments(JsonNode messageNode) {
        if (!messageNode.has("attachments")) {
            return List.of();
        }
        JsonNode attachmentsNode = messageNode.get("attachments");
        if (!attachmentsNode.isArray()) {
            return List.of();
        }

        List<WebhookAttachment> result = new ArrayList<>();
        for (JsonNode att : attachmentsNode) {
            AttachmentType type = mapAttachmentType(textOrNull(att, "type"));
            String url = null;
            String stickerId = null;
            if (att.has("payload")) {
                JsonNode payloadNode = att.get("payload");
                url = textOrNull(payloadNode, "url");
                stickerId = textOrNull(payloadNode, "sticker_id");
            }
            result.add(new WebhookAttachment(type, new WebhookAttachment.AttachmentPayload(url, stickerId)));
        }
        return result;
    }

    private AttachmentType mapAttachmentType(String raw) {
        if (raw == null) return AttachmentType.TEXT;

        if ("share".equalsIgnoreCase(raw)) {
            log.warn("Received deprecated 'share' attachment type — mapping to IG_POST");
            return AttachmentType.IG_POST;
        }

        return AttachmentType.fromValue(raw);
    }

    private static boolean hasAttachmentType(List<WebhookAttachment> attachments, AttachmentType type) {
        return attachments.stream().anyMatch(a -> a.type() == type);
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }
}
