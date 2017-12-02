package com.byoutline.mockserver;


import com.byoutline.mockserver.internal.MatchingMethod;

public final class DefaultValues {
    public static final int MOCK_SERVER_PORT = 8099;
    public static final int RESPONSE_CODE = 200;
    public static final String RESPONSE = "OK";
    public static final MatchingMethod QUERY_MATCHING_METHOD = MatchingMethod.CONTAINS;

    private DefaultValues() {
    }
}
