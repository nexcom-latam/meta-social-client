package com.nexcom.channels.meta.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AttachmentType {

    TEXT("text"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    FILE("file"),
    IG_POST("ig_post"),
    STORY_MENTION("story_mention"),
    REEL("reel"),
    /** Legacy — parser maps this to IG_POST and logs a deprecation warning. */
    SHARE("share");

    private final String value;

    AttachmentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AttachmentType fromValue(String value) {
        for (AttachmentType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return TEXT;
    }
}
