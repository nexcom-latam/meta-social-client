package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessagingType {

    RESPONSE("RESPONSE"),
    UPDATE("UPDATE"),
    MESSAGE_TAG("MESSAGE_TAG");

    private final String value;

    MessagingType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
