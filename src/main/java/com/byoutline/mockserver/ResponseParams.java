package com.byoutline.mockserver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.simpleframework.http.Request;

import java.io.IOException;
import java.util.Map;

final class ResponseParams {

    final int responseCode;
    final String message;
    final String params;
    final boolean staticFile;
    final Map<String, String> headers;

    public ResponseParams(@Nonnull String message, boolean staticFile, @Nonnull Map<String, String> headers) {
        this(DefaultValues.RESPONSE_CODE, message, DefaultValues.PARAMS, staticFile, headers);
    }
    
    public ResponseParams(int responseCode, @Nonnull String message, @Nonnull String params, boolean staticFile, @Nonnull Map<String, String> headers) {
        this.responseCode = responseCode;
        this.message = message;
        this.params = params;
        this.staticFile = staticFile;
        this.headers = headers;
    }
}

