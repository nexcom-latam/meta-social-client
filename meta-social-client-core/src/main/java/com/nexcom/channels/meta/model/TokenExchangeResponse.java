package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenExchangeResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {}
