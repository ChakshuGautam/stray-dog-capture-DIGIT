package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * FraudEvaluationResponse - Result of fraud detection evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvaluationResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    private String businessId;
    private boolean passed;
    private String overallRisk;  // LOW, MEDIUM, HIGH, CRITICAL
    private int totalScore;

    @Builder.Default
    private List<RuleResult> ruleResults = new ArrayList<>();

    @Builder.Default
    private List<String> recommendedActions = new ArrayList<>();

    /**
     * Primary recommended action: APPROVE, MANUAL_REVIEW, AUTO_REJECT, ESCALATE
     */
    private String primaryAction;

    private Long evaluationTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleResult {
        private String ruleId;
        private String ruleName;
        private String ruleType;  // INTERNAL, EXTERNAL
        private boolean triggered;
        private String severity;
        private int score;
        private String message;
        private Object details;
        private Long executionTimeMs;
    }

    public void addRuleResult(RuleResult result) {
        if (ruleResults == null) {
            ruleResults = new ArrayList<>();
        }
        ruleResults.add(result);
    }
}
