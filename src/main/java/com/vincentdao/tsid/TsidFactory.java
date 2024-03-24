package com.vincentdao.tsid;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

public final class TsidFactory {

    private static final SecureRandom RANDOMIZER = new SecureRandom();

    private static TsidFactory instance;

    private final long node;
    private final long epoch;

    private long sequence;
    private long prevTimestamp;

    public TsidFactory(Builder builder) {
        if (Objects.isNull(builder)) {
            throw new NullPointerException("Builder is null.");
        }
        long builderEpoch = builder.epoch;
        if (builderEpoch < 0 || Tsid.MAX_TIMESTAMP < builderEpoch) {
            throw new IllegalArgumentException(String
                    .format("Epoch must be between 0 and %d.", Tsid.MAX_TIMESTAMP));
        }
        this.epoch = builderEpoch;
        long builderNode = builder.node;
        if (builderNode < 0 || Tsid.MAX_NODE < builderNode) {
            throw new IllegalArgumentException(String.format("Node must be between 0 and %d.", Tsid.MAX_NODE));
        }
        this.node = builderNode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TsidFactory instance() {
        if (Objects.isNull(instance)) {
            generateDefaultFactory();
        }
        return instance;
    }

    private static synchronized void generateDefaultFactory() {
        if (Objects.isNull(instance)) {
            instance = TsidFactory.builder()
                    .withEpoch()
                    .asDefault()
                    .withEpoch()
                    .asDefault()
                    .build();
        }
    }

    /**
     * Get quick Tsid with random sequence part. May introduce collision. (Use {@link TsidFactory} instead.)
     *
     * @return New instance of Tsid with randomized sequence part.
     */
    public Tsid quickGenerate() {
        long randSequence = RANDOMIZER.nextInt((int) (Tsid.MAX_SEQUENCE + 1));
        return new Tsid(getUtcTimestamp(), node, randSequence);
    }

    public Tsid generate() {
        long currentTimestamp = getUtcTimestamp();
        if (currentTimestamp == prevTimestamp) {
            long nextSequence = modifyAndGetNextSequence(false);
            return new Tsid(currentTimestamp, node, nextSequence);
        } else {
            this.prevTimestamp = currentTimestamp;
            long nextSequence = modifyAndGetNextSequence(true);
            return new Tsid(currentTimestamp, node, nextSequence);
        }
    }

    private long getUtcTimestamp() {
        return (Instant.now().toEpochMilli() - epoch);
    }

    private synchronized long modifyAndGetNextSequence(boolean reset) {
        if (reset) {
            this.sequence = 0;
        }
        if (sequence > Tsid.MAX_SEQUENCE) {
            throw new IllegalStateException("Sequence reached maximum allowable value (above 12 bit integer).");
        }
        return sequence++;
    }

    public static class Builder {

        private static final String NODE_PROP_NAME = "tsid.node";
        private static final String NODE_ENV_NAME = "TSID_NODE";
        private static final String EPOCH_PROP_NAME = "tsid.epoch";
        private static final String EPOCH_ENV_NAME = "TSID_EPOCH";

        long epoch;
        long node;

        Builder() {
        }

        public static final class NodeBuilder {

            private final Builder builder;

            private NodeBuilder(Builder builder) {
                this.builder = builder;
            }

            public Builder comesFromSystemOrDefault() {
                // Get the node value from the system properties
                Long nodeAsProperty = Builder.extractFromSystemAsLong(NODE_PROP_NAME);
                if (Objects.nonNull(nodeAsProperty)) {
                    builder.node = nodeAsProperty;
                    return builder;
                }
                // Get the node value from the system environment
                Long nodeAsEnv = Builder.extractFromSystemAsLong(NODE_ENV_NAME);
                if (Objects.nonNull(nodeAsEnv)) {
                    builder.node = nodeAsEnv;
                    return builder;
                }
                // Else, the current thread's id will be the node value
                builder.node = Thread.currentThread().getId();
                return builder;
            }

            public Builder asDefault() {
                builder.node = Thread.currentThread().getId();
                return builder;
            }

            public Builder customizedAs(long customNode) {
                builder.node = customNode;
                return builder;
            }
        }

        public NodeBuilder withNode() {
            return new NodeBuilder(this);
        }

        public static final class EpochBuilder {

            private final Builder builder;

            private EpochBuilder(Builder builder) {
                this.builder = builder;
            }

            public Builder comesFromSystemOrDefault() {
                // Get the epoch value from the system properties
                Long epochAsProperty = Builder.extractFromSystemAsLong(EPOCH_PROP_NAME);
                if (Objects.nonNull(epochAsProperty)) {
                    builder.epoch = epochAsProperty;
                    return builder;
                }
                // Get the epoch value from the system environment
                Long epochAsEnv = Builder.extractFromSystemAsLong(EPOCH_ENV_NAME);
                if (Objects.nonNull(epochAsEnv)) {
                    builder.epoch = epochAsEnv;
                    return builder;
                }
                // Else, the epoch will be Unix epoch
                builder.epoch = Instant.EPOCH.toEpochMilli();
                return builder;
            }

            public Builder asDefault() {
                builder.epoch = Instant.EPOCH.toEpochMilli();
                return builder;
            }

            public Builder customizedAs(long customEpoch) {
                builder.epoch = customEpoch;
                return builder;
            }
        }

        public EpochBuilder withEpoch() {
            return new EpochBuilder(this);
        }

        private static Long extractFromSystemAsLong(String varName) {
            String value = System.getProperty(varName);
            if (Objects.nonNull(value)) {
                String trimmed = value.trim();
                return Long.parseLong(trimmed);
            }
            return null;
        }

        public TsidFactory build() {
            if (Objects.nonNull(instance)) {
                throw new IllegalStateException(
                        "Instance of factory has already been generated. Consider using 'reset()' method first.");
            }
            return assignToInstanceAndReturn(this);
        }

        private static synchronized TsidFactory assignToInstanceAndReturn(Builder builder) {
            if (Objects.isNull(instance)) {
                instance = new TsidFactory(builder);
                return instance;
            }
            throw new IllegalStateException(
                    "Race condition encountered. Consider generate the factory safely first.");
        }
    }
}
