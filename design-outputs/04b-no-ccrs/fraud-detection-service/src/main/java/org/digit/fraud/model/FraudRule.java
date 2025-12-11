package org.digit.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * FraudRule - Model representing a fraud detection rule from MDMS.
 * Loaded from FRAUD-DETECTION/FraudRules.json
 *
 * This is a generic fraud rule that can be applied to any DIGIT module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {

    private String id;
    private String name;
    private String description;
    private String category;

    /**
     * INTERNAL - threshold/pattern based rules evaluated locally
     * EXTERNAL - rules requiring external AI/ML API calls
     */
    private RuleType ruleType;

    private String checkType;
    private String severity;  // LOW, MEDIUM, HIGH, CRITICAL
    private boolean enabled;
    private int priority;

    // For INTERNAL rules
    private Map<String, Object> threshold;
    private Map<String, Object> parameters;

    // For EXTERNAL rules
    private String validatorId;

    // Actions to take when rule triggers
    private Map<String, Object> actions;

    public enum RuleType {
        INTERNAL,
        EXTERNAL
    }
}
