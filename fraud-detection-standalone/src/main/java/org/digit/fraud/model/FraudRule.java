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
public class FraudRule {

    private String id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String severity;
    private String ruleType;
    private boolean enabled;
    private List<String> applicableModules;
    private Map<String, Object> condition;
    private Map<String, Object> action;
}
