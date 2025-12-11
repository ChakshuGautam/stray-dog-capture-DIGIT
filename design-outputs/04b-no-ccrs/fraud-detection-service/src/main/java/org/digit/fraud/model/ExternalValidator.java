package org.digit.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ExternalValidator - Model representing an external AI/ML validator from MDMS.
 * Loaded from FRAUD-DETECTION/ExternalValidators.json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalValidator {

    private String id;
    private String name;
    private String description;
    private String type;  // AI_MODEL, EXTERNAL_API
    private boolean enabled;

    private EndpointConfig endpoint;
    private Map<String, Object> inputMapping;
    private Map<String, Object> outputMapping;
    private TimeoutConfig timeout;
    private RetryConfig retry;
    private CircuitBreakerConfig circuitBreaker;
    private CacheConfig cache;
    private FallbackConfig fallback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointConfig {
        private String url;
        private String method;
        private String authType;  // API_KEY, BEARER_TOKEN, OAUTH2, BASIC
        private String authHeader;
        private String authSecretKey;
        private String tokenEndpoint;
        private String clientIdKey;
        private String clientSecretKey;
        private String usernameKey;
        private String passwordKey;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeoutConfig {
        private int connectMs;
        private int readMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfig {
        private int maxAttempts;
        private int backoffMs;
        private double backoffMultiplier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerConfig {
        private int failureThreshold;
        private int resetTimeoutMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfig {
        private boolean enabled;
        private int ttlSeconds;
        private String[] keyFields;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FallbackConfig {
        private String action;  // SKIP_RULE, DEGRADE_TO_MANUAL, APPLY_DEFAULT
        private String logLevel;
        private Object defaultValue;
    }
}
