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

    def "should sleep more than minDelay"() {
        given:
        MockNetworkLag instance = new MockNetworkLag(NetworkType.UMTS);
        def before = Calendar.instance.timeInMillis
        when:
        instance.simulateNetworkLag()
        then:
        def after = Calendar.instance.timeInMillis
        after - before > NetworkType.UMTS.minDelay
    }

    def "should sleep less than maxDelay"() {
        given:
        MockNetworkLag instance = new MockNetworkLag(NetworkType.UMTS);
        def before = Calendar.instance.timeInMillis
        when:
        instance.simulateNetworkLag()
        then:
        def after = Calendar.instance.timeInMillis
        def methodExecutionMargin = 30
        after - before < NetworkType.UMTS.maxDelay + methodExecutionMargin
    }
}
