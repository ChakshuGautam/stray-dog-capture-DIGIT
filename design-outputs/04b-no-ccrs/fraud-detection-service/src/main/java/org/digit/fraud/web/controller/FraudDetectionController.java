package org.digit.fraud.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudEvaluationRequest;
import org.digit.fraud.model.FraudEvaluationResponse;
import org.digit.fraud.model.FraudRule;
import org.digit.fraud.service.FraudDetectionService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * FraudDetectionController - REST API for generic fraud detection service.
 *
 * This service can be used by any DIGIT module (SDCRS, TL, PGR, etc.)
 * to validate submissions against configurable fraud rules.
 *
 * Endpoints:
 * - POST /fraud/v1/_evaluate       - Full evaluation (INTERNAL + EXTERNAL rules)
 * - POST /fraud/v1/_evaluateSync   - INTERNAL rules only (synchronous, fast)
 * - POST /fraud/v1/_evaluateAsync  - EXTERNAL rules only (AI/ML validation)
 * - POST /fraud/v1/rules/_search   - Get fraud rules from MDMS
 */
@Slf4j
@RestController
@RequestMapping("/fraud/v1")
@RequiredArgsConstructor
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    /**
     * Evaluate request against all enabled fraud rules.
     * Runs both INTERNAL (threshold) and EXTERNAL (AI/ML) rules.
     *
     * @param request FraudEvaluationRequest with business data
     * @return FraudEvaluationResponse with all rule results
     */
    @PostMapping("/_evaluate")
    public ResponseEntity<FraudEvaluationResponseWrapper> evaluate(
            @Valid @RequestBody FraudEvaluationRequestWrapper request) {

        log.info("Received fraud evaluation request for businessId: {}",
                request.getEvaluationRequest().getBusinessId());

        FraudEvaluationResponse response = fraudDetectionService.evaluate(
                request.getEvaluationRequest());

        return ResponseEntity.ok(FraudEvaluationResponseWrapper.builder()
                .responseInfo(buildResponseInfo(request.getRequestInfo()))
                .evaluationResponse(response)
                .build());
    }

    /**
     * Evaluate request against INTERNAL rules only.
     * Use for real-time validation during form submission.
     * Faster as it doesn't call external APIs.
     *
     * @param request FraudEvaluationRequest with business data
     * @return FraudEvaluationResponse with internal rule results only
     */
    @PostMapping("/_evaluateSync")
    public ResponseEntity<FraudEvaluationResponseWrapper> evaluateSync(
            @Valid @RequestBody FraudEvaluationRequestWrapper request) {

        log.info("Received sync fraud evaluation request for businessId: {}",
                request.getEvaluationRequest().getBusinessId());

        FraudEvaluationResponse response = fraudDetectionService.evaluateInternalOnly(
                request.getEvaluationRequest());

        return ResponseEntity.ok(FraudEvaluationResponseWrapper.builder()
                .responseInfo(buildResponseInfo(request.getRequestInfo()))
                .evaluationResponse(response)
                .build());
    }

    /**
     * Evaluate request against EXTERNAL rules only.
     * Use for async post-submission AI/ML validation.
     * May take longer due to external API calls.
     *
     * @param request FraudEvaluationRequest with business data
     * @return FraudEvaluationResponse with external rule results only
     */
    @PostMapping("/_evaluateAsync")
    public ResponseEntity<FraudEvaluationResponseWrapper> evaluateAsync(
            @Valid @RequestBody FraudEvaluationRequestWrapper request) {

        log.info("Received async fraud evaluation request for businessId: {}",
                request.getEvaluationRequest().getBusinessId());

        FraudEvaluationResponse response = fraudDetectionService.evaluateExternalOnly(
                request.getEvaluationRequest());

        return ResponseEntity.ok(FraudEvaluationResponseWrapper.builder()
                .responseInfo(buildResponseInfo(request.getRequestInfo()))
                .evaluationResponse(response)
                .build());
    }

    /**
     * Search fraud rules from MDMS.
     *
     * @param request Search criteria with tenantId and optional filters
     * @return List of FraudRule objects
     */
    @PostMapping("/rules/_search")
    public ResponseEntity<FraudRulesResponseWrapper> searchRules(
            @Valid @RequestBody FraudRulesSearchRequest request) {

        log.info("Searching fraud rules for tenant: {}", request.getTenantId());

        List<FraudRule> rules = fraudDetectionService.getFraudRules(
                request.getRequestInfo(), request.getTenantId());

        return ResponseEntity.ok(FraudRulesResponseWrapper.builder()
                .responseInfo(buildResponseInfo(request.getRequestInfo()))
                .fraudRules(rules)
                .build());
    }

    // ========== Request/Response Wrappers ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudEvaluationRequestWrapper {
        private RequestInfo requestInfo;
        private FraudEvaluationRequest evaluationRequest;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudEvaluationResponseWrapper {
        private ResponseInfo responseInfo;
        private FraudEvaluationResponse evaluationResponse;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudRulesSearchRequest {
        private RequestInfo requestInfo;
        private String tenantId;
        private String module;           // Optional: filter by module (SDCRS, TL, etc.)
        private String category;         // Optional: filter by category
        private FraudRule.RuleType ruleType;  // Optional: INTERNAL or EXTERNAL
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudRulesResponseWrapper {
        private ResponseInfo responseInfo;
        private List<FraudRule> fraudRules;
    }

    // ========== Helper Methods ==========

    private ResponseInfo buildResponseInfo(RequestInfo requestInfo) {
        return ResponseInfo.builder()
                .apiId(requestInfo.getApiId())
                .ver(requestInfo.getVer())
                .ts(requestInfo.getTs())
                .resMsgId("uief87324")
                .msgId(requestInfo.getMsgId())
                .status("successful")
                .build();
    }
}
