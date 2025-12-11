package org.digit.fraud.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.*;
import org.digit.fraud.service.FraudDetectionService;
import org.digit.fraud.service.MDMSService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;
    private final MDMSService mdmsService;

    /**
     * Full fraud evaluation - runs both internal and external rules
     */
    @PostMapping("/_evaluate")
    public ResponseEntity<ResponseWrapper<FraudEvaluationResponse>> evaluate(
            @RequestBody RequestWrapper<FraudEvaluationRequest> requestWrapper) {

        log.info("Received fraud evaluation request for application: {}",
                requestWrapper.getRequest().getApplicationId());

        FraudEvaluationResponse response = fraudDetectionService.evaluate(requestWrapper.getRequest());

        String msgId = requestWrapper.getRequestInfo() != null ?
                requestWrapper.getRequestInfo().getMsgId() : UUID.randomUUID().toString();

        return ResponseEntity.ok(ResponseWrapper.success(response, msgId));
    }

    /**
     * Internal rules only - synchronous, fast evaluation
     */
    @PostMapping("/_evaluateSync")
    public ResponseEntity<ResponseWrapper<FraudEvaluationResponse>> evaluateSync(
            @RequestBody RequestWrapper<FraudEvaluationRequest> requestWrapper) {

        log.info("Received sync evaluation request for application: {}",
                requestWrapper.getRequest().getApplicationId());

        FraudEvaluationResponse response = fraudDetectionService.evaluateInternalOnly(requestWrapper.getRequest());

        String msgId = requestWrapper.getRequestInfo() != null ?
                requestWrapper.getRequestInfo().getMsgId() : UUID.randomUUID().toString();

        return ResponseEntity.ok(ResponseWrapper.success(response, msgId));
    }

    /**
     * External rules only - for async/ML-based evaluation
     */
    @PostMapping("/_evaluateAsync")
    public ResponseEntity<ResponseWrapper<FraudEvaluationResponse>> evaluateAsync(
            @RequestBody RequestWrapper<FraudEvaluationRequest> requestWrapper) {

        log.info("Received async evaluation request for application: {}",
                requestWrapper.getRequest().getApplicationId());

        FraudEvaluationResponse response = fraudDetectionService.evaluateExternalOnly(requestWrapper.getRequest());

        String msgId = requestWrapper.getRequestInfo() != null ?
                requestWrapper.getRequestInfo().getMsgId() : UUID.randomUUID().toString();

        return ResponseEntity.ok(ResponseWrapper.success(response, msgId));
    }

    /**
     * Search/list available fraud rules
     */
    @PostMapping("/rules/_search")
    public ResponseEntity<ResponseWrapper<List<FraudRule>>> searchRules(
            @RequestBody(required = false) Map<String, Object> searchCriteria) {

        String moduleCode = searchCriteria != null ?
                (String) searchCriteria.getOrDefault("moduleCode", "SDCRS") : "SDCRS";
        String ruleType = searchCriteria != null ?
                (String) searchCriteria.get("ruleType") : null;
        String category = searchCriteria != null ?
                (String) searchCriteria.get("category") : null;
        Boolean enabled = searchCriteria != null ?
                (Boolean) searchCriteria.get("enabled") : null;

        log.info("Searching rules: module={}, type={}, category={}, enabled={}",
                moduleCode, ruleType, category, enabled);

        List<FraudRule> rules = mdmsService.getRulesForModule(moduleCode);

        // Apply filters
        if (ruleType != null) {
            rules = rules.stream()
                    .filter(r -> ruleType.equals(r.getRuleType()))
                    .collect(Collectors.toList());
        }
        if (category != null) {
            rules = rules.stream()
                    .filter(r -> category.equals(r.getCategory()))
                    .collect(Collectors.toList());
        }
        if (enabled != null) {
            rules = rules.stream()
                    .filter(r -> r.isEnabled() == enabled)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ResponseWrapper.success(rules, UUID.randomUUID().toString()));
    }

    /**
     * Get risk score configuration
     */
    @GetMapping("/config/risk-score")
    public ResponseEntity<ResponseWrapper<RiskScoreConfig>> getRiskScoreConfig() {
        log.info("Fetching risk score configuration");
        return ResponseEntity.ok(ResponseWrapper.success(
                mdmsService.getRiskScoreConfig(),
                UUID.randomUUID().toString()));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        int ruleCount = mdmsService.getAllRules().size();
        int categoryCount = mdmsService.getRiskScoreConfig().getWeights().size();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "fraud-detection-standalone",
                "version", "1.0.0",
                "rulesLoaded", ruleCount,
                "categoriesConfigured", categoryCount,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
