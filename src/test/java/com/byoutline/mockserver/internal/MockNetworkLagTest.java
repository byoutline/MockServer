package com.byoutline.mockserver.internal;


import com.byoutline.mockserver.NetworkType;
import org.junit.Test;

public class MockNetworkLagTest {

    @Test
    public void shouldNotDivideByZero() throws Exception {
        //given
        MockNetworkLag instance = new MockNetworkLag(NetworkType.NONE);
        //when
        instance.simulateNetworkLag();
        //then
        // assume no crash
    }
}