package com.nexcom.channels.meta.model;

/**
 * Immutable context carrying all per-request / per-tenant information
 * needed to make Graph API calls.
 *
 * @param tenantId    NexCom tenant identifier
 * @param channel     INSTAGRAM or FACEBOOK
 * @param authPath    how the account was connected (determines base URL and token flow)
 * @param accessToken OAuth access token
 * @param appSecret   Meta App Secret (used for appsecret_proof and webhook HMAC)
 * @param apiVersion  Graph API version to target
 * @param scopedId    the Page ID or Instagram-scoped User ID that owns the conversation
 */
public record MetaApiContext(
        String tenantId,
        MetaChannel channel,
        InstagramAuthPath authPath,
        String accessToken,
        String appSecret,
        MetaApiVersion apiVersion,
        String scopedId
) {
    public MetaApiContext {
        if (tenantId == null || tenantId.isBlank()) throw new IllegalArgumentException("tenantId is required");
        if (channel == null) throw new IllegalArgumentException("channel is required");
        if (authPath == null) throw new IllegalArgumentException("authPath is required");
        if (accessToken == null || accessToken.isBlank()) throw new IllegalArgumentException("accessToken is required");
        if (apiVersion == null) apiVersion = MetaApiVersion.DEFAULT;
    }

    public MetaApiContext(String tenantId, MetaChannel channel, InstagramAuthPath authPath,
                          String accessToken, String appSecret, String scopedId) {
        this(tenantId, channel, authPath, accessToken, appSecret, MetaApiVersion.DEFAULT, scopedId);
    }
}
