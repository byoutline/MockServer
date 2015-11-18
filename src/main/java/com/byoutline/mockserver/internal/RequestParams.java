package com.byoutline.mockserver.internal;

import com.google.auto.value.AutoValue;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 15.04.14.
 */
@AutoValue
public abstract class RequestParams {

    public static RequestParams create(@Nonnull String method,
                                       @Nonnull String basePath, boolean useRegexForPath,
                                       @Nullable String bodyMustContain,
                                       @Nonnull Map<String, String> queries, @Nonnull MatchingMethod queriesMatchingMethod,
                                       @Nonnull Map<String, String> headers) {
        return new AutoValue_RequestParams(method,
                basePath, useRegexForPath,
                bodyMustContain,
                queries, queriesMatchingMethod,
                headers);
    }

    @Nonnull
    public abstract String getMethod();

    @Nonnull
    public abstract String getBasePath();

    public abstract boolean isUseRegexForPath();

    @Nullable
    public abstract String getBodyMustContain();

    @Nonnull
    public abstract Map<String, String> getQueries();

    @Nonnull
    public abstract MatchingMethod getQueriesMatchingMethod();

    @Nonnull
    public abstract Map<String, String> getHeaders();

    /**
     * Checks if HTTP request matches all fields specified in config.
     * Fails on first mismatch. Both headers and query params can be configured as regex.
     *
     * @param req
     * @return
     */
    public boolean matches(Request req) {
        if (!getMethod().equals(req.getMethod())) return false;
        if (pathDoesNotMatch(req)) return false;
        if (bodyDoesNotMatch(req)) return false;
        if (headersDoesNotMatch(req)) return false;
        return true;
    }

    private boolean headersDoesNotMatch(Request req) {
        if (!req.getNames().containsAll(getHeaders().keySet())) return true;
        for (Map.Entry<String, String> header : getHeaders().entrySet()) {
            String headerValueRegex = header.getValue();
            if (!req.getValue(header.getKey()).matches(headerValueRegex)) return true;
        }
        return false;
    }

    private boolean queriesDoesNotMatch(Request req) {
        Query query = req.getQuery();
        switch (getQueriesMatchingMethod()) {
            case EXACT:
                return !queriesMatchExactly(query);
            case CONTAINS:
                return !queriesContainsAll(query);
            case NOT_CONTAINS:
                return !queriesDoesNotContain(query);
            default:
                throw new AssertionError("Unknown " + ConfigKeys.PATH_QUERIES_MATCHING_METHOD +
                        ": " + getQueriesMatchingMethod());
        }
    }

    private boolean queriesMatchExactly(Query reqQueries) {
        if (getQueries().size() != reqQueries.size()) {
            return false;
        }
        return queriesContainsAll(reqQueries);
    }

    private boolean queriesContainsAll(Query reqQueries) {
        for (Map.Entry<String, String> query : getQueries().entrySet()) {
            String reqValue = reqQueries.get(query.getKey());
            if (reqValue == null) {
                return false;
            }
            String queryRegex = query.getValue();
            if (!reqValue.matches(queryRegex)) return false;
        }
        return true;
    }

    private boolean queriesDoesNotContain(Query reqQueries) {
        for (Map.Entry<String, String> query : getQueries().entrySet()) {
            String reqValue = reqQueries.get(query.getKey());
            if (reqValue == null) {
                continue;
            }
            String queryRegex = query.getValue();
            if (reqValue.matches(queryRegex)) return false;
        }
        return true;
    }

    private boolean pathDoesNotMatch(Request req) {
        String reqPath = req.getPath().getPath();
        if (isUseRegexForPath()) {
            String fullReqPath;
            String query = req.getQuery().toString();
            if (query.isEmpty()) {
                fullReqPath = reqPath;
            } else {
                fullReqPath = reqPath + "?" + query;
            }
            if (!fullReqPath.matches(getBasePath())) return true;
        } else {
            if (!getBasePath().equals(reqPath)) return true;
            if (queriesDoesNotMatch(req)) return true;
        }
        return false;
    }

    private boolean bodyDoesNotMatch(Request req) {
        try {
            if (!isEmpty(getBodyMustContain()) && !req.getContent().contains(getBodyMustContain()))
                return true;
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    //requires java 6, will not work on Android < 2.3
    private static boolean isEmpty(String string) {
        if (string == null) {
            return true;
        }
        return string.isEmpty();
    }
}
