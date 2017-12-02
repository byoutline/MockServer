package com.byoutline.mockserver.internal;


import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AutoPortConnect {
    private final Connector connector;

    public AutoPortConnect() {
        this(new HttpServerConnector());
    }

    public AutoPortConnect(Connector connector) {
        this.connector = connector;
    }

    /**
     * Attempts to connect to all ports from given range. If all attempts fails exception will be thrown.
     */
    public Connection connectToPortFromRange(Server server, int minPort, int maxPort) throws IOException {
        if (minPort > maxPort || minPort < 0 || maxPort > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port range - min: %d, max: %d", minPort, maxPort));
        }

        for (int port = minPort; port<=maxPort; port++) {
            try {
                return connector.connectToPort(port, server);
            } catch (IOException e) {
                if (port >= maxPort) {
                    throw e;
                }
            }
        }
        throw new AssertionError(String.format("Failed to connect to any port from range %d %d", minPort, maxPort));
    }
}

interface Connector {
    Connection connectToPort(int port, Server server) throws IOException;
}

class HttpServerConnector implements Connector {

    @Override
    public Connection connectToPort(int port, Server server) throws IOException {
        SocketConnection conn = new SocketConnection(server);
        SocketAddress sa = new InetSocketAddress(port);
        conn.connect(sa);
        return conn;
    }
}