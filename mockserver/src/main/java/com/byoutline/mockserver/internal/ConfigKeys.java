package com.byoutline.mockserver.internal;

/**
 * List of JSON keys that can be used in config file.
 *
 * @author Sebastian Kacprzak |sebastian.kacprzak at byoutline.com| on 14.04.14.
 */
public class ConfigKeys {
    public static final String PORT = "port";
    public static final String METHOD = "method";
    public static final String PATH = "path";
    public static final String URL_PATTERN = "url pattern";
    public static final String PATH_BASE = "base";
    public static final String PATH_QUERIES = "queries";
    public static final String PATH_QUERIES_FILE = "queries file";
    public static final String PATH_QUERIES_MATCHING_METHOD = "queries matching method";
    public static final String CODE = "code";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_FILE = "response file";
    public static final String RESPONSE_HEADERS = "response headers";
    public static final String REQUESTS = "requests";
    public static final String BODY_CONTAINS = "bodyContains";
    public static final String REQUEST_HEADERS = "headers";
    public static final String REQUEST_HEADERS_FILE = "headers file";

    //extensions
    static final String JSON_EXTENSION = ".json";
}
