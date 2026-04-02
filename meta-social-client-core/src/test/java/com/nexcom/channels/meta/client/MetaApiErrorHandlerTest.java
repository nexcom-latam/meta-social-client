package com.nexcom.channels.meta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexcom.channels.meta.exception.MetaApiException;
import com.nexcom.channels.meta.exception.MetaAuthException;
import com.nexcom.channels.meta.exception.MetaRateLimitException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaApiErrorHandlerTest {

    private final MetaApiErrorHandler handler = new MetaApiErrorHandler(new ObjectMapper());

    @Test
    void code190_returnsMetaAuthException() {
        String body = "{\"error\":{\"message\":\"Invalid token\",\"type\":\"OAuthException\",\"code\":190,\"fbtrace_id\":\"trace1\"}}";
        MetaApiException ex = handler.handleErrorResponse(body, 401);

        assertThat(ex).isInstanceOf(MetaAuthException.class);
        assertThat(ex.getCode()).isEqualTo(190);
        assertThat(ex.getFbtraceId()).isEqualTo("trace1");
    }

    @Test
    void code4_returnsMetaRateLimitException() {
        String body = "{\"error\":{\"message\":\"Rate limit\",\"type\":\"OAuthException\",\"code\":4}}";
        MetaApiException ex = handler.handleErrorResponse(body, 429);

        assertThat(ex).isInstanceOf(MetaRateLimitException.class);
        assertThat(ex.getCode()).isEqualTo(4);
    }

    @Test
    void code32_returnsMetaRateLimitException() {
        String body = "{\"error\":{\"message\":\"Rate limit\",\"type\":\"OAuthException\",\"code\":32}}";
        assertThat(handler.handleErrorResponse(body, 429)).isInstanceOf(MetaRateLimitException.class);
    }

    @Test
    void code613_returnsMetaRateLimitException() {
        String body = "{\"error\":{\"message\":\"Rate limit\",\"type\":\"OAuthException\",\"code\":613}}";
        assertThat(handler.handleErrorResponse(body, 429)).isInstanceOf(MetaRateLimitException.class);
    }

    @Test
    void unknownCode_returnsGenericMetaApiException() {
        String body = "{\"error\":{\"message\":\"Bad request\",\"type\":\"GraphMethodException\",\"code\":100,\"fbtrace_id\":\"t2\"}}";
        MetaApiException ex = handler.handleErrorResponse(body, 400);

        assertThat(ex).isExactlyInstanceOf(MetaApiException.class);
        assertThat(ex.getCode()).isEqualTo(100);
        assertThat(ex.getType()).isEqualTo("GraphMethodException");
    }

    @Test
    void malformedJson_returnsGenericException() {
        MetaApiException ex = handler.handleErrorResponse("not json", 500);
        assertThat(ex.getCode()).isEqualTo(500);
    }

    @Test
    void noErrorField_returnsGenericException() {
        MetaApiException ex = handler.handleErrorResponse("{\"something\":\"else\"}", 400);
        assertThat(ex.getCode()).isEqualTo(400);
    }
}
