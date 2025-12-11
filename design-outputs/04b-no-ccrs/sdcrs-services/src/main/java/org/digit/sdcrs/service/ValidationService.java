package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.kafka.DogReportProducer;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportRequest;
import org.digit.sdcrs.model.Evidence;
import org.digit.sdcrs.model.fraud.FraudEvaluationResult;
import org.digit.sdcrs.repository.DogReportRepository;
import org.egov.common.contract.models.Workflow;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ValidationService - Handles automated validation of dog reports.
 * Runs asynchronously after report creation.
 *
 * NOTE: This service contains LEGACY hardcoded validation logic.
 * Future implementation should delegate to FraudDetectionService
 * which loads rules from MDMS (FRAUD-DETECTION/FraudRules.json).
 *
 * Validation checks (legacy - to be replaced):
 * 1. GPS consistency (EXIF vs submitted)
 * 2. Image freshness (< 48 hours)
 * 3. Duplicate detection (image hash)
 * 4. Tenant boundary check
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final DogReportRepository repository;
    private final DogReportProducer producer;
    private final WorkflowService workflowService;
    private final FraudDetectionService fraudDetectionService;

    @Value("${sdcrs.kafka.update.topic}")
    private String updateTopic;

    @Value("${sdcrs.validation.gps.tolerance.meters:100}")
    private double gpsToleranceMeters;

    @Value("${sdcrs.validation.duplicate.threshold:0.90}")
    private double duplicateThreshold;

    /**
     * Run automated validation on a report.
     * Called asynchronously after report creation.
     *
     * @deprecated Use {@link #runMdmsDrivenValidation} instead.
     *             This method uses hardcoded validation logic.
     */
    @Deprecated
    public void runAutoValidation(RequestInfo requestInfo, DogReport report) {
        log.info("Running auto-validation for report: {}", report.getReportNumber());

        List<String> validationErrors = new ArrayList<>();

        // Run validation checks (LEGACY - hardcoded logic)
        validateGpsConsistency(report, validationErrors);
        validateImageFreshness(report, validationErrors);
        validateDuplicates(report, validationErrors);
        validateTenantBoundary(report, validationErrors);

        // Determine validation result
        if (validationErrors.isEmpty()) {
            // Validation passed - transition to PENDING_VERIFICATION
            transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_PASS, null);
            log.info("Report {} passed auto-validation", report.getReportNumber());
        } else {
            // Validation failed - auto-reject
            String reason = String.join("; ", validationErrors);
            transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_FAIL, reason);
            log.info("Report {} failed auto-validation: {}", report.getReportNumber(), reason);
        }
    }

    /**
     * Run MDMS-driven fraud detection validation.
     * Loads rules from FRAUD-DETECTION/FraudRules.json and evaluates report.
     *
     * This is the preferred validation method - replaces hardcoded logic.
     *
     * @param requestInfo Request context
     * @param report Dog report to validate
     */
    public void runMdmsDrivenValidation(RequestInfo requestInfo, DogReport report) {
        log.info("Running MDMS-driven fraud detection for report: {}", report.getReportNumber());

        // TODO: Replace runAutoValidation() calls with this method
        // 1. Evaluate INTERNAL rules (synchronous)
        FraudEvaluationResult internalResult = fraudDetectionService.evaluateInternalRules(requestInfo, report);

        // 2. Evaluate EXTERNAL rules (may be async for AI/ML calls)
        FraudEvaluationResult externalResult = fraudDetectionService.evaluateExternalRules(requestInfo, report);

        // 3. Combine results and determine action
        String recommendedAction = determineAction(internalResult, externalResult);

        // 4. Transition workflow based on result
        switch (recommendedAction) {
            case "APPROVE":
                transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_PASS, null);
                break;
            case "MANUAL_REVIEW":
                // Flag for manual review but don't auto-reject
                transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_PASS,
                        "Flagged for manual review");
                break;
            case "AUTO_REJECT":
                String reason = buildRejectionReason(internalResult, externalResult);
                transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_FAIL, reason);
                break;
            default:
                log.warn("Unknown recommended action: {}", recommendedAction);
                transitionReport(requestInfo, report, WorkflowService.ACTION_AUTO_VALIDATE_PASS, null);
        }
    }

    /**
     * Determine action based on fraud evaluation results.
     */
    private String determineAction(FraudEvaluationResult internal, FraudEvaluationResult external) {
        // TODO: Implement logic based on rule severity and scores
        if (!internal.isPassed() || !external.isPassed()) {
            return "AUTO_REJECT";
        }
        if ("HIGH".equals(internal.getOverallRisk()) || "HIGH".equals(external.getOverallRisk())) {
            return "MANUAL_REVIEW";
        }
        return "APPROVE";
    }

    /**
     * Build rejection reason from evaluation results.
     */
    private String buildRejectionReason(FraudEvaluationResult internal, FraudEvaluationResult external) {
        List<String> reasons = new ArrayList<>();

        if (internal.getRuleResults() != null) {
            internal.getRuleResults().stream()
                    .filter(FraudEvaluationResult.RuleResult::isTriggered)
                    .forEach(r -> reasons.add(r.getMessage()));
        }

        if (external.getRuleResults() != null) {
            external.getRuleResults().stream()
                    .filter(FraudEvaluationResult.RuleResult::isTriggered)
                    .forEach(r -> reasons.add(r.getMessage()));
        }

        return reasons.isEmpty() ? "Fraud detection failed" : String.join("; ", reasons);
    }

    /**
     * Validate GPS consistency between EXIF and submitted location.
     */
    private void validateGpsConsistency(DogReport report, List<String> errors) {
        if (report.getEvidence() == null) return;

        for (Evidence evidence : report.getEvidence()) {
            if (evidence.getEvidenceType() == Evidence.EvidenceType.DOG_PHOTO) {
                if (evidence.getLatitude() != null && evidence.getLongitude() != null) {
                    double distance = calculateDistance(
                            report.getLocation().getLatitude().doubleValue(),
                            report.getLocation().getLongitude().doubleValue(),
                            evidence.getLatitude(),
                            evidence.getLongitude()
                    );

                    if (distance > gpsToleranceMeters) {
                        errors.add("GPS_MISMATCH: Photo GPS differs by " + (int) distance + "m");
                    }
                }
            }
        }
    }

    /**
     * Validate image is recent (within 48 hours).
     */
    private void validateImageFreshness(DogReport report, List<String> errors) {
        if (report.getEvidence() == null) return;

        long maxAgeMs = 48 * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        for (Evidence evidence : report.getEvidence()) {
            if (evidence.getCaptureTime() != null) {
                if ((now - evidence.getCaptureTime()) > maxAgeMs) {
                    errors.add("IMAGE_TOO_OLD: Photo is older than 48 hours");
                    break;
                }
            }
        }
    }

    /**
     * Check for duplicate images using perceptual hash.
     */
    private void validateDuplicates(DogReport report, List<String> errors) {
        if (report.getImageHash() == null) return;

        List<DogReport> duplicates = repository.findByImageHash(
                report.getTenantId(), report.getImageHash());

        // Filter out current report
        duplicates = duplicates.stream()
                .filter(d -> !d.getId().equals(report.getId()))
                .toList();

        if (!duplicates.isEmpty()) {
            errors.add("DUPLICATE_IMAGE: Similar image found in report " +
                    duplicates.get(0).getReportNumber());
        }
    }

    /**
     * Validate location is within tenant boundary.
     */
    private void validateTenantBoundary(DogReport report, List<String> errors) {
        // This would call Location Service to validate
        // Simplified for pseudo-code
        if (report.getLocation().getLocalityCode() == null) {
            errors.add("INVALID_LOCATION: Location not within valid boundary");
        }
    }

    /**
     * Transition report status after validation.
     */
    private void transitionReport(RequestInfo requestInfo, DogReport report,
                                   String action, String reason) {
        report.setWorkflow(Workflow.builder()
                .action(action)
                .comments(reason)
                .build());

        if (reason != null && action.equals(WorkflowService.ACTION_AUTO_VALIDATE_FAIL)) {
            report.setRejectionReason("AUTO_VALIDATION_FAILED");
            report.setRejectionRemarks(reason);
        }

        DogReportRequest updateRequest = DogReportRequest.builder()
                .requestInfo(requestInfo)
                .dogReports(List.of(report))
                .build();

        // Transition workflow
        workflowService.updateWorkflowStatus(updateRequest);

        // Push to update topic for persistence
        producer.push(updateTopic, updateRequest);
    }

    /**
     * Calculate distance between two GPS points (Haversine formula).
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
