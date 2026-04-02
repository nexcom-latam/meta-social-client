package com.nexcom.channels.meta.model;

/**
 * Graph API version identifier. Supports any version string (e.g. "v25.0", "v26.0").
 * Use {@link #DEFAULT} for the library's default version, or construct with
 * {@link #of(String)} for runtime-configured versions.
 *
 * <p>This replaced the original closed enum so that consumers can target
 * newer API versions without waiting for a library release.</p>
 */
public record MetaApiVersion(String value) {

    /** The library's default Graph API version. */
    public static final MetaApiVersion DEFAULT = new MetaApiVersion("v25.0");

    /** Well-known constant for v25.0. */
    public static final MetaApiVersion V25_0 = DEFAULT;

    public MetaApiVersion {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("API version must not be blank");
        }
        if (!value.startsWith("v")) {
            value = "v" + value;
        }
    }

    /**
     * Creates a version from a string like "v25.0" or "25.0".
     * The "v" prefix is added automatically if missing.
     */
    public static MetaApiVersion of(String version) {
        return new MetaApiVersion(version);
    }

    /** Returns the version string including the "v" prefix, e.g. "v25.0". */
    public String getValue() {
        return value;
    }
}
