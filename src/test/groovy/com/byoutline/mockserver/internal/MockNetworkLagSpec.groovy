package com.byoutline.mockserver.internal

import com.byoutline.mockserver.NetworkType
import spock.lang.Specification

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class MockNetworkLagSpec extends Specification {
    def "should not divide by zero"() {
        given:
        MockNetworkLag instance = new MockNetworkLag(NetworkType.NONE);
        when:
        instance.simulateNetworkLag();
        then:
        notThrown Throwable
    }
}
