package com.byoutline.mockserver.sample;


import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

/**
 * Created by michalp on 25.04.16.
 */
public class ConfigServer {
    private static HttpMockServer httpMockServer;

    public static void main(String... args) {
        // checkArguments(args);
        addShutDownHook();

        String path = null;
        path = readPath(path, args);
        startServer(path);

        while (true){

        }
    }

    private static void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shouting server down ...");
                    httpMockServer.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void startServer(String path) {
        try {
            httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(path), NetworkType.GPRS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readPath(String path, String[] args) {
        if (args.length > 0) {
                path = args[0];
                System.out.println(path);
        }
        return path;
    }

    private static void checkArguments(String[] args) {
        for (String s: args) {
            System.out.println(s);
        }
    }
}
