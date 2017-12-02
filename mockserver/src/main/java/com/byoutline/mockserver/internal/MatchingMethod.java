package com.byoutline.mockserver.internal;

public enum MatchingMethod {
    EXACT("EXACT"), CONTAINS("CONTAINS"), NOT_CONTAINS("NOT_CONTAINS"), ANY("ANY");
    public final String configValue;

    MatchingMethod(String configValue) {
        this.configValue = configValue;
    }
}
