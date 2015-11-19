package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.ConfigReader;
import com.byoutline.mockserver.DefaultValues;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * Parses {@link com.byoutline.mockserver.HttpMockServer} config file and
 * responses from assets.
 *
 * @author Sylwester Madej
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 14.04.14.
 */
public class ConfigParser {

    private final ConfigReader fileReader;
    private final List<Map.Entry<RequestParams, ResponseParams>> responses = new ArrayList<Map.Entry<RequestParams, ResponseParams>>();

    public ConfigParser(@Nonnull ConfigReader fileReader) {
        this.fileReader = fileReader;
    }

    public static byte[] readInitialData(@Nullable InputStream inputStream)
            throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i = inputStream.read();
        while (i != -1) {
            byteArrayOutputStream.write(i);
            i = inputStream.read();
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static JSONObject getMainConfigJson(@Nonnull ConfigReader configReader) throws IOException {
        String configJson = new String(readInitialData(configReader.getMainConfigFile()));
        return configJson.isEmpty() ? new JSONObject() : new JSONObject(configJson);
    }

    public ParsedConfig parseConfig(@Nonnull JSONObject configJson) throws JSONException, IOException {
        int port = configJson.optInt(ConfigKeys.PORT, DefaultValues.MOCK_SERVER_PORT);

        JSONArray jsonArrayOfRequests = configJson.has(ConfigKeys.REQUESTS)
                ? configJson.getJSONArray(ConfigKeys.REQUESTS)
                : new JSONArray();

        parseRequests(jsonArrayOfRequests);
        return new ParsedConfig(port, responses);
    }

    private void parseRequests(JSONArray jsonArrayOfRequests) throws IOException {
        for (int i = 0; i < jsonArrayOfRequests.length(); i++) {
            JSONObject requestJsonObject;
            try {
                requestJsonObject = jsonArrayOfRequests.getJSONObject(i);
            } catch (JSONException ex) {
                String requestFile = jsonArrayOfRequests.getString(i);
                String requestContent = readPartialConfigFile(requestFile);
                requestJsonObject = new JSONObject(requestContent);
            }
            parsePathConfig(requestJsonObject);
        }
    }

    private void parsePathConfig(JSONObject requestJsonObject) throws JSONException, IOException {
        RequestParams path = getPathFromJson(requestJsonObject);
        int responseCode = getIntOrDef(requestJsonObject, ConfigKeys.CODE, DefaultValues.RESPONSE_CODE);
        final String message;
        if (requestJsonObject.has(ConfigKeys.RESPONSE_FILE)) {
            String responseFileName = requestJsonObject.getString(ConfigKeys.RESPONSE_FILE);
            String responseContentString = readPartialConfigFile(responseFileName);
            message = toJsonString(responseContentString);
        } else {
            message = parseConfigResponse(requestJsonObject);
        }
        Map<String, String> headers = getResponseHeaders(requestJsonObject);
        addRequestAndResponse(path, responseCode, message, headers);
    }

    private int getIntOrDef(JSONObject json, String key, int defaultValue) throws JSONException {
        return json.has(key) ? json.getInt(key) : defaultValue;
    }

    private static String getStringOrDef(JSONObject json, String key, String defaultValue) throws JSONException {
        return json.has(key) ? json.getString(key) : defaultValue;
    }

    private Map<String, String> getResponseHeaders(JSONObject requestJsonObject) throws JSONException {
        if (requestJsonObject.has(ConfigKeys.RESPONSE_HEADERS)) {
            JSONObject headers = requestJsonObject.getJSONObject(ConfigKeys.RESPONSE_HEADERS);
            return getStringStringMapFromJson(headers);
        } else {
            return Collections.<String, String>emptyMap();
        }
    }

    private String toJsonString(String responseString) throws JSONException {
        try {
            return new JSONObject(responseString).toString();
        } catch (JSONException ex) {
            return new JSONArray(responseString).toString();
        }
    }

    /**
     * Not private for testing. Converts {@link JSONObject} into {@link RequestParams}
     *
     * @param requestJsonObject json to be converted
     * @return {@link RequestParams} with values from json
     * @throws JSONException if object does not match {@link RequestParams} syntax
     */
    static RequestParams getPathFromJson(JSONObject requestJsonObject) throws JSONException {
        String method = requestJsonObject.getString(ConfigKeys.METHOD);
        String bodyMustContain = getStringOrDef(requestJsonObject, ConfigKeys.BODY_CONTAINS, "");
        Map<String, String> headersMap = getRequestHeaders(requestJsonObject);
        try {
            JSONObject pathObject = requestJsonObject.getJSONObject(ConfigKeys.PATH);
            Map<String, String> queryMap = getPathQueries(pathObject);
            MatchingMethod queryMatchingMethod = getQueryMappingMethod(pathObject);

            if (pathObject.has(ConfigKeys.PATH_BASE)) {
                String basePath = pathObject.getString(ConfigKeys.PATH_BASE);
                return RequestParams.create(method, basePath, false, bodyMustContain,
                        queryMap, queryMatchingMethod, headersMap);
            } else {
                String basePath = pathObject.getString(ConfigKeys.URL_PATTERN);
                return RequestParams.create(method, basePath, true, bodyMustContain,
                        queryMap, queryMatchingMethod, headersMap);
            }
        } catch (JSONException ex) {
            String basePath = requestJsonObject.getString(ConfigKeys.PATH);
            return RequestParams.create(method, basePath, false, bodyMustContain,
                    Collections.<String, String>emptyMap(), DefaultValues.QUERY_MATCHING_METHOD, headersMap);
        }
    }

    private static Map<String, String> getRequestHeaders(JSONObject requestJsonObject) {
        if (!requestJsonObject.has(ConfigKeys.REQUEST_HEADERS)) {
            return Collections.emptyMap();
        }
        JSONObject headers = requestJsonObject.getJSONObject(ConfigKeys.REQUEST_HEADERS);
        return getStringStringMapFromJson(headers);
    }

    private static Map<String, String> getPathQueries(JSONObject pathObject) throws JSONException {
        if (!pathObject.has(ConfigKeys.PATH_QUERIES)) {
            return Collections.emptyMap();
        }
        JSONObject queries = pathObject.getJSONObject(ConfigKeys.PATH_QUERIES);
        return getStringStringMapFromJson(queries);
    }

    private static MatchingMethod getQueryMappingMethod(JSONObject pathObject) {
        if (!pathObject.has(ConfigKeys.PATH_QUERIES_MATCHING_METHOD)) {
            // default value
            return DefaultValues.QUERY_MATCHING_METHOD;
        }
        String matchingMethod = pathObject.getString(ConfigKeys.PATH_QUERIES_MATCHING_METHOD);
        for (MatchingMethod queryMatchingMethod : MatchingMethod.values()) {
            if (queryMatchingMethod.configValue.equals(matchingMethod)) {
                return queryMatchingMethod;
            }
        }
        throw new JSONException("Invalid " + ConfigKeys.PATH_QUERIES_MATCHING_METHOD + " value: " + matchingMethod);
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
            return getStringOrDef(requestJsonObject, ConfigKeys.RESPONSE, DefaultValues.RESPONSE);
        }
    }

    private void addRequestAndResponse(RequestParams path, int responseCode, String message, Map<String, String> headers) {
        ResponseParams rp = ResponseParams.create(responseCode, message, false, headers);
        this.responses.add(new AbstractMap.SimpleImmutableEntry<RequestParams, ResponseParams>(path, rp));
    }

    private String readPartialConfigFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileReader.getPartialConfigFromFile(fileName), "UTF-8"));

        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine);
            mLine = reader.readLine();
        }

        reader.close();
        return sb.toString();
    }
}
