package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.exception.MetaRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Provides a Reactor {@link Retry} spec for Graph API calls.
 * Retries on {@link MetaRateLimitException} with exponential backoff,
 * capped at {@code maxAttempts}.
 */
public final class MetaRetryFilter {

    private static final Logger log = LoggerFactory.getLogger(MetaRetryFilter.class);

    private MetaRetryFilter() {}

    /**
     * Creates a retry spec that retries rate-limit errors up to {@code maxAttempts} times
     * with exponential backoff starting at {@code initialBackoff}.
     */
    public static Retry rateLimitRetry(int maxAttempts, Duration initialBackoff) {
        return Retry.backoff(maxAttempts, initialBackoff)
                .filter(throwable -> throwable instanceof MetaRateLimitException)
                .doBeforeRetry(signal -> log.warn(
                        "Retrying after rate limit (attempt {}/{}): {}",
                        signal.totalRetries() + 1, maxAttempts, signal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    /**
     * Default retry spec: 3 attempts, starting at 1 second backoff.
     */
    public static Retry defaultRetry() {
        return rateLimitRetry(3, Duration.ofSeconds(1));
    }
}
