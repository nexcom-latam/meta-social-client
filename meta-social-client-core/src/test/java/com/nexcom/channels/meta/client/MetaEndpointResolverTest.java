package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.model.HandoverAction;
import com.nexcom.channels.meta.model.InstagramAuthPath;
import com.nexcom.channels.meta.model.MetaApiVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaEndpointResolverTest {

    private final MetaEndpointResolver resolver = new MetaEndpointResolver();

    @Test
    void baseUrl_instagramLogin_usesInstagramDomain() {
        String url = resolver.baseUrl(InstagramAuthPath.INSTAGRAM_LOGIN, MetaApiVersion.V25_0);
        assertThat(url).isEqualTo("https://graph.instagram.com/v25.0");
    }

    @Test
    void baseUrl_facebookPage_usesFacebookDomain() {
        String url = resolver.baseUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0);
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0");
    }

    @Test
    void messagesUrl_instagramLogin_correctPath() {
        String url = resolver.messagesUrl(InstagramAuthPath.INSTAGRAM_LOGIN, MetaApiVersion.V25_0, "12345");
        assertThat(url).isEqualTo("https://graph.instagram.com/v25.0/12345/messages");
    }

    @Test
    void messagesUrl_facebookPage_correctPath() {
        String url = resolver.messagesUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "67890");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/67890/messages");
    }

    @Test
    void privateReplyUrl_correctPath() {
        String url = resolver.privateReplyUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "comment_123");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/comment_123/private_replies");
    }

    @Test
    void privateReplyUrl_instagramLogin() {
        String url = resolver.privateReplyUrl(InstagramAuthPath.INSTAGRAM_LOGIN, MetaApiVersion.V25_0, "comment_456");
        assertThat(url).isEqualTo("https://graph.instagram.com/v25.0/comment_456/private_replies");
    }

    @Test
    void threadControlUrl_passThread() {
        String url = resolver.threadControlUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "page_1", HandoverAction.PASS_THREAD);
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/page_1/pass_thread_control");
    }

    @Test
    void threadControlUrl_takeThread() {
        String url = resolver.threadControlUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "page_1", HandoverAction.TAKE_THREAD);
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/page_1/take_thread_control");
    }

    @Test
    void mediaAttachmentUrl_correctPath() {
        String url = resolver.mediaAttachmentUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "page_1");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/page_1/message_attachments");
    }

    @Test
    void mediaUrl_correctPath() {
        String url = resolver.mediaUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "att_123");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/att_123");
    }

    @Test
    void conversationsUrl_correctPath() {
        String url = resolver.conversationsUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "page_1");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/page_1/conversations");
    }

    @Test
    void conversationsUrl_instagramLogin() {
        String url = resolver.conversationsUrl(InstagramAuthPath.INSTAGRAM_LOGIN, MetaApiVersion.V25_0, "ig_user_1");
        assertThat(url).isEqualTo("https://graph.instagram.com/v25.0/ig_user_1/conversations");
    }

    @Test
    void conversationMessagesUrl_correctPath() {
        String url = resolver.conversationMessagesUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "conv_1");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/conv_1/messages");
    }

    @Test
    void messageUrl_correctPath() {
        String url = resolver.messageUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "msg_1");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/msg_1");
    }

    @Test
    void subscribedAppsUrl_correctPath() {
        String url = resolver.subscribedAppsUrl(InstagramAuthPath.FACEBOOK_PAGE, MetaApiVersion.V25_0, "page_1");
        assertThat(url).isEqualTo("https://graph.facebook.com/v25.0/page_1/subscribed_apps");
    }

    // ─── Runtime version override ───

    @Test
    void baseUrl_customVersion_usesProvidedVersion() {
        MetaApiVersion v26 = MetaApiVersion.of("v26.0");
        String url = resolver.baseUrl(InstagramAuthPath.FACEBOOK_PAGE, v26);
        assertThat(url).isEqualTo("https://graph.facebook.com/v26.0");
    }

    @Test
    void messagesUrl_customVersion_usesProvidedVersion() {
        MetaApiVersion v26 = MetaApiVersion.of("26.0"); // auto-prefixed with "v"
        String url = resolver.messagesUrl(InstagramAuthPath.INSTAGRAM_LOGIN, v26, "12345");
        assertThat(url).isEqualTo("https://graph.instagram.com/v26.0/12345/messages");
    }
}
