package com.vincentdao.tsid;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class TsidFactoryTest {

    @Test
    void givenTsidFactory_whenGetInstance_thenNotNull() {
        assertThat(TsidFactory.instance())
                .isNotNull();
    }

    @Test
    void givenTsidFactory_whenSimpleGenerate_thenNoCollision() {
        final int expectedSize = 1000;
        final Set<Tsid> tsidSet = new HashSet<>();
        for (int i = 0; i < expectedSize; i++) {
            assertThat(tsidSet.add(TsidFactory.instance().generate()))
                    .isTrue();
        }
    }

    private static final class SimpleThread extends Thread {

        private final Set<Tsid> tsidSet;
        private final int maxTsidPerThread;
        private final CountDownLatch countDownLatch;

        private SimpleThread(Set<Tsid> tsidSet, int maxTsidPerThread, CountDownLatch countDownLatch) {
            this.tsidSet = tsidSet;
            this.maxTsidPerThread = maxTsidPerThread;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                countDownLatch.await();
                for (int i = 0; i < maxTsidPerThread; i++) {
                    tsidSet.add(TsidFactory.instance().generate());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    @SuppressWarnings("java:S2925")
    void givenTsidFactory_whenMultipleThreadsGenerate_thenNoCollision() throws InterruptedException {
        for (int testCount = 0; testCount < 5; testCount++) {
            final int maxTsidPerThread = 10000;
            final int maxThreadCount = 100;
            final Set<Tsid> tsidSet = ConcurrentHashMap.newKeySet();
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            // Generate instance for this thread.
            TsidFactory.instance();
            for (int i = 0; i < maxThreadCount; i++) {
                Thread thread = new SimpleThread(tsidSet, maxTsidPerThread, countDownLatch);
                thread.start();
            }
            countDownLatch.countDown();
            Thread.sleep(2000);
            assertThat(tsidSet)
                    .hasSize(maxThreadCount * maxTsidPerThread);
        }
    }

    @Test
    void givenTsidFactory_whenQuickGenerate_thenValid() {
        assertThatCode(() -> TsidFactory.instance().quickGenerate())
                .doesNotThrowAnyException();
        assertThat(TsidFactory.instance().quickGenerate())
                .isNotNull();
    }

    @Test
    @SuppressWarnings("java:S2925")
    void givenTsidFactory_whenConfigureWithBuilder_thenSuccessful() throws InterruptedException {
        final long node = 100;
        final long epoch = 1000000L;
        TsidFactory.reset();
        assertThatCode(() -> TsidFactory.builder()
                .withNode()
                .customizedAs(node)
                .withEpoch()
                .customizedAs(epoch)
                .build())
                .doesNotThrowAnyException();
        assertThat(TsidFactory.instance().getNode())
                .isEqualTo(node);
        assertThat(TsidFactory.instance().getEpoch())
                .isEqualTo(epoch);
        final Tsid id = TsidFactory.instance().generate();
        Thread.sleep(1L);
        final long currentTimestamp = Instant.now().toEpochMilli() - epoch;
        assertThat(id)
                .isNotNull();
        assertThat(id.getTimestamp())
                .isLessThanOrEqualTo(currentTimestamp);
        final long tsidNode = ((id.asLong() >> 12) & 0x3FF);
        assertThat(tsidNode)
                .isEqualTo(node);
    }

    @Test
    @SuppressWarnings("java:S2925")
    void givenTsidFactory_whenReset_thenSuccessful() throws InterruptedException {
        final long node = Thread.currentThread().getId();
        assertThatCode(TsidFactory::reset)
                .doesNotThrowAnyException();
        TsidFactory.builder()
                .withNode()
                .customizedAs(((node + 1) % Tsid.MAX_NODE))
                .withEpoch()
                .customizedAs(1000000L)
                .build();
        assertThatCode(TsidFactory::reset)
                .doesNotThrowAnyException();
        final Tsid id = TsidFactory.instance().generate();
        Thread.sleep(1L);
        final long currentTimestamp = Instant.now().toEpochMilli();
        assertThat(id)
                .isNotNull();
        assertThat(id.getTimestamp())
                .isLessThanOrEqualTo(currentTimestamp);
        final long tsidNode = ((id.asLong() >> 12) & 0x3FF);
        assertThat(tsidNode)
                .isNotEqualTo(node);
    }

    @Test
    void givenTsidFactory_whenAsDefault_thenSuccessful() {
        final long node = Thread.currentThread().getId();
        TsidFactory.reset();
        TsidFactory.builder()
                .withNode()
                .asDefault()
                .withEpoch()
                .asDefault()
                .build();
        assertThat(TsidFactory.instance().getNode())
                .isEqualTo(node);
        assertThat(TsidFactory.instance().getEpoch())
                .isEqualTo(Instant.EPOCH.toEpochMilli());
    }

    @Test
    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = TsidFactory.Builder.NODE_ENV_NAME, value = "69"),
            @SetEnvironmentVariable(key = TsidFactory.Builder.EPOCH_ENV_NAME, value = "1000000")
    })
    void givenTsidFactory_whenComesFromSystemEnvironment_thenSuccessful() {
        TsidFactory.reset();
        TsidFactory.builder()
                .withNode()
                .comesFromSystemOrDefault()
                .withEpoch()
                .comesFromSystemOrDefault()
                .build();
        assertThat(TsidFactory.instance().getNode())
                .isEqualTo(69);
        assertThat(TsidFactory.instance().getEpoch())
                .isEqualTo(1000000);
    }

    @Test
    @SetSystemProperty.SetSystemProperties({
            @SetSystemProperty(key = TsidFactory.Builder.NODE_PROP_NAME, value = "96"),
            @SetSystemProperty(key = TsidFactory.Builder.EPOCH_PROP_NAME, value = "10000000")
    })
    void givenTsidFactory_whenComesFromSystemProperty_thenSuccessful() {
        TsidFactory.reset();
        TsidFactory.builder()
                .withNode()
                .comesFromSystemOrDefault()
                .withEpoch()
                .comesFromSystemOrDefault()
                .build();
        assertThat(TsidFactory.instance().getNode())
                .isEqualTo(96);
        assertThat(TsidFactory.instance().getEpoch())
                .isEqualTo(10000000);
    }

    @Test
    @ClearEnvironmentVariable.ClearEnvironmentVariables({
            @ClearEnvironmentVariable(key = TsidFactory.Builder.NODE_ENV_NAME),
            @ClearEnvironmentVariable(key = TsidFactory.Builder.EPOCH_ENV_NAME)
    })
    @ClearSystemProperty.ClearSystemProperties({
            @ClearSystemProperty(key = TsidFactory.Builder.NODE_PROP_NAME),
            @ClearSystemProperty(key = TsidFactory.Builder.EPOCH_PROP_NAME)
    })
    void givenTsidFactory_whenComesFromSystemButNotDefined_thenUseDefault() {
        TsidFactory.reset();
        TsidFactory.builder()
                .withNode()
                .comesFromSystemOrDefault()
                .withEpoch()
                .comesFromSystemOrDefault()
                .build();
        assertThat(TsidFactory.instance().getNode())
                .isEqualTo(Thread.currentThread().getId());
        assertThat(TsidFactory.instance().getEpoch())
                .isEqualTo(Instant.EPOCH.toEpochMilli());
    }
}
