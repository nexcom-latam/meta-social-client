# meta-social-client — Architecture

Spring Boot 3 client library for Instagram DM and Facebook Messenger
via Meta Graph API.

---

## Scope

This library owns the HTTP boundary with Meta and the object model around it.

**Does:**
endpoint resolution, outbound messaging, media upload/download, token exchange,
webhook signature verification, webhook parsing, typed event objects,
webhook subscription management, cursor-based pagination, auto-pagination,
retry on rate-limit errors, `appsecret_proof` injection, Micrometer hooks,
Spring Boot auto-configuration.

**Does not:**
Kafka, Redis, Vault, token persistence, token refresh scheduling, tenant
orchestration, deduplication, business logic, application runtime.

---

## Packages

```
com.nexcom.channels.meta
├── api                  # Public interfaces — consumer-facing contracts
├── auth                 # HMAC utilities (appsecret_proof, webhook signature)
├── autoconfigure        # Spring Boot auto-configuration + properties
├── client               # HTTP client internals + default implementations
├── exception            # Typed exception hierarchy
├── metrics              # Micrometer counter hooks
├── model                # Domain records + enums
├── model.webhook        # Sealed webhook event hierarchy + envelope records
└── webhook              # Webhook controller, parser, dispatcher, validator
```

---

## api (7 interfaces)

| Interface | Purpose |
|---|---|
| `MetaSocialClientFactory` | Creates bound client instances from `MetaApiContext` |
| `MetaMessagingClient` | send, reply, privateReply, senderAction, handover, listConversations, listMessages, deleteMessage |
| `MetaMediaClient` | upload, retrieveUrl, delete |
| `MetaTokenExchangeClient` | exchangeForLongLived, refresh |
| `MetaWebhookSubscriptionClient` | subscribe(fields), unsubscribe |
| `MetaWebhookDispatcher` | dispatch(MetaWebhookEvent) |
| `MetaWebhookEventHandler\<T\>` | supports, handle, eventType — consumer SPI |

---

## client (11 classes)

| Class | Role |
|---|---|
| `MetaGraphApiClient` | Interface: post, postMultipart, get, getPaged, getAll, delete |
| `WebClientMetaGraphApiClient` | WebClient implementation with retry + auth attributes |
| `MetaAuthFilter` | ExchangeFilterFunction: Bearer token + appsecret_proof |
| `MetaRetryFilter` | Reactor Retry spec: exponential backoff on rate-limit |
| `MetaApiErrorHandler` | Maps Graph API error JSON → typed exceptions |
| `MetaEndpointResolver` | URL resolution by auth path + version |
| `MetaWebClientConfiguration` | WebClient builder |
| `DefaultMetaMessagingClient` | Implements MetaMessagingClient |
| `DefaultMetaMediaClient` | Implements MetaMediaClient |
| `DefaultMetaTokenExchangeClient` | Implements MetaTokenExchangeClient |
| `DefaultMetaWebhookSubscriptionClient` | Implements MetaWebhookSubscriptionClient |
| `DefaultMetaSocialClientFactory` | Implements MetaSocialClientFactory |

---

## model (15 records/enums)

| Type | Kind |
|---|---|
| `MetaChannel` | enum: INSTAGRAM, FACEBOOK |
| `MetaApiVersion` | record: runtime-configurable, DEFAULT = v25.0 |
| `InstagramAuthPath` | enum: INSTAGRAM_LOGIN, FACEBOOK_PAGE |
| `MetaApiContext` | record: tenantId, channel, authPath, accessToken, appSecret, apiVersion, scopedId |
| `MessagingType` | enum: RESPONSE, UPDATE, MESSAGE_TAG |
| `SenderAction` | enum: TYPING_ON, TYPING_OFF, MARK_SEEN |
| `AttachmentType` | enum: TEXT, IMAGE, VIDEO, AUDIO, FILE, IG_POST, STORY_MENTION, REEL, SHARE |
| `HandoverAction` | enum: PASS_THREAD, TAKE_THREAD |
| `MessageTag` | enum: HUMAN_AGENT |
| `SendRequest` | record: recipient, messagingType, message, senderAction, tag + factories |
| `SendResponse` | record: recipientId, messageId |
| `PrivateReplyRequest` | record: message |
| `ThreadControl` | record: action, recipient, targetAppId, metadata |
| `Conversation` | record: id, updatedTime, snippet, messageCount, canReply, link |
| `ConversationMessage` | record: id, message, from, to, createdTime |
| `GraphApiPage\<T\>` | record: data, paging (cursors, next, previous) + hasNext(), nextUrl() |
| `MediaUploadResponse` | record: attachmentId |
| `MediaUrlResponse` | record: url, mimeType, size |
| `TokenExchangeResponse` | record: accessToken, tokenType, expiresIn |

---

## model.webhook (21 types)

### Hierarchy

