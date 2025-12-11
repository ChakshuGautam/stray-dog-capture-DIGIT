package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudEvaluationResponse {

    private String evaluationId;
    private String applicationId;
    private Integer totalScore;
    private String riskLevel;
    private String recommendation;
    private List<RuleResult> ruleResults;
    private Map<String, Integer> categoryScores;
    private Long evaluatedAt;
    private String evaluationType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuleResult {
        private String ruleId;
        private String ruleCode;
        private String ruleName;
        private String category;
        private String severity;
        private boolean triggered;
        private Integer score;
        private String message;
        private Map<String, Object> details;
    }
}
