package com.nexcom.channels.meta.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "nexcom.meta")
public record MetaSocialClientProperties(
        String appId,
        String appSecret,
        @DefaultValue("v25.0") String apiVersion,
        @DefaultValue Webhook webhook
) {
    public record Webhook(
            @DefaultValue("/webhooks/meta") String path,
            String verifyToken
    ) {}
}
