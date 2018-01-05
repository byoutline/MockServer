package com.byoutline.mockserver.internal;

import java.util.List;
import java.util.Map;

public final class ParsedConfig {

    public final int port;
    public final int maxPort;
    public final List<Map.Entry<RequestParams, ResponseParams>> responses;

    public ParsedConfig(int port, int maxPort, List<Map.Entry<RequestParams, ResponseParams>> responses) {
        this.port = port;
        this.maxPort = maxPort;
        this.responses = responses;
    }
}
