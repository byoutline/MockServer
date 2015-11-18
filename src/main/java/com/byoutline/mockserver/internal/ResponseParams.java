package com.byoutline.mockserver.internal;

import com.byoutline.mockserver.DefaultValues;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Map;

@AutoValue
public abstract class ResponseParams {

    public static ResponseParams create(@Nonnull String message, boolean staticFile, @Nonnull Map<String, String> headers) {
        return new AutoValue_ResponseParams(DefaultValues.RESPONSE_CODE, message, DefaultValues.PARAMS, staticFile, headers);
    }

    public static ResponseParams create(int responseCode, @Nonnull String message, @Nonnull String params, boolean staticFile, @Nonnull Map<String, String> headers) {
        return new AutoValue_ResponseParams(responseCode, message, params, staticFile, headers);
    }

    public abstract int getResponseCode();

    @Nonnull
    public abstract String getMessage();

    public abstract String getParams();

    public abstract boolean isStaticFile();

    @Nonnull
    public abstract Map<String, String> getHeaders();
}

