package com.byoutline.mockserver.internal;


import com.byoutline.mockserver.ConfigReader;
import com.byoutline.mockserver.DefaultValues;
import com.byoutline.mockserver.TestConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConfigParserTest {

    private ConfigParser configParser;
    private JSONObject configJson;

    @Before
    public void setUp() throws Exception {
        configParser = new ConfigParser(mock(ConfigReader.class));
    }

    @Test
    public void shouldSetDefaultPort() throws Exception {
        configJson = new JSONObject("{}");

        ParsedConfig result = configParser.parseConfig(configJson);

        assertThat(result.port).isEqualTo(DefaultValues.MOCK_SERVER_PORT);
    }

    @Test
    public void shouldParsePort() throws Exception {
        int port = 8000;
        configJson = new JSONObject("{\"port\":" + port + "}");

        ParsedConfig configResult = configParser.parseConfig(configJson);
        assertThat(configResult.port).isEqualTo(port);
    }

    @Test
    public void shouldHaveMethodInRequest() throws Exception {
        //given
        JSONObject request = getRequestWithMethod(TestConstants.METHOD, TestConstants.TEST_PATH);

        configJson = getConfigWithSingleRequest(request);

        //when
        ParsedConfig configResult = configParser.parseConfig(configJson);

        //then
        assertThat(configResult.responses.get(0).getKey().method).isEqualTo(TestConstants.METHOD);

    }

    @Test
    public void shouldHavePathInRequest() throws Exception {
        //given
        JSONObject request = getRequestWithMethod(TestConstants.METHOD, TestConstants.TEST_PATH);

        configJson = getConfigWithSingleRequest(request);

        //when
        ParsedConfig configResult = configParser.parseConfig(configJson);

        //then
        assertThat(configResult.responses.get(0).getKey().basePath).isEqualTo(TestConstants.TEST_PATH);

    }

    @Test
    public void shouldParseRequestHeaders() throws Exception {
        //given
        String name = "name";
        String value = "value";
        JSONObject request = getRequestWithMethod(TestConstants.METHOD, TestConstants.TEST_PATH);

        JSONObject headersJson = new JSONObject();
        headersJson.put(name, value);
        request.put("headers", headersJson);
        configJson = getConfigWithSingleRequest(request);

        //when
        ParsedConfig result = configParser.parseConfig(configJson);

        //then
        Map<String, String> headers = result.responses.get(0).getKey().headers;

        assertThat(headers.get(name)).isEqualTo(value);
    }

    private JSONObject getConfigWithSingleRequest(JSONObject request) {
        JSONObject config = new JSONObject();
        JSONArray requestsJsonArray = new JSONArray();
        config.put("requests", requestsJsonArray);
        requestsJsonArray.put(request);
        return config;
    }

    private JSONObject getRequestWithMethod(String method, String path) {
        JSONObject singleRequest = new JSONObject();
        singleRequest.put("method", method);
        singleRequest.put("path", path);
        return singleRequest;
    }
}