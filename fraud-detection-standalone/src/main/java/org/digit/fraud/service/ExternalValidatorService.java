package org.digit.fraud.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudEvaluationRequest;
import org.digit.fraud.model.FraudEvaluationResponse.RuleResult;
import org.digit.fraud.model.FraudRule;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * External validator service for AI/ML-based fraud detection rules.
 * In standalone mode, this simulates external validations with mock responses.
 * In production, this would call actual ML services for:
 * - Object detection (dog detection in photos)
 * - Face matching (selfie verification)
 * - Image quality analysis
 * - Anomaly detection
 * - GPS spoofing detection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalValidatorService {

    private final MDMSService mdmsService;
    private final ExpressionEvaluatorService expressionEvaluator;
    private final Random random = new Random();

    @CircuitBreaker(name = "externalValidator", fallbackMethod = "fallbackValidation")
    @Retry(name = "externalValidator")
    public RuleResult validate(FraudRule rule, FraudEvaluationRequest request) {
        Map<String, Object> condition = rule.getCondition();
        String validatorId = (String) condition.get("validatorId");

        log.debug("Running external validation: {} for rule {}", validatorId, rule.getCode());

        return switch (validatorId) {
            case "OBJECT_DETECTOR" -> simulateObjectDetection(rule, request, condition);
            case "FACE_MATCHER" -> simulateFaceMatching(rule, request, condition);
            case "IMAGE_QUALITY_ANALYZER" -> simulateImageQualityAnalysis(rule, request, condition);
            case "ANOMALY_DETECTOR" -> simulateAnomalyDetection(rule, request, condition);
            case "GPS_SPOOFING_DETECTOR" -> simulateGpsSpoofingDetection(rule, request, condition);
            default -> createNotTriggeredResult(rule, "Unknown validator: " + validatorId);
        };
    }

    private RuleResult simulateObjectDetection(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulate object detection with high accuracy
        // 95% chance of detecting a dog (assuming valid submissions)
        boolean dogDetected = random.nextDouble() < 0.95;
        int dogCount = dogDetected ? 1 + random.nextInt(3) : 0;
        double confidence = 0.85 + (random.nextDouble() * 0.14);

        Map<String, Object> predictions = new HashMap<>();
        predictions.put("dogCount", dogCount);
        predictions.put("catCount", 0);
        predictions.put("confidence", confidence);

        String checkExpression = (String) condition.get("checkExpression");
        if (checkExpression != null && !checkExpression.isBlank()) {
            // Evaluate using SpEL - expression can reference predictions map
            // Example: "dogCount == 0" or "confidence < 0.5"
            boolean triggered = expressionEvaluator.evaluate(checkExpression, request, predictions);
            if (triggered) {
                return createTriggeredResult(rule,
                        String.format("Object detection check failed: %s", checkExpression),
                        Map.of("predictions", predictions, "confidence", confidence, "expression", checkExpression));
            }
        }

        return createNotTriggeredResult(rule,
                String.format("Dog detected (count: %d, confidence: %.2f)", dogCount, confidence));
    }

    private RuleResult simulateFaceMatching(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulate face detection and matching
        // 90% chance of face matching profile
        boolean faceDetected = random.nextDouble() < 0.98;
        int facesDetected = faceDetected ? 1 : 0;
        boolean match = faceDetected && random.nextDouble() < 0.90;
        double similarity = match ? 0.85 + (random.nextDouble() * 0.14) : 0.3 + (random.nextDouble() * 0.3);

        Map<String, Object> predictions = new HashMap<>();
        predictions.put("facesDetected", facesDetected);
        predictions.put("match", match);
        predictions.put("similarity", similarity);

        String checkExpression = (String) condition.get("checkExpression");
        if (checkExpression != null && !checkExpression.isBlank()) {
            // Evaluate using SpEL - expression can reference predictions map
            // Example: "facesDetected == 0 || (match == false && similarity < 0.7)"
            boolean triggered = expressionEvaluator.evaluate(checkExpression, request, predictions);
            if (triggered) {
                String message = facesDetected == 0 ? "No face detected in selfie" :
                        !match ? "Face does not match profile photo" : "Face matching check failed";
                return createTriggeredResult(rule, message,
                        Map.of("predictions", predictions, "similarity", similarity, "expression", checkExpression));
            }
        }

        return createNotTriggeredResult(rule,
                String.format("Face verification passed (similarity: %.2f)", similarity));
    }

    private RuleResult simulateImageQualityAnalysis(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulate image quality analysis
        double blurScore = 0.5 + (random.nextDouble() * 0.4);
        double exposureScore = 0.5 + (random.nextDouble() * 0.4);
        double overallQuality = (blurScore + exposureScore) / 2;
        boolean isAcceptable = overallQuality >= 0.4;

        Map<String, Object> predictions = new HashMap<>();
        predictions.put("blurScore", blurScore);
        predictions.put("exposureScore", exposureScore);
        predictions.put("overallQuality", overallQuality);
        predictions.put("isAcceptable", isAcceptable);

        String checkExpression = (String) condition.get("checkExpression");
        if (checkExpression != null && !checkExpression.isBlank()) {
            // Evaluate using SpEL - expression can reference predictions map
            // Example: "blurScore < 0.2 || overallQuality < 0.4"
            boolean triggered = expressionEvaluator.evaluate(checkExpression, request, predictions);
            if (triggered) {
                String message = blurScore < 0.2 ? "Image too blurry" :
                        overallQuality < 0.4 ? "Poor image quality" : "Image quality check failed";
                return createTriggeredResult(rule, message,
                        Map.of("predictions", predictions, "expression", checkExpression));
            }
        }

        return createNotTriggeredResult(rule,
                String.format("Image quality acceptable (score: %.2f)", overallQuality));
    }

    private RuleResult simulateAnomalyDetection(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulate anomaly detection - 5% chance of flagging anomaly
        boolean isAnomaly = random.nextDouble() < 0.05;
        double anomalyScore = isAnomaly ? 0.7 + (random.nextDouble() * 0.3) : random.nextDouble() * 0.5;

        Map<String, Object> predictions = new HashMap<>();
        predictions.put("isAnomaly", isAnomaly);
        predictions.put("anomalyScore", anomalyScore);

        String checkExpression = (String) condition.get("checkExpression");
        if (checkExpression != null && !checkExpression.isBlank()) {
            // Evaluate using SpEL - expression can reference predictions map
            // Example: "isAnomaly == true && anomalyScore > 0.85"
            boolean triggered = expressionEvaluator.evaluate(checkExpression, request, predictions);
            if (triggered) {
                return createTriggeredResult(rule, "Anomalous submission pattern detected",
                        Map.of("predictions", predictions, "anomalyScore", anomalyScore, "expression", checkExpression));
            }
        }

        return createNotTriggeredResult(rule,
                String.format("No anomaly detected (score: %.2f)", anomalyScore));
    }

    private RuleResult simulateGpsSpoofingDetection(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulate GPS spoofing detection - 2% chance of detecting spoofing
        boolean isSpoofed = random.nextDouble() < 0.02;
        double spoofingConfidence = isSpoofed ? 0.7 + (random.nextDouble() * 0.29) : random.nextDouble() * 0.3;

        Map<String, Object> predictions = new HashMap<>();
        predictions.put("isSpoofed", isSpoofed);
        predictions.put("spoofingConfidence", spoofingConfidence);

        String checkExpression = (String) condition.get("checkExpression");
        if (checkExpression != null && !checkExpression.isBlank()) {
            // Evaluate using SpEL - expression can reference predictions map
            // Example: "isSpoofed == true && spoofingConfidence > 0.6"
            boolean triggered = expressionEvaluator.evaluate(checkExpression, request, predictions);
            if (triggered) {
                String message = spoofingConfidence > 0.9 ? "GPS spoofing detected with high confidence" :
                        "Suspicious GPS pattern detected";
                return createTriggeredResult(rule, message,
                        Map.of("predictions", predictions, "expression", checkExpression));
            }
        }

        return createNotTriggeredResult(rule,
                String.format("GPS validation passed (spoofing score: %.2f)", spoofingConfidence));
    }

    private RuleResult fallbackValidation(FraudRule rule, FraudEvaluationRequest request, Throwable t) {
        log.warn("External validation failed for rule {}, using fallback: {}",
                rule.getCode(), t.getMessage());

        return RuleResult.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .triggered(false)
                .score(0)
                .message("External validation unavailable - skipped")
                .details(Map.of("fallback", true, "error", t.getMessage()))
                .build();
    }

    private RuleResult createTriggeredResult(FraudRule rule, String message, Map<String, Object> details) {
        Integer weight = mdmsService.getCategoryWeight(rule.getCategory());
        Integer score = calculateRuleScore(rule, weight);

        return RuleResult.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .triggered(true)
                .score(score)
                .message(message)
                .details(details)
                .build();
    }

    private RuleResult createNotTriggeredResult(FraudRule rule, String message) {
        return RuleResult.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .triggered(false)
                .score(0)
                .message(message)
                .details(Map.of())
                .build();
    }

    private Integer calculateRuleScore(FraudRule rule, Integer categoryWeight) {
        int severityMultiplier = switch (rule.getSeverity()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 1;
        };
        return (categoryWeight * severityMultiplier) / 4;
    }
}
