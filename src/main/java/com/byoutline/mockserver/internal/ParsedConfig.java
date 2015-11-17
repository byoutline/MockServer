package com.byoutline.mockserver.internal;

import java.util.List;
import java.util.Map;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public final class ParsedConfig {

    public final int port;
    public final List<Map.Entry<RequestParams, ResponseParams>> responses;

    public ParsedConfig(int port, List<Map.Entry<RequestParams, ResponseParams>> responses) {
        this.port = port;
        this.responses = responses;
    }
}
