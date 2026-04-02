package com.nexcom.channels.meta.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaApiVersionTest {

    @Test
    void default_isV25() {
        assertThat(MetaApiVersion.DEFAULT.value()).isEqualTo("v25.0");
    }

    @Test
    void v25_constant_equalsDefault() {
        assertThat(MetaApiVersion.V25_0).isEqualTo(MetaApiVersion.DEFAULT);
    }

    @Test
    void of_withVPrefix_preservesValue() {
        MetaApiVersion v = MetaApiVersion.of("v26.0");
        assertThat(v.value()).isEqualTo("v26.0");
    }

    @Test
    void of_withoutVPrefix_addsPrefix() {
        MetaApiVersion v = MetaApiVersion.of("26.0");
        assertThat(v.value()).isEqualTo("v26.0");
    }

    @Test
    void of_arbitraryVersion_works() {
        MetaApiVersion v = MetaApiVersion.of("v99.1");
        assertThat(v.getValue()).isEqualTo("v99.1");
    }

    @Test
    void equality_sameVersionString_areEqual() {
        assertThat(MetaApiVersion.of("v25.0")).isEqualTo(MetaApiVersion.of("25.0"));
    }

    @Test
    void null_throws() {
        assertThatThrownBy(() -> MetaApiVersion.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blank_throws() {
        assertThatThrownBy(() -> MetaApiVersion.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
