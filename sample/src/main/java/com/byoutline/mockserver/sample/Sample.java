package com.byoutline.mockserver.sample;

import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Sample that starts server (with config defined in resources),
 * makes a requests and prints a response.
 */
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) throws IOException, InterruptedException {
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);
        doSomethingWithUsingHttpMockServer();
        shutDownSerwer();
    }

    private static void doSomethingWithUsingHttpMockServer() {
        String urlString = "http://localhost:8099/books";
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            conn.disconnect();
            System.out.print(result.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutDownSerwer() {
        try {
            httpMockServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



