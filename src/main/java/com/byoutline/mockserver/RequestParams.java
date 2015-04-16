package com.byoutline.mockserver;

import org.simpleframework.http.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 15.04.14.
 */
final class RequestParams {
    final String method;
    final String basePath;
    final boolean useRegexForPath;
    final String bodyMustContain;
    final Map<String, String> queries;
    final Map<String, String> headers;

    RequestParams(@Nonnull String method,
                  @Nonnull String basePath, @Nonnull boolean useRegexForPath,
                  @Nullable String bodyMustContain,
                  @Nonnull Map<String, String> queries, Map<String, String> headers) {
        this.method = method;
        this.basePath = basePath;
        this.useRegexForPath = useRegexForPath;
        this.bodyMustContain = bodyMustContain;
        this.queries = queries;
        this.headers = headers;
    }

    /**
     * Checks if HTTP request matches all fields specified in config.
     * Fails on first mismatch. Both headers and query params can be configured as regex.
     * @param req
     * @return
     */
    public boolean matches(Request req) {
        if (!method.equals(req.getMethod())) return false;
        if (useRegexForPath) {
            if (!req.getPath().getPath().matches(basePath)) return false;
        } else {
            if (!basePath.equals(req.getPath().getPath())) return false;
        }
        if (!queries.keySet().containsAll(req.getQuery().keySet())) return false;
        if (!req.getNames().containsAll(headers.keySet())) return false;

        try {
            if (!isEmpty(bodyMustContain) && !req.getContent().contains(bodyMustContain))
                return false;
        } catch (IOException e) {
            return false;
        }

        for (Map.Entry<String, String> reqQuery : req.getQuery().entrySet()) {
            String respRegex = queries.get(reqQuery.getKey());
            if (!reqQuery.getValue().matches(respRegex)) return false;
        }
        for(Map.Entry<String, String> header : headers.entrySet()) {
            String headerValueRegex = header.getValue();
            if(!req.getValue(header.getKey()).matches(headerValueRegex)) return false;
        }
        return true;
    }

    //requires java 6, will not work on Android < 2.3
    private static boolean isEmpty(String string) {
        if(string == null) {
            return true;
        }
        return string.isEmpty();
    }
}
