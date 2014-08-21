package com.byoutline.mockserver;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class URLEncoderUtils {


    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    private URLEncoderUtils() {
    }

    public static Map<String, Object> parse(final String query, final String encoding) {
        Map<String, Object> result = Collections.emptyMap();
        if (query != null && query.length() > 0) {
            result = new HashMap<String, Object>();
            parse(result, new Scanner(query), encoding);
        }
        return result;
    }

    public static void parse(
            final Map<String, Object> parameters,
            final Scanner scanner,
            final String encoding) {
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2) {
                throw new IllegalArgumentException("bad parameter");
            }
            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2) {
                value = decode(nameValue[1], encoding);
            }
            parameters.put(name, value);
        }
    }

    public static String format(
            final Map<String, String> parameters,
            final String encoding, String optionalQuery) {
        final StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            final String value = String.valueOf(parameter.getValue());
            final String encodedName = parameter.getKey();
            final String encodedValue = (value != null) ? value : "";
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        if (optionalQuery != null) {
            result.append(optionalQuery);
        }
        return result.toString();
    }

    private static String decode(final String content, final String encoding) {
        try {
            return URLDecoder.decode(content,
                    encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

}
