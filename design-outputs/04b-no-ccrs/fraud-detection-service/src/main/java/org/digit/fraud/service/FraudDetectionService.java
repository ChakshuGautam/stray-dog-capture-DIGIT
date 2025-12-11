package org.digit.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FraudDetectionService - Core fraud detection engine.
 *
 * This is a generic, reusable service that can evaluate fraud rules
 * for any DIGIT module (SDCRS, TL, PGR, etc.).
 *
 * Loads fraud rules from MDMS (FRAUD-DETECTION/FraudRules.json) and evaluates
 * requests against both INTERNAL and EXTERNAL rules.
 *
 * INTERNAL rules: Threshold-based checks (GPS, timestamp, duplicates, velocity)
 * EXTERNAL rules: AI/ML API calls via ExternalValidatorService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final MDMSService mdmsService;
    private final InternalRuleEvaluator internalRuleEvaluator;
    private final ExternalValidatorService externalValidatorService;

    /**
     * Evaluate a request against all enabled fraud rules.
     *
     * @param request Fraud evaluation request from any module
     * @return FraudEvaluationResponse with all rule results and recommended action
     */
    public FraudEvaluationResponse evaluate(FraudEvaluationRequest request) {
        log.info("Evaluating fraud rules for businessId: {}, module: {}",
                request.getBusinessId(), request.getSourceModule());

        long startTime = System.currentTimeMillis();

        // TODO: Implement
        // 1. Load FraudRules from MDMS via mdmsService.getFraudRules()
        // 2. Filter by enabled=true and ruleCategories if specified
        // 3. Sort by priority
        // 4. Evaluate INTERNAL rules via internalRuleEvaluator
        // 5. Evaluate EXTERNAL rules via externalValidatorService (if !internalOnly)
        // 6. Aggregate results, calculate total score
        // 7. Determine primaryAction based on score thresholds

        FraudEvaluationResponse response = FraudEvaluationResponse.builder()
                .businessId(request.getBusinessId())
                .passed(true)
                .overallRisk("LOW")
                .totalScore(0)
                .primaryAction("APPROVE")
                .evaluationTimeMs(System.currentTimeMillis() - startTime)
                .build();

        return response;
    }

    /**
     * Evaluate only INTERNAL rules (faster, synchronous).
     * Use for real-time validation during submission.
     */
    public FraudEvaluationResponse evaluateInternalOnly(FraudEvaluationRequest request) {
        log.info("Evaluating internal rules only for businessId: {}", request.getBusinessId());

        // TODO: Implement
        // 1. Load FraudRules where ruleType = INTERNAL
        // 2. Evaluate each rule via internalRuleEvaluator

        return FraudEvaluationResponse.builder()
                .businessId(request.getBusinessId())
                .passed(true)
                .overallRisk("LOW")
                .totalScore(0)
                .primaryAction("APPROVE")
                .build();
    }

    /**
     * Evaluate only EXTERNAL rules (AI/ML APIs).
     * Use for async post-submission validation.
     */
    public FraudEvaluationResponse evaluateExternalOnly(FraudEvaluationRequest request) {
        log.info("Evaluating external rules only for businessId: {}", request.getBusinessId());

        // TODO: Implement
        // 1. Load FraudRules where ruleType = EXTERNAL
        // 2. For each rule, call externalValidatorService.validate()

        return FraudEvaluationResponse.builder()
                .businessId(request.getBusinessId())
                .passed(true)
                .overallRisk("LOW")
                .totalScore(0)
                .primaryAction("APPROVE")
                .build();
    }

    /**
     * Get fraud rules from MDMS.
     */
    public List<FraudRule> getFraudRules(RequestInfo requestInfo, String tenantId) {
        return mdmsService.getFraudRules(requestInfo, tenantId);
    }

    /**
     * Get fraud rules filtered by module.
     */
    public List<FraudRule> getFraudRulesForModule(RequestInfo requestInfo, String tenantId, String module) {
        // TODO: Filter rules by module/category
        return mdmsService.getFraudRules(requestInfo, tenantId);
    }
}
