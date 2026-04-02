# meta-social-client

![Java](https://img.shields.io/badge/java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4-green.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)

Spring Boot 3 client library for Meta social messaging — Instagram DM and Facebook Messenger via Meta Graph API.

## What it does

- **Send messages** — text, media (by URL or attachment ID), thread replies, private replies to comments
- **Sender actions** — typing indicators, mark-seen
- **Handover protocol** — pass/take thread control between apps
- **Conversations** — list threads with cursor-based pagination
- **Media** — upload, retrieve CDN URLs, delete attachments
- **Token exchange** — exchange short-lived tokens for long-lived, refresh before expiry
- **Webhooks** — receive, verify (HMAC-SHA256), parse, and dispatch inbound events
- **17 typed webhook events** — sealed interface hierarchy covering messages, echoes, reactions, postbacks, optins, referrals, mentions, handover, standby for both Instagram and Facebook
- **Auto-configuration** — drops into any Spring Boot 3 app with minimal properties

## What it does NOT do

This is a client library. It does not own runtime concerns:

- No Kafka, Redis, or Vault integration
- No token persistence or refresh scheduling
- No tenant orchestration or business workflow
- No application-level deduplication

Consumers layer those on top of this library's API.

## Requirements

- Java 21+
- Spring Boot 3.x

## Installation

### JitPack

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.nexcom-latam</groupId>
    <artifactId>meta-social-client</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

### Maven Central (planned)

```xml
<dependency>
    <groupId>com.nexcom.channels</groupId>
    <artifactId>meta-social-client-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuration

```yaml
nexcom:
  meta:
    app-id: ${META_APP_ID}
    app-secret: ${META_APP_SECRET}
    api-version: v25.0          # optional, defaults to v25.0
    webhook:
      verify-token: ${META_WEBHOOK_VERIFY_TOKEN}
      path: /webhooks/meta      # optional, defaults to /webhooks/meta
```

## Sending messages

```java
// Create a context for the tenant/channel
MetaApiContext context = new MetaApiContext(
    "tenant_1", MetaChannel.INSTAGRAM, InstagramAuthPath.INSTAGRAM_LOGIN,
    accessToken, appSecret, MetaApiVersion.DEFAULT, igUserId
);

// Get a messaging client from the factory
MetaMessagingClient client = metaSocialClientFactory.create(context);

// Send a text message
SendRequest request = SendRequest.text(recipientId, "Hello!", MessagingType.RESPONSE);
client.send(request).block();

// Reply to a message
client.reply("m_original_mid", request).block();

// Send media by URL
SendRequest media = SendRequest.mediaByUrl(recipientId, "image",
    "https://example.com/photo.jpg", MessagingType.RESPONSE);
client.send(media).block();

// Sender action
client.senderAction(recipientId, SenderAction.TYPING_ON).block();
```

## Handling webhook events

Implement `MetaWebhookEventHandler<T>` as a Spring `@Component`:

```java
@Component
public class InboundMessageHandler
        implements MetaWebhookEventHandler<InstagramInboundMessage> {

    @Override
    public boolean supports(MetaWebhookEvent event) {
        return event instanceof InstagramInboundMessage;
    }

    @Override
    public Mono<Void> handle(InstagramInboundMessage event) {
        System.out.println("Received: " + event.text());
        return Mono.empty();
    }

    @Override
    public Class<InstagramInboundMessage> eventType() {
        return InstagramInboundMessage.class;
    }
}
```

The library auto-discovers handlers and routes matching events via `DefaultMetaWebhookDispatcher`.

## Webhook setup

1. Set your callback URL in the Meta App Dashboard to `https://yourdomain.com/webhooks/meta`
2. Subscribe to fields: `messages`, `message_echoes`, `message_reactions`, `messaging_seen`, `messaging_postbacks`, `messaging_optins`, `messaging_referral`, `messaging_handover`, `standby`, `mentions`

## Supported webhook event types

| Event | Instagram | Facebook |
|---|---|---|
| Inbound message | `InstagramInboundMessage` | `FacebookInboundMessage` |
| Message echo | `InstagramMessageEcho` | `FacebookMessageEcho` |
| Reaction | `InstagramMessageReaction` | `FacebookMessageReaction` |
| Read/seen | `InstagramMessageSeen` | `FacebookMessageSeen` |
| Postback | — | `FacebookPostback` |
| Opt-in | — | `FacebookOptin` |
| Referral | `InstagramReferral` | `FacebookReferral` |
| Mention | `InstagramMention` | — |
| Handover | `InstagramHandover` | `FacebookHandover` |
| Standby | `InstagramStandby` | `FacebookStandby` |

## Security

- All webhook POSTs validated via HMAC-SHA256 (`X-Hub-Signature-256`)
- `appsecret_proof` automatically appended to all Graph API requests when app secret is configured
- Rate-limit errors retried with exponential backoff (configurable)

## API version

Defaults to `v25.0`. Override per-tenant via `MetaApiVersion.of("v26.0")` in `MetaApiContext`, or globally via `nexcom.meta.api-version` property.

## License

Apache License 2.0
