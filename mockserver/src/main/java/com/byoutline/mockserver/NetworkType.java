package com.byoutline.mockserver;

/**
 * Contains expected delays for common network types.
 *
 * @author Sebastian Kacprzak |sebastian.kacprzak at byoutline.com| on 14.04.14.
 */
public enum NetworkType {
    /**
     * Delay 500-200ms
     */
    VPN(500, 2000),
    /**
     * Delay 150-550ms
     */
    GPRS(150, 550),
    /**
     * Delay 80-400ms
     */
    EDGE(80, 400),
    /**
     * Delay 35-200ms
     */
    UMTS(35, 200),
    /**
     * Delay 0ms
     */
    NO_DELAY(0, 0);

    public final long minDelay;
    public final long maxDelay;

    NetworkType(long min, long max) {
        if (HttpMockServer.DEBUG && min > max) {
            throw new AssertionError("Delay values are incorrect: " + min + " " + max);
        }
        this.minDelay = min;
        this.maxDelay = max;
    }
}
