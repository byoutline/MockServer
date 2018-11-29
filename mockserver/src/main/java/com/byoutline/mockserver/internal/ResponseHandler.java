package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.ConfigReader;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes care of replying to requests.
 *
 * @author Sylwester Madej |sylwester.madej at byoutline.com|
 * @author Sebastian Kacprzak |sebastian.kacprzak at byoutline.com| on 14.04.14.
 */
public class ResponseHandler {

    private final static Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());
    private final List<Map.Entry<RequestParams, ResponseParams>> responses;
    private final MockNetworkLag mockNetworkLag;
    private final ConfigReader configReader;

    public ResponseHandler(@Nonnull List<Map.Entry<RequestParams, ResponseParams>> responses,
                           @Nonnull MockNetworkLag mockNetworkLag, @Nonnull ConfigReader fileReader) {
        this.responses = responses;
        this.mockNetworkLag = mockNetworkLag;
        this.configReader = fileReader;
    }

    public void handle(@Nonnull Request req, @Nonnull Response resp) {
        String path = req.getPath().getPath();
        ResponseParams rp = getResponseParams(req, path);

        try {
            setResponseFields(resp, rp);
            mockNetworkLag.simulateNetworkLag();
            streamResponse(resp, rp);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private void setResponseFields(Response resp, ResponseParams rp) {
        final long time = System.currentTimeMillis();
        String contentType = getContentType(rp);
        resp.setContentType(contentType);
        resp.setValue("Server", "Mock");
        resp.setDate("Date", time);
        resp.setDate("Last-Modified", time);
        resp.setCode(rp.getResponseCode());
        for (Map.Entry<String, String> header : rp.getHeaders().entrySet()) {
            resp.setValue(header.getKey(), header.getValue());
        }
    }

    private void streamResponse(Response resp, ResponseParams rp) throws IOException {
        OutputStream body = null;
        try {
            if (rp.isStaticFile()) {
                String fileName = rp.getMessage();

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
                    ((PrintStream) body).print(rp.getMessage());
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

    private String getContentType(ResponseParams rp) {
        String responseMsg = rp.getMessage();
        if (rp.isStaticFile()) {
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

        String adjustedPath = path;
        if (configReader.isFolder(adjustedPath)) {
            adjustedPath += "/index.html";
        }
        if (getInputStreamOrNull(adjustedPath) != null) {
            return ResponseParams.create(adjustedPath, true, Collections.<String, String>emptyMap());
        }
        LOGGER.warning("No response found for " + path + " : returning 404");
        return ResponseParams.create(404, "", false, Collections.<String, String>emptyMap());
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
