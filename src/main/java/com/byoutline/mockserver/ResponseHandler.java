package com.byoutline.mockserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Takes care of replying to requests.
 *
 * @author Sylwester Madej
 * @author Sebastian Kacprzak <nait at naitbit.com> on 14.04.14.
 */
public class ResponseHandler {

    private final static Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());
    private final List<Map.Entry<ResponsePath, ResponseParams>> responses;
    private final Random random = new Random();
    private final NetworkType networkType;
    private final ConfigReader fileReader;

    public ResponseHandler(@Nonnull List<Map.Entry<ResponsePath, ResponseParams>> responses,
            @Nonnull NetworkType networkType, @Nonnull ConfigReader fileReader) {
        this.responses = responses;
        this.networkType = networkType;
        this.fileReader = fileReader;
    }

    public void handle(@Nonnull Request req, @Nonnull Response resp) {
        String path = req.getPath().getPath();
        boolean isFile = path.startsWith("/files/");
        ResponseParams rp = getResponseParms(req, path, isFile);

        try {
            setResponseFields(resp, isFile, rp);
            simulateNetworkLag();
            streamResponse(resp, isFile, rp);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private void setResponseFields(Response resp, boolean isFile, ResponseParams rp) {
        final long time = System.currentTimeMillis();
        String contentType = getContentType(isFile, rp.message);
        resp.setContentType(contentType);
        resp.setValue("Server", "Mock");
        resp.setDate("Date", time);
        resp.setDate("Last-Modified", time);
        resp.setCode(rp.responseCode);
        for (Map.Entry<String, String> header : rp.headers.entrySet()) {
            resp.setValue(header.getKey(), header.getValue());
        }
    }

    private void streamResponse(Response resp, boolean isFile, ResponseParams rp) throws IOException {
        OutputStream body = null;
        try {
            if (isFile) {
                String fileName = rp.message;

                body = resp.getOutputStream();

                byte[] buffer = new byte[32 * 1024];
                FileInputStream input = new FileInputStream(fileReader.getResponseFile(fileName));
                int bytesRead;
                while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0) {
                    body.write(buffer, 0, bytesRead);
                }

            } else {
                body = resp.getPrintStream();
                if (body != null) {
                    ((PrintStream) body).print(rp.message);
                }
            }
        } finally {
            if (body != null) {
                try {
                    body.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Exception on MockServer close", e);
                }
            }
        }
    }

    private void simulateNetworkLag() {
        try {
            long avg = (networkType.minDelay + networkType.maxDelay) / 2;
            long spread = avg - networkType.minDelay;
            Thread.sleep(avg + random.nextLong() % spread);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "simulating network lag failed", e);
        }
    }

    private String getContentType(boolean isFile, String responseMsg) {
        if (isFile) {
            if (responseMsg.endsWith(".png")) {
                return "image/png";
            } else if (responseMsg.endsWith(".ogg")) {
                return "audio/ogg";
            } else if (responseMsg.endsWith(".jpg")) {
                return "image/jpeg";
            } else {
                return "text/plain; charset=utf-8";
            }
        }
        return "application/json; charset=utf-8";
    }

    public void stopResponding() {
        responses.clear();
    }

//    private boolean fileExists(String lookingForFile) {
//
//        for (String file : Environment.getExternalStorageDirectory().list()) {
//            if (lookingForFile.equals(file)) {
//                return true;
//            }
//        }
//        return false;
//    }
    ResponseParams getResponseParms(Request req, String path, boolean isFile) {
        if (isFile) {
            String filename = path.split("/")[2];
            if (filename != null && filename.length() > 0 && fileExists(filename)) {
                return new ResponseParams(filename, Collections.EMPTY_MAP);
            }
        }

        for (Map.Entry<ResponsePath, ResponseParams> response : responses) {
            if (response.getKey().matches(req)) {
                return response.getValue();
            }
        }
        LOGGER.warning("No response found...returning 404");
        return new ResponseParams(404, "", "", Collections.EMPTY_MAP);
    }

    private boolean fileExists(String fileName) {
        for (String file : fileReader.getResponseFolderFileNames()) {
            if (fileName.equals(file)) {
                return true;
            }
        }
        return false;
    }
}
