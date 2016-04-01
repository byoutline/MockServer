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
        httpMockServer = HttpMockServer.startMockApiServer(new ConfigReader() {
            @Override
            public InputStream getMainConfigFile() {
                File file = new File("sample/src/main/resources/config.json");
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return inputStream;
            }

            @Override
            public InputStream getPartialConfigFromFile(String relativePath) throws IOException {
                FileInputStream fileInputStream = new FileInputStream("sample/src/main/resources/static/books/" + relativePath);
                return fileInputStream;
            }

            @Override
            public InputStream getStaticFile(String relativePath) throws IOException {
                InputStream is = getClass().getResourceAsStream("static/"+relativePath);
                return is;
            }

            @Override
            public boolean isFolder(String relativePath) {
                try {
                    boolean directory = new File("static/" + relativePath).isFile();
                    return !directory;
                } catch (Exception e) {
                    return false;
                }
            }
        }, NetworkType.GPRS);

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
//make sure the selected port is not used
//check in web browser


