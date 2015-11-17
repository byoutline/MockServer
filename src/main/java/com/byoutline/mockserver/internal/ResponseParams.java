package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.DefaultValues;

import javax.annotation.Nonnull;
import java.util.Map;

public final class ResponseParams {

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

