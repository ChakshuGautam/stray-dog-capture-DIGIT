package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskScoreConfig {

    private Map<String, Integer> weights;
    private Map<String, String> categoryDescriptions;
    private Map<String, ThresholdConfig> thresholds;
    private Integer autoRejectThreshold;
    private Integer escalationThreshold;
    private Integer manualReviewThreshold;
    private ScoringDecay scoringDecay;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThresholdConfig {
        private Integer min;
        private Integer max;
        private String color;
        private String action;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoringDecay {
        private boolean enabled;
        private Integer halfLifeDays;
    }
}
