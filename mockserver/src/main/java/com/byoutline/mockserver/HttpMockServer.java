package com.byoutline.mockserver;

import com.byoutline.mockserver.internal.AutoPortConnect;
import com.byoutline.mockserver.internal.ConfigParser;
import com.byoutline.mockserver.internal.MockNetworkLag;
import com.byoutline.mockserver.internal.ParsedConfig;
import com.byoutline.mockserver.internal.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Local mock HTTP server.
 */
public class HttpMockServer implements Container {

    public static boolean DEBUG = true;
    private final Connection conn;
    private final ResponseHandler responseHandler;

    // Reference is kept only to prevent server from being garbage collected
    private static HttpMockServer sMockServer;

    public HttpMockServer(
            @Nonnull JSONObject jsonObject,
            @Nonnull ConfigReader configReader,
            @Nonnull NetworkType simulatedNetworkType
    ) throws IOException, JSONException {
        ParsedConfig config = new ConfigParser(configReader).parseConfig(jsonObject);
        MockNetworkLag networkLag = new MockNetworkLag(simulatedNetworkType);
        this.responseHandler = new ResponseHandler(config.responses, networkLag, configReader);
        Server server = new ContainerServer(this);

        conn = new AutoPortConnect().connectToPortFromRange(server, config.port, config.maxPort);
    }

    /**
     * Starts mock server and keeps reference to it.
     *
     * @param configReader         wrapper for platform specific bits
     * @param simulatedNetworkType delay time before response is sent.
     */
    public static HttpMockServer startMockApiServer(
            @Nonnull ConfigReader configReader,
            @Nonnull NetworkType simulatedNetworkType
    ) throws IOException {
        JSONObject jsonObj = ConfigParser.getMainConfigJson(configReader);
        HttpMockServer mockServer;
        mockServer = new HttpMockServer(jsonObj, configReader, simulatedNetworkType);

        sMockServer = mockServer;
        return mockServer;
    }

    public void reset() {
        this.responseHandler.stopResponding();
    }

    public void shutdown() throws Exception {
        conn.close();
        sMockServer = null;
        this.reset();
    }

    @Override
    public void handle(Request req, Response resp) {
        responseHandler.handle(req, resp);
    }

}
