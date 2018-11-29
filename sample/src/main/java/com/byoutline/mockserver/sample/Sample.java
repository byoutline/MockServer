package com.byoutline.mockserver.sample;

import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Sample that starts server (with config defined in resources),
 * makes a requests and prints a response.
 */
public class Sample {

    private static HttpMockServer httpMockServer;

    public static void main(String... args) throws IOException, InterruptedException {
        httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(), NetworkType.GPRS);
        System.out.println("Sample with JSON file");
        fetchJsonWithUsingHttpMockServer();
        System.out.println("Sample with XML file");
        fetchXmlWithUsingHttpMockServer();
        System.out.println("Sample with another file");
        fetchSomeFileWithUsingHttpMockServer();
        System.out.println("Sample with static file");
        fetchImageWithUsingHttpMockServer();
        shutDownSerwer();
    }

    private static void fetchJsonWithUsingHttpMockServer() {
        callAndPrintResponse("http://localhost:8099/books","GET");
    }

    private static void fetchXmlWithUsingHttpMockServer() {
        callAndPrintResponse("http://localhost:8099/testxml","POST");
    }

    private static void fetchSomeFileWithUsingHttpMockServer() {
        callAndPrintResponse("http://localhost:8099/something","GET");
    }

    private static void fetchImageWithUsingHttpMockServer() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:8099/donkey.jpg");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Image image = ImageIO.read(conn.getInputStream());
            JFrame f = new JFrame("Donkey Image");
            JLabel jLabel = new JLabel();
            jLabel.setIcon(new ImageIcon(image));
            f.getContentPane().add(jLabel);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void callAndPrintResponse(String apiUrl,String httpMethod) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(httpMethod);
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                System.out.println(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
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



