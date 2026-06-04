package com.nexcom.channels.meta.model.webhook;

/**
 * Reply context attached to an inbound DM message.
 *
 * <p>Meta surfaces two kinds of replies in the same {@code message.reply_to} block:
 * <ul>
 *   <li>Quote-reply to a previous DM → only {@code mid} is set.</li>
 *   <li>Reply to a story (Instagram) → {@code storyId} and {@code storyUrl} are set.
 *       The {@code storyUrl} is a Meta CDN-signed URL with ~24h TTL, so callers
 *       should snapshot it to durable storage on receipt.</li>
 * </ul>
 * Any field may be {@code null}.
 */
public record ReplyContext(
        String mid,
        String storyId,
        String storyUrl
) {
    public static ReplyContext mid(String mid) {
        return new ReplyContext(mid, null, null);
    }

    public static ReplyContext story(String storyId, String storyUrl) {
        return new ReplyContext(null, storyId, storyUrl);
    }

    public boolean isStoryReply() {
        return storyId != null || storyUrl != null;
    }
}
