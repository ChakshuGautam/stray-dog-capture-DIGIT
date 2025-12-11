package org.digit.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudEvaluationRequest;
import org.digit.fraud.model.FraudEvaluationResponse;
import org.digit.fraud.model.FraudEvaluationResponse.RuleResult;
import org.digit.fraud.model.FraudRule;
import org.digit.fraud.model.RiskScoreConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final MDMSService mdmsService;
    private final InternalRuleEvaluator internalRuleEvaluator;
    private final ExternalValidatorService externalValidatorService;

    @Value("${fraud.score.threshold.approve:20}")
    private int approveThreshold;

    @Value("${fraud.score.threshold.manual-review:50}")
    private int manualReviewThreshold;

    @Value("${fraud.score.threshold.auto-reject:80}")
    private int autoRejectThreshold;

    @Value("${fraud.risk.threshold.low:25}")
    private int lowRiskThreshold;

    @Value("${fraud.risk.threshold.medium:50}")
    private int mediumRiskThreshold;

    @Value("${fraud.risk.threshold.high:75}")
    private int highRiskThreshold;

    /**
     * Full evaluation - runs both internal and external rules
     */
    public FraudEvaluationResponse evaluate(FraudEvaluationRequest request) {
        log.info("Starting full fraud evaluation for application: {}", request.getApplicationId());

        String moduleCode = request.getModuleCode() != null ? request.getModuleCode() : "SDCRS";
        List<FraudRule> allRules = mdmsService.getRulesForModule(moduleCode);

        log.debug("Found {} enabled rules for module {}", allRules.size(), moduleCode);

        List<RuleResult> results = new ArrayList<>();

        // Evaluate internal rules
        List<FraudRule> internalRules = allRules.stream()
                .filter(r -> "INTERNAL".equals(r.getRuleType()))
                .collect(Collectors.toList());

        for (FraudRule rule : internalRules) {
            try {
                RuleResult result = internalRuleEvaluator.evaluate(rule, request);
                results.add(result);
                if (result.isTriggered()) {
                    log.info("Rule triggered: {} - {}", rule.getCode(), result.getMessage());
                }
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getCode(), e.getMessage());
            }
        }

        // Evaluate external rules
        List<FraudRule> externalRules = allRules.stream()
                .filter(r -> "EXTERNAL".equals(r.getRuleType()))
                .collect(Collectors.toList());

        for (FraudRule rule : externalRules) {
            try {
                RuleResult result = externalValidatorService.validate(rule, request);
                results.add(result);
                if (result.isTriggered()) {
                    log.info("External rule triggered: {} - {}", rule.getCode(), result.getMessage());
                }
            } catch (Exception e) {
                log.error("Error evaluating external rule {}: {}", rule.getCode(), e.getMessage());
            }
        }

        return buildResponse(request, results, "FULL");
    }

    /**
     * Internal rules only - synchronous, fast evaluation
     */
    public FraudEvaluationResponse evaluateInternalOnly(FraudEvaluationRequest request) {
        log.info("Starting internal-only fraud evaluation for application: {}", request.getApplicationId());

        String moduleCode = request.getModuleCode() != null ? request.getModuleCode() : "SDCRS";
        List<FraudRule> internalRules = mdmsService.getInternalRules(moduleCode);

        log.debug("Found {} internal rules for module {}", internalRules.size(), moduleCode);

        List<RuleResult> results = new ArrayList<>();

        for (FraudRule rule : internalRules) {
            try {
                RuleResult result = internalRuleEvaluator.evaluate(rule, request);
                results.add(result);
                if (result.isTriggered()) {
                    log.info("Rule triggered: {} - {}", rule.getCode(), result.getMessage());
                }
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getCode(), e.getMessage());
            }
        }

        return buildResponse(request, results, "INTERNAL");
    }

    /**
     * External rules only - for async/ML-based evaluation
     */
    public FraudEvaluationResponse evaluateExternalOnly(FraudEvaluationRequest request) {
        log.info("Starting external-only fraud evaluation for application: {}", request.getApplicationId());

        String moduleCode = request.getModuleCode() != null ? request.getModuleCode() : "SDCRS";
        List<FraudRule> externalRules = mdmsService.getExternalRules(moduleCode);

        log.debug("Found {} external rules for module {}", externalRules.size(), moduleCode);

        List<RuleResult> results = new ArrayList<>();

        for (FraudRule rule : externalRules) {
            try {
                RuleResult result = externalValidatorService.validate(rule, request);
                results.add(result);
                if (result.isTriggered()) {
                    log.info("External rule triggered: {} - {}", rule.getCode(), result.getMessage());
                }
            } catch (Exception e) {
                log.error("Error evaluating external rule {}: {}", rule.getCode(), e.getMessage());
            }
        }

        return buildResponse(request, results, "EXTERNAL");
    }

    private FraudEvaluationResponse buildResponse(FraudEvaluationRequest request,
                                                   List<RuleResult> results,
                                                   String evaluationType) {
        // Calculate total score
        int totalScore = results.stream()
                .filter(RuleResult::isTriggered)
                .mapToInt(RuleResult::getScore)
                .sum();

        // Cap at 100
        totalScore = Math.min(totalScore, 100);

        // Calculate category scores
        Map<String, Integer> categoryScores = results.stream()
                .filter(RuleResult::isTriggered)
                .collect(Collectors.groupingBy(
                        RuleResult::getCategory,
                        Collectors.summingInt(RuleResult::getScore)
                ));

        // Determine risk level
        String riskLevel = determineRiskLevel(totalScore);

        // Determine recommendation
        String recommendation = determineRecommendation(totalScore, results);

        // Generate evaluation ID
        String evaluationId = UUID.randomUUID().toString();

        log.info("Fraud evaluation complete for {}: score={}, risk={}, recommendation={}",
                request.getApplicationId(), totalScore, riskLevel, recommendation);

        return FraudEvaluationResponse.builder()
                .evaluationId(evaluationId)
                .applicationId(request.getApplicationId())
                .totalScore(totalScore)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .ruleResults(results)
                .categoryScores(categoryScores)
                .evaluatedAt(System.currentTimeMillis())
                .evaluationType(evaluationType)
                .build();
    }

    private String determineRiskLevel(int score) {
        if (score <= lowRiskThreshold) return "LOW";
        if (score <= mediumRiskThreshold) return "MEDIUM";
        if (score <= highRiskThreshold) return "HIGH";
        return "CRITICAL";
    }

    private String determineRecommendation(int score, List<RuleResult> results) {
        // Check for auto-reject rules
        boolean hasAutoReject = results.stream()
                .anyMatch(r -> r.isTriggered() &&
                        ("CRITICAL".equals(r.getSeverity()) ||
                                r.getMessage().contains("AUTO_REJECT")));

        if (hasAutoReject || score >= autoRejectThreshold) {
            return "AUTO_REJECT";
        }

        if (score <= approveThreshold) {
            return "APPROVE";
        }

        if (score <= manualReviewThreshold) {
            return "MANUAL_REVIEW";
        }

        return "MANUAL_REVIEW";
    }
}
