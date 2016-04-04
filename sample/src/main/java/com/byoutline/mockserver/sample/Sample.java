package com.byoutline.mockserver.sample;

import com.byoutline.mockserver.ConfigReader;
import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) {
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);

        shutDownSerwer();
    }


    private static void shutDownSerwer() {
        try {
            Thread.sleep(100000);
            httpMockServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//todo
//check in web browser


