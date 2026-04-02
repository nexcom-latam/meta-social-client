package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic wrapper for paginated Graph API responses.
 * <pre>
 * {
 *   "data": [...],
 *   "paging": {
 *     "cursors": { "before": "...", "after": "..." },
 *     "next": "https://graph.facebook.com/...",
 *     "previous": "https://graph.facebook.com/..."
 *   }
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GraphApiPage<T>(
        List<T> data,
        Paging paging
) {
    public record Paging(
            Cursors cursors,
            String next,
            String previous
    ) {}

    public record Cursors(
            String before,
            String after
    ) {}

    /** Returns true if there is a next page URL. */
    public boolean hasNext() {
        return paging != null && paging.next() != null && !paging.next().isBlank();
    }

    /** Returns the next page URL, or null if no more pages. */
    public String nextUrl() {
        return hasNext() ? paging.next() : null;
    }
}
