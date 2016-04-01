package com.byoutline.mockserver;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 14.04.14.
 */
public enum NetworkType {
    VPN(500, 2000),
    GPRS(150, 550),
    EDGE(80, 400),
    UMTS(35, 200),
    NONE(0, 0);

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
