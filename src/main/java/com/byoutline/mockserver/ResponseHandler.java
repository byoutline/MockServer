package com.byoutline.mockserver;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes care of replying to requests.
 *
 * @author Sylwester Madej <sylwester.madej at byoutline.com>
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 14.04.14.
 */
public class ResponseHandler {

    private final static Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());
    private final List<Map.Entry<RequestParams, ResponseParams>> responses;
    private final Random random = new Random();
    private final NetworkType networkType;
    private final ConfigReader configReader;

    public ResponseHandler(@Nonnull List<Map.Entry<RequestParams, ResponseParams>> responses,
            @Nonnull NetworkType networkType, @Nonnull ConfigReader fileReader) {
        this.responses = responses;
        this.networkType = networkType;
        this.configReader = fileReader;
    }

    public void handle(@Nonnull Request req, @Nonnull Response resp) {
        String path = req.getPath().getPath();
        ResponseParams rp = getResponseParams(req, path);

        try {
            setResponseFields(resp, rp);
            simulateNetworkLag();
            streamResponse(resp, rp);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private void setResponseFields(Response resp, ResponseParams rp) {
        final long time = System.currentTimeMillis();
        String contentType = getContentType(rp.staticFile, rp.message);
        resp.setContentType(contentType);
        resp.setValue("Server", "Mock");
        resp.setDate("Date", time);
        resp.setDate("Last-Modified", time);
        resp.setCode(rp.responseCode);
        for (Map.Entry<String, String> header : rp.headers.entrySet()) {
            resp.setValue(header.getKey(), header.getValue());
        }
    }

    private void streamResponse(Response resp, ResponseParams rp) throws IOException {
        OutputStream body = null;
        try {
            if (rp.staticFile) {
                String fileName = rp.message;

                body = resp.getOutputStream();

                byte[] buffer = new byte[32 * 1024];
                InputStream input = configReader.getStaticFile(fileName);
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
            } else if (responseMsg.endsWith(".html")) {
                return "text/html";
            } else {
                return "text/plain; charset=utf-8";
            }
        }
        return "application/json; charset=utf-8";
    }

    public void stopResponding() {
        responses.clear();
    }

    ResponseParams getResponseParams(Request req, String path) {
        for (Map.Entry<RequestParams, ResponseParams> response : responses) {
            if (response.getKey().matches(req)) {
                return response.getValue();
            }
        }
        
        if (getInputStreamOrNull(path) != null) {
                return new ResponseParams(path, true, Collections.EMPTY_MAP);
        }
        LOGGER.warning("No response found...returning 404");
        return new ResponseParams(404, "", DefaultValues.PARAMS, false, Collections.EMPTY_MAP);
    }

    private InputStream getInputStreamOrNull(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        try {
            return configReader.getStaticFile(fileName);
        } catch (IOException ex) {
            return null;
        }
    }
}
