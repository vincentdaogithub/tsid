package com.vincentdao.tsid;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Tsid {

    /**
     * Equivalent to 2^10 nodes possible, starting from 0.
     */
    private static final long MAX_NODE = 1023;

    /**
     * Equivalent to 2^12 sequences possible, starting from 0;
     */
    private static final long MAX_SEQUENCE = 4095;

    private static final Map<Byte, Character> crockfordMapping;
    private static final Random RANDOMIZER = new Random();

    static {
        crockfordMapping = new HashMap<>();
        crockfordMapping.put((byte) 0, '0');
        crockfordMapping.put((byte) 1, '1');
        crockfordMapping.put((byte) 2, '2');
        crockfordMapping.put((byte) 3, '3');
        crockfordMapping.put((byte) 4, '4');
        crockfordMapping.put((byte) 5, '5');
        crockfordMapping.put((byte) 6, '6');
        crockfordMapping.put((byte) 7, '7');
        crockfordMapping.put((byte) 8, '8');
        crockfordMapping.put((byte) 9, '9');
        crockfordMapping.put((byte) 10, 'A');
        crockfordMapping.put((byte) 11, 'B');
        crockfordMapping.put((byte) 12, 'C');
        crockfordMapping.put((byte) 13, 'D');
        crockfordMapping.put((byte) 14, 'E');
        crockfordMapping.put((byte) 15, 'F');
        crockfordMapping.put((byte) 16, 'G');
        crockfordMapping.put((byte) 17, 'H');
        crockfordMapping.put((byte) 18, 'J');
        crockfordMapping.put((byte) 19, 'K');
        crockfordMapping.put((byte) 20, 'M');
        crockfordMapping.put((byte) 21, 'N');
        crockfordMapping.put((byte) 22, 'P');
        crockfordMapping.put((byte) 23, 'Q');
        crockfordMapping.put((byte) 24, 'R');
        crockfordMapping.put((byte) 25, 'S');
        crockfordMapping.put((byte) 26, 'T');
        crockfordMapping.put((byte) 27, 'V');
        crockfordMapping.put((byte) 28, 'W');
        crockfordMapping.put((byte) 29, 'X');
        crockfordMapping.put((byte) 30, 'Y');
        crockfordMapping.put((byte) 31, 'Z');
    }

    public static final class Factory {

        private static final ThreadLocal<Factory> instance = new ThreadLocal<>();

        private final long node;
        private long sequence;
        private long prevTimestamp;

        private Factory(long node) {
            if (node < 0 || MAX_NODE < node) {
                throw new IllegalArgumentException("Node must be 10-bit integer.");
            }
            this.node = node;
        }

        public static Factory withNode(long node) {
            generateInstance(node);
            return instance.get();
        }

        private static synchronized void generateInstance(long node) {
            instance.set(new Factory(node));
        }

        public static Factory getInstance() {
            Factory factory = instance.get();
            if (Objects.isNull(factory)) {
                generateQuickInstance();
            }
            return instance.get();
        }

        private static synchronized void generateQuickInstance() {
            instance.set(new Factory(Thread.currentThread().getId()));
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

        private synchronized long modifyAndGetNextSequence(boolean reset) {
            if (reset) {
                this.sequence = 0;
            }
            if (sequence > MAX_SEQUENCE) {
                throw new IllegalStateException("Sequence reached maximum allowable value (above 12 bit integer).");
            }
            return sequence++;
        }

        public void resetFactory() {
            instance.remove();
        }
    }

    private static long getUtcTimestamp() {
        return Instant.now(Clock.systemUTC()).toEpochMilli();
    }

    private final long timestamp;
    private final long node;
    private final long sequence;

    private Tsid(long timestamp, long node, long sequence) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("Timestamp must be non-negative.");
        }
        if (node < 0 || MAX_NODE < node) {
            throw new IllegalArgumentException("Node must be 10-bit integer.");
        }
        if (sequence < 0 || MAX_SEQUENCE < sequence) {
            throw new IllegalArgumentException("Sequence must be 12-bit integer.");
        }
        this.timestamp = timestamp;
        this.node = node;
        this.sequence = sequence;
    }

    /**
     * Get quick Tsid with random sequence part. May introduce collision (use {@code Tsid.Factory.generate()} instead).
     *
     * @return      New instance of Tsid with randomized sequence part.
     */
    public final Tsid quickGenerate() {
        long randSequence = RANDOMIZER.nextInt((int) (MAX_SEQUENCE + 1));
        return new Tsid(getUtcTimestamp(), Thread.currentThread().getId(), randSequence);
    }

    public final long asLong() {
        long result = 0;
        result |= (timestamp << 22);
        result |= (node << 12);
        result |= sequence;
        return result;
    }

    public final String asString() {
        long idAsLong = asLong();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < (64 / 5); i++) {
            byte value = (byte) (idAsLong >>> 59);
            idAsLong <<= 5;
            builder.append(getCrockfordChar(value));
        }
        // Handle the last 4 bits to have the rightmost bit randomized between 1 and 0, so that the last character of
        // the String can introduce "even" character (aka even value character).
        byte value = (byte) (idAsLong >>> 59);
        value += (byte) (RANDOMIZER.nextBoolean() ? 1 : 0);
        builder.append(getCrockfordChar(value));
        return builder.toString();
    }

    private char getCrockfordChar(byte value) {
        if (crockfordMapping.containsKey(value)) {
            return crockfordMapping.get(value);
        } else {
            throw new IllegalArgumentException("The value is not 32-bit integer.");
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (getClass() != o.getClass())) {
            return false;
        }
        Tsid tsid = (Tsid) o;
        return (timestamp == tsid.timestamp)
                && (node == tsid.node)
                && (sequence == tsid.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, node, sequence);
    }
}
