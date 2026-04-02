package com.nexcom.channels.meta.webhook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.model.AttachmentType;
import com.nexcom.channels.meta.model.webhook.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetaWebhookParserTest {

    private MetaWebhookParser parser;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        parser = new MetaWebhookParser(mapper);
    }

    // ─── Instagram ───

    @Test
    void parse_instagramTextMessage_returnsInstagramInboundMessage() {
        List<MetaWebhookEvent> events = parse("instagram/text-message.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramInboundMessage.class);

        InstagramInboundMessage msg = (InstagramInboundMessage) events.getFirst();
        assertThat(msg.mid()).isEqualTo("m_AG4xRHhGQZGFhYjRlYTdiN");
        assertThat(msg.text()).isEqualTo("Hello from Instagram!");
        assertThat(msg.senderId()).isEqualTo("9876543210987654");
        assertThat(msg.recipientId()).isEqualTo("1234567890123456");
        assertThat(msg.attachments()).isEmpty();
    }

    @Test
    void parse_instagramImageMessage_hasImageAttachment() {
        List<MetaWebhookEvent> events = parse("instagram/image-message.json");

        assertThat(events).hasSize(1);
        InstagramInboundMessage msg = (InstagramInboundMessage) events.getFirst();
        assertThat(msg.text()).isNull();
        assertThat(msg.attachments()).hasSize(1);
        assertThat(msg.attachments().getFirst().type()).isEqualTo(AttachmentType.IMAGE);
        assertThat(msg.attachments().getFirst().payload().url())
                .contains("scontent.cdninstagram.com");
    }

    @Test
    void parse_instagramSeen_returnsInstagramMessageSeen() {
        List<MetaWebhookEvent> events = parse("instagram/messaging-seen.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramMessageSeen.class);

        InstagramMessageSeen seen = (InstagramMessageSeen) events.getFirst();
        assertThat(seen.watermark()).isEqualTo(1712000001500L);
        assertThat(seen.mid()).isNotBlank(); // synthetic UUID
    }

    @Test
    void parse_legacyShare_mapsToIG_POST() {
        List<MetaWebhookEvent> events = parse("instagram/legacy-share.json");

        assertThat(events).hasSize(1);
        InstagramInboundMessage msg = (InstagramInboundMessage) events.getFirst();
        assertThat(msg.attachments()).hasSize(1);
        // legacy "share" maps to IG_POST
        assertThat(msg.attachments().getFirst().type()).isEqualTo(AttachmentType.IG_POST);
    }

    // ─── Facebook ───

    @Test
    void parse_facebookTextMessage_returnsFacebookInboundMessage() {
        List<MetaWebhookEvent> events = parse("facebook/text-message.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookInboundMessage.class);

        FacebookInboundMessage msg = (FacebookInboundMessage) events.getFirst();
        assertThat(msg.mid()).isEqualTo("m_DL7ATKkJTcJIkBmUoAwQe");
        assertThat(msg.text()).isEqualTo("Hello from Messenger!");
        assertThat(msg.senderId()).isEqualTo("1111222233334444");
    }

    @Test
    void parse_facebookMediaMessage_hasAttachment() {
        List<MetaWebhookEvent> events = parse("facebook/media-message.json");

        assertThat(events).hasSize(1);
        FacebookInboundMessage msg = (FacebookInboundMessage) events.getFirst();
        assertThat(msg.attachments()).hasSize(1);
        assertThat(msg.attachments().getFirst().type()).isEqualTo(AttachmentType.VIDEO);
    }

    @Test
    void parse_facebookSeen_returnsFacebookMessageSeen() {
        List<MetaWebhookEvent> events = parse("facebook/messaging-seen.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookMessageSeen.class);

        FacebookMessageSeen seen = (FacebookMessageSeen) events.getFirst();
        assertThat(seen.watermark()).isEqualTo(1712000010500L);
    }

    // ─── Instagram Phase 3 event types ───

    @Test
    void parse_instagramMessageEcho_returnsInstagramMessageEcho() {
        List<MetaWebhookEvent> events = parse("instagram/message-echo.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramMessageEcho.class);

        InstagramMessageEcho echo = (InstagramMessageEcho) events.getFirst();
        assertThat(echo.mid()).isEqualTo("m_echo_IG01");
        assertThat(echo.appId()).isEqualTo("app_111");
        assertThat(echo.text()).isEqualTo("Echo from IG");
    }

    @Test
    void parse_instagramReaction_returnsInstagramMessageReaction() {
        List<MetaWebhookEvent> events = parse("instagram/message-reaction.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramMessageReaction.class);

        InstagramMessageReaction reaction = (InstagramMessageReaction) events.getFirst();
        assertThat(reaction.mid()).isEqualTo("m_reacted_IG01");
        assertThat(reaction.action()).isEqualTo("react");
        assertThat(reaction.emoji()).isEqualTo("\uD83D\uDC4D");
    }

    @Test
    void parse_instagramReferral_returnsInstagramReferral() {
        List<MetaWebhookEvent> events = parse("instagram/messaging-referral.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramReferral.class);

        InstagramReferral ref = (InstagramReferral) events.getFirst();
        assertThat(ref.ref()).isEqualTo("promo_summer");
        assertThat(ref.source()).isEqualTo("SHORTLINK");
        assertThat(ref.adId()).isEqualTo("ad_999");
    }

    @Test
    void parse_storyMention_returnsInstagramMention() {
        List<MetaWebhookEvent> events = parse("instagram/story-mention.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramMention.class);

        InstagramMention mention = (InstagramMention) events.getFirst();
        assertThat(mention.mediaUrl()).contains("scontent.cdninstagram.com");
    }

    @Test
    void parse_instagramHandover_returnsInstagramHandover() {
        List<MetaWebhookEvent> events = parse("instagram/handover.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramHandover.class);

        InstagramHandover handover = (InstagramHandover) events.getFirst();
        assertThat(handover.newOwnerAppId()).isEqualTo("app_new_222");
        assertThat(handover.metadata()).isEqualTo("Passing to human agent");
    }

    @Test
    void parse_instagramMentionFromChanges_returnsInstagramMention() {
        List<MetaWebhookEvent> events = parse("instagram/mention-changes.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramMention.class);

        InstagramMention mention = (InstagramMention) events.getFirst();
        assertThat(mention.mediaId()).isEqualTo("media_789");
        assertThat(mention.senderId()).isEqualTo("9876543210987654");
    }

    // ─── Facebook Phase 3 event types ───

    @Test
    void parse_facebookPostback_returnsFacebookPostback() {
        List<MetaWebhookEvent> events = parse("facebook/postback.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookPostback.class);

        FacebookPostback postback = (FacebookPostback) events.getFirst();
        assertThat(postback.title()).isEqualTo("Get Started");
        assertThat(postback.payload()).isEqualTo("GET_STARTED_PAYLOAD");
        assertThat(postback.referral()).isEqualTo("ad_campaign_1");
    }

    @Test
    void parse_facebookOptin_returnsFacebookOptin() {
        List<MetaWebhookEvent> events = parse("facebook/optin.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookOptin.class);

        FacebookOptin optin = (FacebookOptin) events.getFirst();
        assertThat(optin.ref()).isEqualTo("optin_ref_123");
        assertThat(optin.userRef()).isEqualTo("user_ref_456");
        assertThat(optin.type()).isEqualTo("one_time_notif_req");
    }

    @Test
    void parse_facebookHandover_takeThread_returnsFacebookHandover() {
        List<MetaWebhookEvent> events = parse("facebook/handover.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookHandover.class);

        FacebookHandover handover = (FacebookHandover) events.getFirst();
        assertThat(handover.previousOwnerAppId()).isEqualTo("app_old_333");
        assertThat(handover.metadata()).isEqualTo("Bot taking over");
    }

    @Test
    void parse_facebookReaction_returnsFacebookMessageReaction() {
        List<MetaWebhookEvent> events = parse("facebook/message-reaction.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookMessageReaction.class);

        FacebookMessageReaction reaction = (FacebookMessageReaction) events.getFirst();
        assertThat(reaction.action()).isEqualTo("unreact");
    }

    // ─── Standby events ───

    @Test
    void parse_instagramStandby_returnsInstagramStandby() {
        List<MetaWebhookEvent> events = parse("instagram/standby.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InstagramStandby.class);

        InstagramStandby standby = (InstagramStandby) events.getFirst();
        assertThat(standby.text()).isEqualTo("Standby message from IG");
    }

    @Test
    void parse_facebookStandby_returnsFacebookStandby_withAttachments() {
        List<MetaWebhookEvent> events = parse("facebook/standby.json");

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(FacebookStandby.class);

        FacebookStandby standby = (FacebookStandby) events.getFirst();
        assertThat(standby.text()).isEqualTo("Standby message from Messenger");
        assertThat(standby.attachments()).hasSize(1);
        assertThat(standby.attachments().getFirst().type()).isEqualTo(AttachmentType.IMAGE);
    }

    // ─── Edge cases ───

    @Test
    void parse_unknownPayload_returnsEmptyList() {
        List<MetaWebhookEvent> events = parser.parse("{\"object\":\"page\",\"entry\":[{\"id\":\"1\",\"time\":0,\"messaging\":[{\"sender\":{\"id\":\"1\"},\"recipient\":{\"id\":\"2\"},\"timestamp\":0}]}]}");
        assertThat(events).isEmpty();
    }

    @Test
    void parse_malformedJson_returnsEmptyList() {
        List<MetaWebhookEvent> events = parser.parse("not json");
        assertThat(events).isEmpty();
    }

    // ─── Helpers ───

    private List<MetaWebhookEvent> parse(String fixturePath) {
        String json = loadFixture(fixturePath);
        return parser.parse(json);
    }

    private String loadFixture(String path) {
        try (InputStream is = getClass().getResourceAsStream("/fixtures/webhook/" + path)) {
            if (is == null) throw new IllegalStateException("Fixture not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
