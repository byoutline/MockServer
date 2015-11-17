package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.NetworkType;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public final class MockNetworkLag {
    private final static Logger LOGGER = Logger.getLogger(MockNetworkLag.class.getName());
    private final Random random = new Random();
    private final NetworkType networkType;

    public MockNetworkLag(NetworkType networkType) {
        this.networkType = networkType;
    }


    void simulateNetworkLag() {
        try {
            long avg = (networkType.minDelay + networkType.maxDelay) / 2;
            if (avg == 0) {
                return;
            }
            long spread = avg - networkType.minDelay;
            Thread.sleep(avg + random.nextLong() % spread);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "simulating network lag failed", e);
        }
    }
}