```
MetaWebhookEvent (sealed)
├── InstagramWebhookEvent (sealed)
│   ├── InstagramInboundMessage
│   ├── InstagramMessageEcho
│   ├── InstagramMessageReaction
│   ├── InstagramMessageSeen
│   ├── InstagramReferral
│   ├── InstagramMention
│   ├── InstagramHandover
│   └── InstagramStandby
└── FacebookWebhookEvent (sealed)
    ├── FacebookInboundMessage
    ├── FacebookMessageEcho
    ├── FacebookMessageReaction
    ├── FacebookMessageSeen
    ├── FacebookPostback
    ├── FacebookOptin
    ├── FacebookReferral
    ├── FacebookHandover
    └── FacebookStandby
```

All 17 concrete types are Java records. All are emitted by the parser.

### Envelope records (internal to parser)

`MetaWebhookPayload` → `WebhookEntry` → `WebhookMessaging`

Supporting: `WebhookSender`, `WebhookAttachment`

### Deserialization strategy

Programmatic field-presence discriminator (not `@JsonTypeInfo`), because
Meta's webhook format uses which field is non-null in `messaging[]`
combined with the top-level `object` field (`"instagram"` / `"page"`)
to determine event type.

---

## webhook (4 classes)

| Class | Role |
|---|---|
| `MetaWebhookController` | GET hub challenge + POST receive → validate → parse → dispatch |
| `MetaWebhookSignatureValidator` | HMAC-SHA256 of raw body vs X-Hub-Signature-256 |
| `MetaWebhookParser` | JSON → List\<MetaWebhookEvent\> (two-phase field-presence) |
| `DefaultMetaWebhookDispatcher` | Routes to MetaWebhookEventHandler beans, error-isolated |

---

## Endpoint resolution

`MetaEndpointResolver` resolves URLs by `InstagramAuthPath` + `MetaApiVersion`:

| Auth path | Base URL |
|---|---|
| INSTAGRAM_LOGIN | `https://graph.instagram.com/{version}` |
| FACEBOOK_PAGE | `https://graph.facebook.com/{version}` |

Endpoints: messages, privateReplies, threadControl, conversations,
conversationMessages, message, mediaAttachment, media, subscribedApps,
tokenExchange.

`MetaApiVersion` is a record, not an enum. `MetaApiVersion.DEFAULT` is `v25.0`.
Consumers override per-tenant with `MetaApiVersion.of("v26.0")` or globally
via `nexcom.meta.api-version`.

---

## Auth

- `MetaAuthFilter` injects `Authorization: Bearer {token}` and appends
  `appsecret_proof` query parameter on every request when app secret is present.
- `HmacUtils` provides `hmacSha256Hex`, `appSecretProof`, `verifyWebhookSignature`.
- Token management is the consumer's responsibility. This library provides
  `MetaTokenExchangeClient` for the HTTP calls (exchange, refresh) but does
  not store, cache, or schedule tokens.

---

## Retry

`MetaRetryFilter` provides a Reactor `Retry` spec that retries on
`MetaRateLimitException` (codes 4, 32, 613) with exponential backoff.
Default: 3 attempts, 1s initial backoff. Wired into all
`WebClientMetaGraphApiClient` methods.

---

## Pagination

`GraphApiPage<T>` models the standard `{data, paging}` envelope.
Two access patterns:

- **Manual:** `MetaGraphApiClient.getPaged()` → `Mono<GraphApiPage<T>>`
- **Auto:** `MetaGraphApiClient.getAll()` → `Flux<T>` (follows `paging.next`)

Both are available. `getAll` is a default method built on `getPaged`.

---

## Metrics

`MetaClientMetrics` (optional, conditional on `MeterRegistry`):

- `meta.client.send.success` (channel)
- `meta.client.send.error` (channel, error_type)
- `meta.webhook.received` (channel, event_type)
- `meta.webhook.signature.failure`

---

## Auto-configuration

Activates when `nexcom.meta.app-secret` is set. Registers:
webhook validator, parser, dispatcher, controller, endpoint resolver,
error handler, graph API client, client factory, metrics (if MeterRegistry present).

```yaml
nexcom:
  meta:
    app-id: ${META_APP_ID}
    app-secret: ${META_APP_SECRET}
    api-version: v25.0
    webhook:
      verify-token: ${META_WEBHOOK_VERIFY_TOKEN}
      path: /webhooks/meta
```

---

## Build

Gradle (Kotlin DSL), multi-module. Java 21 toolchain. Spring Boot 3.4.4 BOM
via `io.spring.dependency-management` plugin.

## Dependencies

| Scope | Artifact |
|---|---|
| api | spring-boot-starter-webflux, spring-boot-starter-actuator, spring-boot-autoconfigure, jackson-databind |
| annotationProcessor | spring-boot-configuration-processor |
| testImplementation | spring-boot-starter-test, reactor-test, okhttp3:mockwebserver |

No Kafka. No Redis. No Vault. No Resilience4j.

---

## Tests

129 tests across 21 test classes. 19 JSON webhook fixtures.
Every public class has test coverage.
