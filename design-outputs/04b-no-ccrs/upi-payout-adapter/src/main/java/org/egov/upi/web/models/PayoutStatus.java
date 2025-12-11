package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PayoutStatus {

    INITIATED("INITIATED"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    REVERSED("REVERSED");

    private final String value;

    PayoutStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PayoutStatus fromValue(String value) {
        for (PayoutStatus status : PayoutStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PayoutStatus: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
