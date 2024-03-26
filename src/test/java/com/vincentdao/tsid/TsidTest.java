package com.vincentdao.tsid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class TsidTest {

    @Test
    void givenTsid_whenCreated_thenValid() {
        assertThatCode(TsidTestUtils::createDefault)
                .doesNotThrowAnyException();
        final Tsid id = TsidTestUtils.createDefault();
        assertThat(id)
                .isNotNull();
        assertThat(id.asLong())
                .isEqualTo(TsidTestUtils.TSID_DEFAULT_LONG);
        assertThat(id.asString())
                .isEqualTo(id.toString())
                .isEqualTo(TsidTestUtils.TSID_DEFAULT_STRING);
        assertThat(id.getTimestamp())
                .isEqualTo(TsidTestUtils.TSID_DEFAULT_TIMESTAMP);
    }

    @Test
    void givenLong_whenConvertToTsid_thenValid() {
        assertThatCode(() -> Tsid.fromLong(TsidTestUtils.TSID_DEFAULT_LONG))
                .doesNotThrowAnyException();
        final Tsid id = Tsid.fromLong(TsidTestUtils.TSID_DEFAULT_LONG);
        assertThat(id)
                .isNotNull()
                .isEqualTo(TsidTestUtils.createDefault());
    }

    @Test
    void givenCrockfordString_whenConvertToTsid_thenValid() {
        assertThatCode(() -> Tsid.fromString(TsidTestUtils.TSID_DEFAULT_STRING))
                .doesNotThrowAnyException();
        final Tsid id = Tsid.fromString(TsidTestUtils.TSID_DEFAULT_STRING);
        assertThat(id)
                .isNotNull()
                .isEqualTo(TsidTestUtils.createDefault());
    }
}
