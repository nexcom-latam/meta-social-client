package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendRequestTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void text_factory_producesCorrectStructure() {
        SendRequest req = SendRequest.text("user_123", "Hello", MessagingType.RESPONSE);

        assertThat(req.recipient().id()).isEqualTo("user_123");
        assertThat(req.messagingType()).isEqualTo(MessagingType.RESPONSE);
        assertThat(req.message().text()).isEqualTo("Hello");
        assertThat(req.message().attachment()).isNull();
        assertThat(req.message().replyTo()).isNull();
        assertThat(req.senderAction()).isNull();
        assertThat(req.tag()).isNull();
    }

    @Test
    void reply_factory_setsReplyToMid() {
        SendRequest req = SendRequest.reply("user_123", "m_original", "Thanks!", MessagingType.RESPONSE);

        assertThat(req.message().text()).isEqualTo("Thanks!");
        assertThat(req.message().replyTo()).isNotNull();
        assertThat(req.message().replyTo().mid()).isEqualTo("m_original");
    }

    @Test
    void mediaByUrl_factory_setsAttachment() {
        SendRequest req = SendRequest.mediaByUrl("user_123", "image", "https://example.com/img.jpg", MessagingType.RESPONSE);

        assertThat(req.message().text()).isNull();
        assertThat(req.message().attachment()).isNotNull();
        assertThat(req.message().attachment().type()).isEqualTo("image");
        assertThat(req.message().attachment().payload().url()).isEqualTo("https://example.com/img.jpg");
        assertThat(req.message().attachment().payload().attachmentId()).isNull();
    }

    @Test
    void mediaById_factory_setsAttachmentId() {
        SendRequest req = SendRequest.mediaById("user_123", "video", "att_456", MessagingType.UPDATE);

        assertThat(req.message().attachment().payload().attachmentId()).isEqualTo("att_456");
        assertThat(req.message().attachment().payload().url()).isNull();
        assertThat(req.messagingType()).isEqualTo(MessagingType.UPDATE);
    }

    @Test
    void withTag_returnsNewInstanceWithTag() {
        SendRequest original = SendRequest.text("user_123", "Hello", MessagingType.MESSAGE_TAG);
        SendRequest tagged = original.withTag(MessageTag.HUMAN_AGENT);

        assertThat(tagged.tag()).isEqualTo("HUMAN_AGENT");
        assertThat(tagged.message().text()).isEqualTo("Hello");
        // original unchanged
        assertThat(original.tag()).isNull();
    }

    @Test
    void withReplyTo_returnsNewInstanceWithReplyTo() {
        SendRequest original = SendRequest.text("user_123", "Hello", MessagingType.RESPONSE);
        SendRequest replied = original.withReplyTo("m_prev");

        assertThat(replied.message().replyTo().mid()).isEqualTo("m_prev");
        assertThat(replied.message().text()).isEqualTo("Hello");
        // original unchanged
        assertThat(original.message().replyTo()).isNull();
    }

    @Test
    void senderAction_factory_noMessage() {
        SendRequest req = SendRequest.senderAction("user_123", SenderAction.TYPING_ON);

        assertThat(req.senderAction()).isEqualTo(SenderAction.TYPING_ON);
        assertThat(req.message()).isNull();
        assertThat(req.messagingType()).isNull();
    }

    @Test
    void text_serializesToCorrectJson() throws Exception {
        SendRequest req = SendRequest.text("user_123", "Hello", MessagingType.RESPONSE);
        String json = mapper.writeValueAsString(req);

        assertThat(json).contains("\"recipient\":{\"id\":\"user_123\"}");
        assertThat(json).contains("\"messaging_type\":\"RESPONSE\"");
        assertThat(json).contains("\"message\":{\"text\":\"Hello\"}");
        assertThat(json).doesNotContain("sender_action");
        assertThat(json).doesNotContain("\"tag\"");
    }

    @Test
    void mediaByUrl_serializesAttachmentCorrectly() throws Exception {
        SendRequest req = SendRequest.mediaByUrl("u1", "image", "https://cdn.example.com/a.jpg", MessagingType.RESPONSE);
        String json = mapper.writeValueAsString(req);

        assertThat(json).contains("\"type\":\"image\"");
        assertThat(json).contains("\"url\":\"https://cdn.example.com/a.jpg\"");
        assertThat(json).doesNotContain("attachment_id");
    }

    @Test
    void withTag_serializesTagField() throws Exception {
        SendRequest req = SendRequest.text("u1", "Help", MessagingType.MESSAGE_TAG)
                .withTag(MessageTag.HUMAN_AGENT);
        String json = mapper.writeValueAsString(req);

        assertThat(json).contains("\"tag\":\"HUMAN_AGENT\"");
        assertThat(json).contains("\"messaging_type\":\"MESSAGE_TAG\"");
    }
}
