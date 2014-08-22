package com.byoutline.mockserver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.simpleframework.http.Request;

import java.io.IOException;
import java.util.Map;

/**
 * @author Sylwester Madej
 */
final class ResponseParams {

    final int responseCode;
    final String message;
    final String params;
    final Map<String, String> headers;

    public ResponseParams(@Nonnull String message, @Nonnull Map<String, String> headers) {
        this(200, message, "", headers);
    }
    
    public ResponseParams(int responseCode, @Nonnull String message, @Nonnull String params, @Nonnull Map<String, String> headers) {
        this.responseCode = responseCode;
        this.message = message;
        this.params = params;
        this.headers = headers;
    }
}

/**
 * @author Sebastian Kacprzak <nait at naitbit.com> on 15.04.14.
 */
final class ResponsePath {
    final String method;
    final String basePath;
    final boolean useRegexForPath;
    final String bodyMustContain;
    final Map<String, String> queries;

    ResponsePath(@Nonnull String method,
                 @Nonnull String basePath, @Nonnull boolean useRegexForPath,
                 @Nullable String bodyMustContain,
                 @Nonnull Map<String, String> queries) {
        this.method = method;
        this.basePath = basePath;
        this.useRegexForPath = useRegexForPath;
        this.bodyMustContain = bodyMustContain;
        this.queries = queries;
    }

    public boolean matches(Request req) {
        if (!method.equals(req.getMethod())) return false;
        if (useRegexForPath) {
            if (!req.getPath().getPath().matches(basePath)) return false;
        } else {
            if (!basePath.equals(req.getPath().getPath())) return false;
        }
        if (!queries.keySet().containsAll(req.getQuery().keySet())) return false;
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
        return true;
    }
    
    private static boolean isEmpty(String string) {
        if(string == null) {
            return true;
        }
        return string.isEmpty();
    }
}