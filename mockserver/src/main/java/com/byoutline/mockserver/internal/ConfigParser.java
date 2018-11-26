package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.ConfigReader;
import com.byoutline.mockserver.DefaultValues;
import com.byoutline.mockserver.HttpMockServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Parses {@link HttpMockServer} config file and
 * responses from assets.
 *
 * @author Sylwester Madej
 * @author Sebastian Kacprzak |sebastian.kacprzak at byoutline.com| on 14.04.14.
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
        int port = configJson.optInt(ConfigKeys.PORT, -1);
        int maxPort = port;
        if (port == -1) {
            port = DefaultValues.MOCK_SERVER_PORT;
            maxPort = DefaultValues.MOCK_SERVER_MAX_PORT;
        }

        JSONArray jsonArrayOfRequests = configJson.has(ConfigKeys.REQUESTS)
                ? configJson.getJSONArray(ConfigKeys.REQUESTS)
                : new JSONArray();

        parseRequests(jsonArrayOfRequests);
        return new ParsedConfig(port, maxPort, responses);
    }

    private void parseRequests(JSONArray jsonArrayOfRequests) throws IOException {
        for (int i = 0; i < jsonArrayOfRequests.length(); i++) {
            JSONObject requestJsonObject;
            try {
                requestJsonObject = jsonArrayOfRequests.getJSONObject(i);
            } catch (JSONException ex) {
                String requestFile = jsonArrayOfRequests.getString(i);
                String requestContent = readPartialConfigFile(requestFile, fileReader);
                requestJsonObject = new JSONObject(requestContent);
            }
            parsePathConfig(requestJsonObject);
        }
    }

    private void parsePathConfig(JSONObject requestJsonObject) throws JSONException, IOException {
        RequestParams path = getPathFromJson(requestJsonObject, fileReader);
        int responseCode = getIntOrDef(requestJsonObject, ConfigKeys.CODE, DefaultValues.RESPONSE_CODE);
        final String message;
        if (requestJsonObject.has(ConfigKeys.RESPONSE_FILE)) {
            String responseFileName = requestJsonObject.getString(ConfigKeys.RESPONSE_FILE);
            String responseContentString = readPartialConfigFile(responseFileName, fileReader);
            message = getParsedMessage(responseContentString, responseFileName);
        } else {
            message = parseConfigResponse(requestJsonObject);
        }
        Map<String, String> headers = getResponseHeaders(requestJsonObject);
        addRequestAndResponse(path, responseCode, message, headers);
    }


    private Map<String, String> getResponseHeaders(JSONObject requestJsonObject) throws JSONException {
        if (requestJsonObject.has(ConfigKeys.RESPONSE_HEADERS)) {
            JSONObject headers = requestJsonObject.getJSONObject(ConfigKeys.RESPONSE_HEADERS);
            return getStringStringMapFromJson(headers);
        } else {
            return Collections.<String, String>emptyMap();
        }
    }

    /**
     * Not private for testing. Converts {@link JSONObject} into {@link RequestParams}
     *
     * @param requestJsonObject json to be converted
     * @return {@link RequestParams} with values from json
     * @throws JSONException if object does not match {@link RequestParams} syntax
     */
    static RequestParams getPathFromJson(@Nonnull JSONObject requestJsonObject, @Nonnull ConfigReader configReader)
            throws JSONException, IOException {
        String method = requestJsonObject.getString(ConfigKeys.METHOD);
        String bodyMustContain = getStringOrDef(requestJsonObject, ConfigKeys.BODY_CONTAINS, "");
        Map<String, String> headersMap = getRequestHeaders(requestJsonObject, configReader);
        try {
            JSONObject pathObject = requestJsonObject.getJSONObject(ConfigKeys.PATH);
            Map<String, String> queryMap = getPathQueries(pathObject, configReader);
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

    private static Map<String, String> getRequestHeaders(JSONObject requestJsonObject, ConfigReader configReader) throws IOException {
        return getStringMapFromFileAndObject(requestJsonObject, configReader, ConfigKeys.REQUEST_HEADERS_FILE, ConfigKeys.REQUEST_HEADERS);
    }


    private static Map<String, String> getPathQueries(JSONObject pathObject, ConfigReader configReader) throws IOException {
        return getStringMapFromFileAndObject(pathObject, configReader, ConfigKeys.PATH_QUERIES_FILE, ConfigKeys.PATH_QUERIES);
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

    private static String parseConfigResponse(JSONObject requestJsonObject) throws JSONException {
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

    private static int getIntOrDef(JSONObject json, String key, int defaultValue) throws JSONException {
        return json.has(key) ? json.getInt(key) : defaultValue;
    }

    @Nonnull
    private static String getStringOrDef(JSONObject json, String key, @Nonnull String defaultValue) throws JSONException {
        return json.has(key) ? json.getString(key) : defaultValue;
    }

    private static String getParsedMessage(String respContentString, String fileName) throws JSONException {
        if (fileName.endsWith(ConfigKeys.JSON_EXTENSION)) {
            try {
                return new JSONObject(respContentString).toString();
            } catch (JSONException ex) {
                return new JSONArray(respContentString).toString();
            }
        }
        //remain types return as string without validation
        else return respContentString;
    }

    private static Map<String, String> getStringMapFromFileAndObject(JSONObject requestJsonObject, ConfigReader configReader, String fileConfigKey, String objectConfigKey) throws IOException {
        Map<String, String> headersFromFile = getStringMapFromFile(requestJsonObject, configReader, fileConfigKey);
        Map<String, String> headersFromObject = getStringMapFromObject(requestJsonObject, objectConfigKey);
        // Override headers from file with headers from object if needed.
        Map<String, String> headers = new HashMap<String, String>(headersFromFile);
        headers.putAll(headersFromObject);
        return headers;
    }

    private static Map<String, String> getStringMapFromFile(JSONObject requestJsonObject, ConfigReader configReader, String key)
            throws IOException {
        String headersFilePath = getStringOrDef(requestJsonObject, key, "");
        if (!headersFilePath.isEmpty()) {
            String headersFileContent = readPartialConfigFile(headersFilePath, configReader);
            JSONObject headersFromFileJson = new JSONObject(headersFileContent);
            return getStringStringMapFromJson(headersFromFileJson);
        }
        return Collections.<String, String>emptyMap();
    }

    private static Map<String, String> getStringMapFromObject(JSONObject requestJsonObject, String requestHeaders) {
        if (!requestJsonObject.has(requestHeaders)) {
            return Collections.<String, String>emptyMap();
        }
        JSONObject headers = requestJsonObject.getJSONObject(requestHeaders);
        return getStringStringMapFromJson(headers);
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

    private static String readPartialConfigFile(String fileName, ConfigReader fileReader) throws IOException {
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
