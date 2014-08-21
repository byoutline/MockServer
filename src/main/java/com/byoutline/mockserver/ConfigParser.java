package com.byoutline.mockserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses {@link com.byoutline.mockserver.HttpMockServer} config file and
 * responses from assets.
 *
 * @author Sylwester Madej
 * @author Sebastian Kacprzak <nait at naitbit.com> on 14.04.14.
 */
class ConfigParser {

    private final ConfigReader fileReader;
    private final List<Map.Entry<ResponsePath, ResponseParams>> responses = new ArrayList<Map.Entry<ResponsePath, ResponseParams>>();

    ConfigParser(@Nonnull ConfigReader fileReader) {
        this.fileReader = fileReader;
    }

    public HttpMockServer.ConfigResult parseConfig(@Nonnull JSONObject configJson) throws JSONException, IOException {
        int port = configJson.optInt(ConfigKeys.PORT, HttpMockServer.MOCK_SERVER_PORT);
        JSONArray jsonArrayOfRequests = configJson.getJSONArray(ConfigKeys.REQUESTS);

        for (int i = 0; i < jsonArrayOfRequests.length(); i++) {
            JSONObject requestJsonObject = jsonArrayOfRequests.getJSONObject(i);
            parsePathConfig(requestJsonObject);
        }
        return new HttpMockServer.ConfigResult(port, responses);
    }

    private void parsePathConfig(JSONObject requestJsonObject) throws JSONException, IOException {
        ResponsePath path = getPathFromJson(requestJsonObject);
        int responseCode = requestJsonObject.getInt(ConfigKeys.CODE);
        final String message;
        if (requestJsonObject.has(ConfigKeys.RESPONSE_FILE)) {
            String responseFileName = requestJsonObject.getString(ConfigKeys.RESPONSE_FILE);
            String responseContentString = readResponseFile(responseFileName);
            message = toJsonString(responseContentString);
        } else {
            message = parseConfigResponse(requestJsonObject);
        }
        Map<String, String> headers = getResponseHeaders(requestJsonObject);
        String params = requestJsonObject.getString(ConfigKeys.REQUEST);
        addRequestAndResponse(path, params, responseCode, message, headers);
    }

    private Map<String, String> getResponseHeaders(JSONObject requestJsonObject) throws JSONException {
        if (requestJsonObject.has(ConfigKeys.RESPONSE_HEADERS)) {
            JSONObject headers = requestJsonObject.getJSONObject(ConfigKeys.RESPONSE_HEADERS);
            return getStringStringMapFromJson(headers);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    private String toJsonString(String responseString) throws JSONException {
        try {
            return new JSONObject(responseString).toString();
        } catch (JSONException ex) {
            return new JSONArray(responseString).toString();
        }
    }

    private ResponsePath getPathFromJson(JSONObject requestJsonObject) throws JSONException {
        String method = requestJsonObject.getString(ConfigKeys.METHOD);
        String bodyMustContain = getBodyMustContain(requestJsonObject);
        try {
            JSONObject pathObject = requestJsonObject.getJSONObject(ConfigKeys.PATH);
            Map<String, String> queryMap = getPathQueries(pathObject);
            if (pathObject.has(ConfigKeys.PATH_BASE)) {
                String basePath = pathObject.getString(ConfigKeys.PATH_BASE);
                return new ResponsePath(method, basePath, false, bodyMustContain, queryMap);
            } else {
                String basePath = pathObject.getString(ConfigKeys.URL_PATTERN);
                return new ResponsePath(method, basePath, true, bodyMustContain, queryMap);
            }
        } catch (JSONException ex) {
            String basePath = requestJsonObject.getString(ConfigKeys.PATH);
            return new ResponsePath(method, basePath, false, bodyMustContain, Collections.EMPTY_MAP);
        }
    }

    private Map<String, String> getPathQueries(JSONObject pathObject) throws JSONException {
        if (!pathObject.has(ConfigKeys.PATH_QUERIES)) {
            return Collections.EMPTY_MAP;
        }
        JSONObject queries = pathObject.getJSONObject(ConfigKeys.PATH_QUERIES);
        return getStringStringMapFromJson(queries);
    }

    private String getBodyMustContain(JSONObject requestJsonObject) throws JSONException {
        if (requestJsonObject.has(ConfigKeys.BODY_PATTERNS)) {
            JSONObject bodyPatterns = requestJsonObject.getJSONObject(ConfigKeys.BODY_PATTERNS);
            if (bodyPatterns.has(ConfigKeys.BODY_CONTAINS)) {
                return bodyPatterns.getString(ConfigKeys.BODY_CONTAINS);
            }
        }
        return "";
    }

    private static Map<String, String> getStringStringMapFromJson(JSONObject queries) throws JSONException {
        Iterator queryIterator = queries.keys();
        Map<String, String> queryMap = new HashMap<String, String>(8);
        while (queryIterator.hasNext()) {
            String query = queryIterator.next().toString();
            String value = queries.getString(query);
            queryMap.put(query, value);
        }
        return queryMap;
    }

    private String parseConfigResponse(JSONObject requestJsonObject) throws JSONException {
        try {
            return requestJsonObject.getJSONObject(ConfigKeys.RESPONSE).toString();
        } catch (JSONException ex) {
            return requestJsonObject.getString(ConfigKeys.RESPONSE);
        }
    }

    public void addRequestAndResponse(ResponsePath path, String params, int responseCode, String message, Map<String, String> headers) {
        ResponseParams rp = new ResponseParams(responseCode, message, params, headers);
        this.responses.add(new AbstractMap.SimpleImmutableEntry<ResponsePath, ResponseParams>(path, rp));
    }
    
    private String readResponseFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileReader.getResponseConfigFromFileAsStream(fileName), "UTF-8"));

        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine);
            mLine = reader.readLine();
        }

        reader.close();
        return sb.toString();
    }
}
