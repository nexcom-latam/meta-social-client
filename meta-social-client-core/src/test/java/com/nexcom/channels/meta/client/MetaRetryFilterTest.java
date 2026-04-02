package com.nexcom.channels.meta.client;

import com.nexcom.channels.meta.exception.MetaApiException;
import com.nexcom.channels.meta.exception.MetaRateLimitException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MetaRetryFilterTest {

    @Test
    void rateLimitRetry_retriesOnRateLimitException() {
        Retry retry = MetaRetryFilter.rateLimitRetry(2, Duration.ofMillis(1));
        AtomicInteger attempts = new AtomicInteger();

        StepVerifier.create(
                Mono.defer(() -> {
                    if (attempts.incrementAndGet() < 3) {
                        return Mono.error(new MetaRateLimitException(4, "limit", null, Duration.ofSeconds(1)));
                    }
                    return Mono.just("ok");
                }).retryWhen(retry)
        ).expectNext("ok").verifyComplete();

        assertThat(attempts.get()).isEqualTo(3); // 1 initial + 2 retries
    }

    @Test
    void rateLimitRetry_doesNotRetryOnOtherExceptions() {
        Retry retry = MetaRetryFilter.rateLimitRetry(3, Duration.ofMillis(1));

        StepVerifier.create(
                Mono.<String>error(new MetaApiException(100, "bad request", "GraphMethodException", null))
                        .retryWhen(retry)
        ).expectError(MetaApiException.class).verify();
    }

    @Test
    void rateLimitRetry_exhausted_throwsOriginalException() {
        Retry retry = MetaRetryFilter.rateLimitRetry(1, Duration.ofMillis(1));

        StepVerifier.create(
                Mono.<String>error(new MetaRateLimitException(4, "limit", null, Duration.ofSeconds(1)))
                        .retryWhen(retry)
        ).expectError(MetaRateLimitException.class).verify();
    }

    @Test
    void defaultRetry_creates3AttemptSpec() {
        Retry retry = MetaRetryFilter.defaultRetry();
        // Verify it's usable — no exception on creation
        assertThat(retry).isNotNull();
    }
}
