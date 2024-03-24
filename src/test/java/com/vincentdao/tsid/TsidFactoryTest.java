package com.vincentdao.tsid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class TsidFactoryTest {

    @Test
    void givenTsidFactory_whenGetInstance_thenNotNull() {
        assertThat(TsidFactory.instance())
                .isNotNull();
    }
}
