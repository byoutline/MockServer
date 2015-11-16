package com.byoutline.mockserver.internal;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public enum MatchingMethod {
    EXACT("EXACT"), CONTAINS("CONTAINS"), NOT_CONTAINS("NOT_CONTAINS");
    public final String configValue;

    MatchingMethod(String configValue) {
        this.configValue = configValue;
    }
}
