package com.byoutline.mockserver;


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