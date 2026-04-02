package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MediaUploadResponse(
        @JsonProperty("attachment_id") String attachmentId
) {}
