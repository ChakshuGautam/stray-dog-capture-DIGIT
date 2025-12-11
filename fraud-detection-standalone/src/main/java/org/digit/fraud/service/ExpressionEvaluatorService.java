package org.digit.fraud.service;

import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudEvaluationRequest;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Spring Expression Language (SpEL) evaluator for dynamic fraud rule expressions.
 *
 * Supports expressions like:
 * - "additionalData['dogCount'] > 3"
 * - "locationData.latitude > 28.4 && locationData.latitude < 28.9"
 * - "evidences.size() >= 2"
 * - "predictions['dogCount'] == 0"
 * - "applicantInfo.name != null"
 *
 * Variables available in context:
 * - request: The full FraudEvaluationRequest object
 * - applicantInfo: Applicant details (applicantId, name, deviceId, mobileNumber, email)
 * - locationData: Location info (latitude, longitude, locality, ward, district)
 * - evidences: List of evidence objects
 * - additionalData: Map of custom key-value pairs
 * - predictions: Map of ML/AI prediction results (for external validators)
 * - metadata: Map of first evidence metadata
 */
@Slf4j
@Service
public class ExpressionEvaluatorService {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluate a SpEL expression against a FraudEvaluationRequest.
     *
     * @param expression The SpEL expression string
     * @param request The fraud evaluation request containing all data
     * @return true if expression evaluates to true, false otherwise
     */
    public boolean evaluate(String expression, FraudEvaluationRequest request) {
        return evaluate(expression, request, null);
    }

    /**
     * Evaluate a SpEL expression against a FraudEvaluationRequest with additional predictions.
     *
     * @param expression The SpEL expression string
     * @param request The fraud evaluation request containing all data
     * @param predictions Additional predictions from ML/AI services
     * @return true if expression evaluates to true, false otherwise
     */
    public boolean evaluate(String expression, FraudEvaluationRequest request, Map<String, Object> predictions) {
        if (expression == null || expression.isBlank()) {
            log.warn("Empty expression provided, returning false");
            return false;
        }

        try {
            EvaluationContext context = buildContext(request, predictions);
            Expression exp = parser.parseExpression(expression);
            Boolean result = exp.getValue(context, Boolean.class);

            log.debug("Expression '{}' evaluated to: {}", expression, result);
            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Failed to evaluate expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }

    /**
     * Evaluate a SpEL expression and return the raw result (not just boolean).
     * Useful for extracting values or complex calculations.
     *
     * @param expression The SpEL expression string
     * @param request The fraud evaluation request
     * @param predictions Optional predictions map
     * @param resultType The expected result type class
     * @return The evaluation result or null on error
     */
    public <T> T evaluateForValue(String expression, FraudEvaluationRequest request,
                                   Map<String, Object> predictions, Class<T> resultType) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        try {
            EvaluationContext context = buildContext(request, predictions);
            Expression exp = parser.parseExpression(expression);
            return exp.getValue(context, resultType);
        } catch (Exception e) {
            log.error("Failed to evaluate expression '{}' for value: {}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * Build the evaluation context with all available variables.
     */
    private EvaluationContext buildContext(FraudEvaluationRequest request, Map<String, Object> predictions) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Root object is the request itself
        context.setRootObject(request);

        // Set individual variables for easier access
        context.setVariable("request", request);

        if (request.getApplicantInfo() != null) {
            context.setVariable("applicantInfo", request.getApplicantInfo());
            context.setVariable("applicantId", request.getApplicantInfo().getApplicantId());
            context.setVariable("applicantName", request.getApplicantInfo().getName());
            context.setVariable("deviceId", request.getApplicantInfo().getDeviceId());
            context.setVariable("mobileNumber", request.getApplicantInfo().getMobileNumber());
        }

        if (request.getLocationData() != null) {
            context.setVariable("locationData", request.getLocationData());
            context.setVariable("latitude", request.getLocationData().getLatitude());
            context.setVariable("longitude", request.getLocationData().getLongitude());
            context.setVariable("locality", request.getLocationData().getLocality());
        }

        if (request.getEvidences() != null) {
            context.setVariable("evidences", request.getEvidences());
            context.setVariable("evidenceCount", request.getEvidences().size());

            // Extract first evidence metadata for convenience
            if (!request.getEvidences().isEmpty() && request.getEvidences().get(0).getMetadata() != null) {
                context.setVariable("metadata", request.getEvidences().get(0).getMetadata());
            }
        }

        if (request.getAdditionalData() != null) {
            context.setVariable("additionalData", request.getAdditionalData());

            // Also expose individual additional data fields
            for (Map.Entry<String, Object> entry : request.getAdditionalData().entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        // Add predictions from external validators
        if (predictions != null) {
            context.setVariable("predictions", predictions);

            // Also expose individual prediction fields
            for (Map.Entry<String, Object> entry : predictions.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        // Add useful constants
        context.setVariable("now", System.currentTimeMillis());

        return context;
    }

    /**
     * Validate that an expression is syntactically correct without evaluating it.
     *
     * @param expression The SpEL expression to validate
     * @return true if the expression is valid, false otherwise
     */
    public boolean isValidExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        try {
            parser.parseExpression(expression);
            return true;
        } catch (Exception e) {
            log.warn("Invalid expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }
}
