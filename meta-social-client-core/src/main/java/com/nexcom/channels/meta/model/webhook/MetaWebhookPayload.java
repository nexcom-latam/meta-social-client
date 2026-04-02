package com.nexcom.channels.meta.model.webhook;

import java.util.List;

/**
 * Raw webhook envelope as received from Meta.
 * <pre>
 * {
 *   "object": "instagram" | "page",
 *   "entry": [{ "id": "...", "time": ..., "messaging": [...] }]
 * }
 * </pre>
 */
public record MetaWebhookPayload(
        String object,
        List<WebhookEntry> entry
) {}
