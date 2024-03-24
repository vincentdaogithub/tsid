package com.vincentdao.tsid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Tsid {

    /**
     * Equivalent to 2^41 timestamps possible, starting from 0.
     */
    static final long MAX_TIMESTAMP = 2199023255551L;

    /**
     * Equivalent to 2^10 nodes possible, starting from 0.
     */
    static final long MAX_NODE = 1023;

    /**
     * Equivalent to 2^12 sequences possible, starting from 0;
     */
    static final long MAX_SEQUENCE = 4095;

    /**
     * Equivalent to 2^12 sequences possible, starting from 0;
     */
    static final int MAX_TSID_STRING_LENGTH = 13;

    /**
     * <a href="https://en.wikipedia.org/wiki/Base32#Crockford's_Base32">Crockford's Base32</a> character mappings. See
     * the <a href="https://www.crockford.com/base32.html">author's page</a> for more details.
     */
    private static final Map<Byte, Character> crockfordEncodeMapping;

    static {
        crockfordEncodeMapping = new HashMap<>();

        crockfordEncodeMapping.put((byte) 0, '0');
        crockfordEncodeMapping.put((byte) 1, '1');
        crockfordEncodeMapping.put((byte) 2, '2');
        crockfordEncodeMapping.put((byte) 3, '3');
        crockfordEncodeMapping.put((byte) 4, '4');
        crockfordEncodeMapping.put((byte) 5, '5');
        crockfordEncodeMapping.put((byte) 6, '6');
        crockfordEncodeMapping.put((byte) 7, '7');
        crockfordEncodeMapping.put((byte) 8, '8');
        crockfordEncodeMapping.put((byte) 9, '9');
        crockfordEncodeMapping.put((byte) 10, 'A');
        crockfordEncodeMapping.put((byte) 11, 'B');
        crockfordEncodeMapping.put((byte) 12, 'C');
        crockfordEncodeMapping.put((byte) 13, 'D');
        crockfordEncodeMapping.put((byte) 14, 'E');
        crockfordEncodeMapping.put((byte) 15, 'F');
        crockfordEncodeMapping.put((byte) 16, 'G');
        crockfordEncodeMapping.put((byte) 17, 'H');
        crockfordEncodeMapping.put((byte) 18, 'J');
        crockfordEncodeMapping.put((byte) 19, 'K');
        crockfordEncodeMapping.put((byte) 20, 'M');
        crockfordEncodeMapping.put((byte) 21, 'N');
        crockfordEncodeMapping.put((byte) 22, 'P');
        crockfordEncodeMapping.put((byte) 23, 'Q');
        crockfordEncodeMapping.put((byte) 24, 'R');
        crockfordEncodeMapping.put((byte) 25, 'S');
        crockfordEncodeMapping.put((byte) 26, 'T');
        crockfordEncodeMapping.put((byte) 27, 'V');
        crockfordEncodeMapping.put((byte) 28, 'W');
        crockfordEncodeMapping.put((byte) 29, 'X');
        crockfordEncodeMapping.put((byte) 30, 'Y');
        crockfordEncodeMapping.put((byte) 31, 'Z');
    }

    /**
     * Character "u" and "U" are excluded to avoid "accidental obscenity" as defined by Crockford.
     */
    private static final Map<Character, Byte> crockfordDecodeMapping;

    static {
        crockfordDecodeMapping = new HashMap<>();

        // Digit
        crockfordDecodeMapping.put('0', (byte) 0);
        crockfordDecodeMapping.put('1', (byte) 1);
        crockfordDecodeMapping.put('2', (byte) 2);
        crockfordDecodeMapping.put('3', (byte) 3);
        crockfordDecodeMapping.put('4', (byte) 4);
        crockfordDecodeMapping.put('5', (byte) 5);
        crockfordDecodeMapping.put('6', (byte) 6);
        crockfordDecodeMapping.put('7', (byte) 7);
        crockfordDecodeMapping.put('8', (byte) 8);
        crockfordDecodeMapping.put('9', (byte) 9);

        // Lowercase (excluding "ilo")
        crockfordDecodeMapping.put('a', (byte) 10);
        crockfordDecodeMapping.put('b', (byte) 11);
        crockfordDecodeMapping.put('c', (byte) 12);
        crockfordDecodeMapping.put('d', (byte) 13);
        crockfordDecodeMapping.put('e', (byte) 14);
        crockfordDecodeMapping.put('f', (byte) 15);
        crockfordDecodeMapping.put('g', (byte) 16);
        crockfordDecodeMapping.put('h', (byte) 17);
        crockfordDecodeMapping.put('j', (byte) 18);
        crockfordDecodeMapping.put('k', (byte) 19);
        crockfordDecodeMapping.put('m', (byte) 20);
        crockfordDecodeMapping.put('n', (byte) 21);
        crockfordDecodeMapping.put('p', (byte) 22);
        crockfordDecodeMapping.put('q', (byte) 23);
        crockfordDecodeMapping.put('r', (byte) 24);
        crockfordDecodeMapping.put('s', (byte) 25);
        crockfordDecodeMapping.put('t', (byte) 26);
        crockfordDecodeMapping.put('v', (byte) 27);
        crockfordDecodeMapping.put('w', (byte) 28);
        crockfordDecodeMapping.put('x', (byte) 29);
        crockfordDecodeMapping.put('y', (byte) 30);
        crockfordDecodeMapping.put('z', (byte) 31);

        // Lowercase "ilo"
        crockfordDecodeMapping.put('i', (byte) 1);
        crockfordDecodeMapping.put('l', (byte) 1);
        crockfordDecodeMapping.put('o', (byte) 0);

        // Uppercase (excluding "ILO")
        crockfordDecodeMapping.put('A', (byte) 10);
        crockfordDecodeMapping.put('B', (byte) 11);
        crockfordDecodeMapping.put('C', (byte) 12);
        crockfordDecodeMapping.put('D', (byte) 13);
        crockfordDecodeMapping.put('E', (byte) 14);
        crockfordDecodeMapping.put('F', (byte) 15);
        crockfordDecodeMapping.put('G', (byte) 16);
        crockfordDecodeMapping.put('H', (byte) 17);
        crockfordDecodeMapping.put('J', (byte) 18);
        crockfordDecodeMapping.put('K', (byte) 19);
        crockfordDecodeMapping.put('M', (byte) 20);
        crockfordDecodeMapping.put('N', (byte) 21);
        crockfordDecodeMapping.put('P', (byte) 22);
        crockfordDecodeMapping.put('Q', (byte) 23);
        crockfordDecodeMapping.put('R', (byte) 24);
        crockfordDecodeMapping.put('S', (byte) 25);
        crockfordDecodeMapping.put('T', (byte) 26);
        crockfordDecodeMapping.put('V', (byte) 27);
        crockfordDecodeMapping.put('W', (byte) 28);
        crockfordDecodeMapping.put('X', (byte) 29);
        crockfordDecodeMapping.put('Y', (byte) 30);
        crockfordDecodeMapping.put('Z', (byte) 31);

        // Uppercase "ILO"
        crockfordDecodeMapping.put('I', (byte) 1);
        crockfordDecodeMapping.put('L', (byte) 1);
        crockfordDecodeMapping.put('O', (byte) 0);
    }

    private final long value;

    Tsid(long timestamp, long node, long sequence) {
        if (timestamp < 0 || MAX_TIMESTAMP < timestamp) {
            throw new IllegalArgumentException(String
                    .format("Timestamp must be between 0 and %d.", MAX_TIMESTAMP));
        }
        if (node < 0 || MAX_NODE < node) {
            throw new IllegalArgumentException(String.format("Node must be between 0 and %d.", MAX_NODE));
        }
        if (sequence < 0 || MAX_SEQUENCE < sequence) {
            throw new IllegalArgumentException(String.format("Sequence must be between 0 and %d.", MAX_SEQUENCE));
        }
        long result = 0;
        result |= (timestamp << 22);
        result |= (node << 12);
        result |= sequence;
        this.value = result;
    }

    public static Tsid fromLong(long tsid) {
        if (tsid < 0) {
            throw new IllegalArgumentException("Tsid value must be positive 64-bit integer.");
        }
        long timestamp = (tsid >>> 22);
        long node = ((tsid >> 12) & 0x3FF);
        long sequence = (tsid & 0xFFF);
        return new Tsid(timestamp, node, sequence);
    }

    public static Tsid fromString(String tsid) {
        if (Objects.isNull(tsid)) {
            throw new NullPointerException("Tsid String is null.");
        }
        String trimmed = tsid.trim();
        if (trimmed.length() != MAX_TSID_STRING_LENGTH) {
            throw new IllegalArgumentException(String.format("Tsid String length must be %d.", MAX_TSID_STRING_LENGTH));
        }
        long tsidAsLong = convertCrockfordStringToLong(trimmed);
        return Tsid.fromLong(tsidAsLong);
    }

    private static long convertCrockfordStringToLong(String crockfordTsid) {
        char[] chars = crockfordTsid.toCharArray();
        long result = 0;
        for (int i = 1; i < MAX_TSID_STRING_LENGTH; i++) {
            Byte charValue = crockfordDecodeMapping.get(chars[i - 1]);
            if (Objects.isNull(charValue)) {
                throw new IllegalArgumentException("Invalid Crockford character.");
            }
            result |= (((long) charValue) << (64 - (5 * i)));
        }
        Byte charValue = crockfordDecodeMapping.get(chars[MAX_TSID_STRING_LENGTH - 1]);
        if (Objects.isNull(charValue)) {
            throw new IllegalArgumentException("Invalid Crockford character.");
        }
        result |= charValue;
        return result;
    }

    public long getTimestamp() {
        return (value >>> 22);
    }

    public long asLong() {
        return value;
    }

    public String asString() {
        long idAsLong = value;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < (MAX_TSID_STRING_LENGTH - 1); i++) {
            byte charValue = (byte) (idAsLong >>> 59);
            idAsLong <<= 5;
            builder.append(getCrockfordChar(charValue));
        }
        // We shift to the right 60 times because there's only 4 bits left.
        byte charValue = (byte) (idAsLong >>> 60);
        Character crockfordChar = getCrockfordChar(charValue);
        if (Objects.isNull(crockfordChar)) {
            throw new IllegalArgumentException("Invalid Crockford value.");
        }
        builder.append(crockfordChar);
        return builder.toString();
    }

    private Character getCrockfordChar(byte value) {
        if (crockfordEncodeMapping.containsKey(value)) {
            return crockfordEncodeMapping.get(value);
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
        return (value == tsid.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
