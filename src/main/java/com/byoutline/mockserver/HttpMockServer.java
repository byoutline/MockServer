package com.byoutline.mockserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Local mock HTTP server. Configured with R.raw.config file. Based on
 * http://www.simpleframework.org/doc/tutorial/tutorial.php
 */
public class HttpMockServer implements Container {

    public static final int MOCK_SERVER_PORT = 8099;
    public static final String SEPARATOR = "";
    public static boolean DEBUG = true;

    private final Connection conn;
    private final ResponseHandler responseHandler;
    private final static Logger LOGGER = Logger.getLogger(HttpMockServer.class.getName());

    private static HttpMockServer sMockServer;

    public HttpMockServer(@Nonnull JSONObject jsonObject, @Nonnull ConfigReader configReader, @Nonnull NetworkType simulatedNetworkType)
            throws IOException, JSONException {
        ConfigResult config = new ConfigParser(configReader).parseConfig(jsonObject);
        this.responseHandler = new ResponseHandler(config.responses, simulatedNetworkType, configReader);
        Server server = new ContainerServer(this);
        conn = new SocketConnection(server);
        final SocketAddress sa = new InetSocketAddress(config.port);
        conn.connect(sa);
    }

    /**
     *
     * @param configInputStream stream of config file. <br />
     * For android <code
     * class="java">getResources().openRawResource(R.raw.config);</code>
     * @param configReader wrapper for platform specific bits
     * @param simulatedNetworkType delay time before response is sent.
     */
    public static void startMockApiServer(@Nonnull InputStream configInputStream,
            @Nonnull ConfigReader configReader, @Nonnull NetworkType simulatedNetworkType) {
        try {
            String configJson = new String(readInitialData(configInputStream));
            sMockServer = new HttpMockServer(new JSONObject(configJson), configReader, simulatedNetworkType);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "MockServer error:", e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "MockServer error:", e);
        }
    }

    public void reset() {
        this.responseHandler.stopResponding();
    }

    public void shutdown() throws Exception {
        conn.close();
        this.reset();
    }

    @Override
    public void handle(Request req, Response resp) {
        responseHandler.handle(req, resp);
    }

    static byte[] readInitialData(@Nonnull InputStream inputStream)
            throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i = inputStream.read();
        while (i != -1) {
            byteArrayOutputStream.write(i);
            i = inputStream.read();
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    static final class ConfigResult {

        public final int port;
        public final List<Map.Entry<ResponsePath, ResponseParams>> responses;

        ConfigResult(int port, List<Map.Entry<ResponsePath, ResponseParams>> responses) {
            this.port = port;
            this.responses = responses;
        }
    }
}
