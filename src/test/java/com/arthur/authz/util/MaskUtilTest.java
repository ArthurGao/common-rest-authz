package com.arthur.authz.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MaskUtilTest {

    @Test
    void test_whenValidKeyGiven_ExpectValidOutput() {
        String key = UUID.randomUUID().toString();
        assertThat(MaskUtil.mask(key)).isEqualTo(key.substring(0, 4) + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + key.substring(key.length() - 4));
    }

    @Test
    void test_whenEmptyKeyGiven_DoNotThrowException() {
        assertThat(MaskUtil.mask("")).isEmpty();
    }

    @Test
    void test_whenNullKeyGiven_DoNotThrowException() {
        assertThat(MaskUtil.mask(null)).isNull();
    }

    @Test
    void test_whenNonUUIDKeyGivenShorterLengthLessThanOrEqualTo8_DoNotThrowException() {
        assertThat(MaskUtil.mask("anystrin")).isEqualTo("anystrin");
    }

    @Test
    void test_whenNonUUIDKeyGivenLengthGreaterThan8_Then_ExpectMaskedString() {
        assertThat(MaskUtil.mask("anystring")).isEqualTo("anysxxxxxring");
    }

}