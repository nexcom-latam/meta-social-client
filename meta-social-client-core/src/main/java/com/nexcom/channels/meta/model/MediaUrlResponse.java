package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MediaUrlResponse(
        String url,
        @JsonProperty("mime_type") String mimeType,
        Long size
) {}
