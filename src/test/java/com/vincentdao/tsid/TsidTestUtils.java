package com.vincentdao.tsid;

final class TsidTestUtils {

    private TsidTestUtils() {
    }

    /**
     * {@code long} value for default Tsid below.
     */
    static final long TSID_DEFAULT_LONG = 1541815603606036480L;

    /**
     * {@code String} value for default Tsid below.
     */
    static final String TSID_DEFAULT_STRING = "2NJT27V22YG00";

    /**
     * {@code String} value for default Tsid below.
     */
    static final long TSID_DEFAULT_TIMESTAMP = 367597485448L;

    /**
     * The Tsid will have value of {@code 1541815603606036480} as {@code long}, and {@code 2NJT27V22YG00} as
     * {@code String}.
     *
     * @return      The default Tsid for testing. The sample is from the wiki page of
     *              <a href="https://en.wikipedia.org/wiki/Snowflake_ID">Snowflake ID</a>.
     */
    static Tsid createDefault() {
        long timestamp = 367597485448L;
        long node = 378;
        long sequence = 0;
        return new Tsid(timestamp, node, sequence);
    }
}
