package com.vincentdao.tsid;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

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
}
