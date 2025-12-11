package org.digit.fraud.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudRule;
import org.digit.fraud.model.RiskScoreConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MDMSService {

    private final ObjectMapper objectMapper;

    private List<FraudRule> fraudRules;
    private RiskScoreConfig riskScoreConfig;

    @PostConstruct
    public void init() {
        loadFraudRules();
        loadRiskScoreConfig();
        log.info("MDMS data loaded successfully. Rules: {}, Categories: {}",
                fraudRules.size(),
                riskScoreConfig.getWeights().size());
    }

    private void loadFraudRules() {
        try {
            ClassPathResource resource = new ClassPathResource("mdms/FRAUD-DETECTION/FraudRules.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode rulesNode = root.get("FraudRules");

            fraudRules = objectMapper.convertValue(rulesNode, new TypeReference<List<FraudRule>>() {});
            log.info("Loaded {} fraud rules from embedded MDMS", fraudRules.size());
        } catch (IOException e) {
            log.error("Failed to load FraudRules.json", e);
            fraudRules = Collections.emptyList();
        }
    }

    private void loadRiskScoreConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("mdms/FRAUD-DETECTION/RiskScoreConfig.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode configNode = root.get("RiskScoreConfig");

            riskScoreConfig = objectMapper.convertValue(configNode, RiskScoreConfig.class);
            log.info("Loaded risk score config with {} category weights", riskScoreConfig.getWeights().size());
        } catch (IOException e) {
            log.error("Failed to load RiskScoreConfig.json", e);
            riskScoreConfig = new RiskScoreConfig();
        }
    }

    @Cacheable(value = "fraudRules", key = "'all'")
    public List<FraudRule> getAllRules() {
        return fraudRules;
    }

    @Cacheable(value = "fraudRules", key = "#moduleCode")
    public List<FraudRule> getRulesForModule(String moduleCode) {
        return fraudRules.stream()
                .filter(rule -> rule.isEnabled())
                .filter(rule -> rule.getApplicableModules() == null ||
                        rule.getApplicableModules().isEmpty() ||
                        rule.getApplicableModules().contains(moduleCode))
                .collect(Collectors.toList());
    }

    public List<FraudRule> getInternalRules(String moduleCode) {
        return getRulesForModule(moduleCode).stream()
                .filter(rule -> "INTERNAL".equals(rule.getRuleType()))
                .collect(Collectors.toList());
    }

    public List<FraudRule> getExternalRules(String moduleCode) {
        return getRulesForModule(moduleCode).stream()
                .filter(rule -> "EXTERNAL".equals(rule.getRuleType()))
                .collect(Collectors.toList());
    }

    public RiskScoreConfig getRiskScoreConfig() {
        return riskScoreConfig;
    }

    public Integer getCategoryWeight(String category) {
        return riskScoreConfig.getWeights().getOrDefault(category, 10);
    }

    public String getCategoryDescription(String category) {
        return riskScoreConfig.getCategoryDescriptions().getOrDefault(category, category);
    }
}
