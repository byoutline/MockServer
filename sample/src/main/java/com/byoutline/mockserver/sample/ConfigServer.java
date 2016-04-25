package com.byoutline.mockserver.sample;


import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

/**
 * Created by michalp on 25.04.16.
 */
public class ConfigServer {

    public static void main(String... args) {
        // TODO: search for lib for handling arguments
        String path = readPath(args);
        if (path == null) {
            // TODO: output user messege
            return;
        }
        final HttpMockServer httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(path), NetworkType.NONE);
        try {
            synchronized (httpMockServer) {
                httpMockServer.wait();
            }
        } catch (InterruptedException e) {
            // Interrupting is expected way to stop this sleep.
            try {
                System.out.println("Shouting server down ...");
                httpMockServer.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String readPath(String[] args) {
        return args.length > 0 ? args[0] : null;
    }
}
