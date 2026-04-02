package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message tags for sending outside the standard messaging window.
 * As of Feb 2026, only HUMAN_AGENT remains non-deprecated.
 */
public enum MessageTag {

    HUMAN_AGENT("HUMAN_AGENT");

    private final String value;

    MessageTag(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
