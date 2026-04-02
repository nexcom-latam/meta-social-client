package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SenderAction {

    TYPING_ON("typing_on"),
    TYPING_OFF("typing_off"),
    MARK_SEEN("mark_seen");

    private final String value;

    SenderAction(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
