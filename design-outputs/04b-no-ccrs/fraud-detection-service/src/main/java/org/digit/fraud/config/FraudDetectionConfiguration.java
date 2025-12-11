package org.digit.fraud.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * FraudDetectionConfiguration - Configuration properties for fraud detection service.
 *
 * Externalized configuration for:
 * - MDMS integration
 * - External validator endpoints
 * - Timeout and retry settings
 * - Scoring thresholds
 */
@Configuration
@Getter
@Setter
public class FraudDetectionConfiguration {

    // ========== Server Configuration ==========

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.context-path:/fraud}")
    private String contextPath;

    // ========== MDMS Configuration ==========

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint:/egov-mdms-service/v1/_search}")
    private String mdmsSearchEndpoint;

    @Value("${fraud.mdms.module:FRAUD-DETECTION}")
    private String fraudMdmsModule;

    // ========== External Validator Defaults ==========

    @Value("${fraud.external.timeout.connect.ms:5000}")
    private int externalConnectTimeoutMs;

    @Value("${fraud.external.timeout.read.ms:30000}")
    private int externalReadTimeoutMs;

    @Value("${fraud.external.retry.max-attempts:3}")
    private int externalRetryMaxAttempts;

    @Value("${fraud.external.retry.backoff.ms:1000}")
    private int externalRetryBackoffMs;

    // ========== Scoring Thresholds ==========

    @Value("${fraud.score.threshold.approve:20}")
    private int scoreThresholdApprove;

    @Value("${fraud.score.threshold.manual-review:50}")
    private int scoreThresholdManualReview;

    @Value("${fraud.score.threshold.auto-reject:80}")
    private int scoreThresholdAutoReject;

    // ========== Risk Level Thresholds ==========

    @Value("${fraud.risk.threshold.low:25}")
    private int riskThresholdLow;

    @Value("${fraud.risk.threshold.medium:50}")
    private int riskThresholdMedium;

    @Value("${fraud.risk.threshold.high:75}")
    private int riskThresholdHigh;

    // ========== Caching Configuration ==========

    @Value("${fraud.cache.rules.ttl.seconds:300}")
    private int rulesCacheTtlSeconds;

    @Value("${fraud.cache.validators.ttl.seconds:300}")
    private int validatorsCacheTtlSeconds;

    // ========== Kafka Configuration ==========

    @Value("${kafka.topics.fraud.evaluation.result:fraud-evaluation-result}")
    private String fraudEvaluationResultTopic;

    @Value("${kafka.topics.fraud.alert:fraud-alert}")
    private String fraudAlertTopic;

    // ========== Helper Methods ==========

    public String getMdmsUrl() {
        return mdmsHost + mdmsSearchEndpoint;
    }

    public String getRiskLevel(int score) {
        if (score <= riskThresholdLow) return "LOW";
        if (score <= riskThresholdMedium) return "MEDIUM";
        if (score <= riskThresholdHigh) return "HIGH";
        return "CRITICAL";
    }

    public String getPrimaryAction(int score) {
        if (score <= scoreThresholdApprove) return "APPROVE";
        if (score <= scoreThresholdManualReview) return "MANUAL_REVIEW";
        return "AUTO_REJECT";
    }
}
