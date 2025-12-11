package org.digit.sdcrs.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportRequest;
import org.digit.sdcrs.model.Evidence;
import org.digit.sdcrs.model.Location;
import org.digit.sdcrs.repository.DogReportRepository;
import org.digit.sdcrs.service.MDMSService;
import org.digit.sdcrs.service.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DogReportValidator - Validates dog report requests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DogReportValidator {

    private final MDMSService mdmsService;
    private final DogReportRepository repository;
    private final WorkflowService workflowService;

    @Value("${sdcrs.validation.gps.tolerance.meters:100}")
    private double gpsToleranceMeters;

    @Value("${sdcrs.validation.image.max.age.hours:48}")
    private int imageMaxAgeHours;

    @Value("${sdcrs.validation.daily.limit:5}")
    private int dailyReportLimit;

    @Value("${sdcrs.validation.duplicate.threshold:0.90}")
    private double duplicateThreshold;

    // Role codes for permission validation
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_VERIFIER = "VERIFIER";
    private static final String ROLE_MC_OFFICER = "MC_OFFICER";
    private static final String ROLE_MC_SUPERVISOR = "MC_SUPERVISOR";

    /**
     * Validate create request.
     */
    public void validateCreate(DogReportRequest request) {
        log.info("Validating create request");
        Map<String, String> errors = new HashMap<>();

        RequestInfo requestInfo = request.getRequestInfo();

        for (DogReport report : request.getDogReports()) {
            // Validate tenant
            if (report.getTenantId() == null || report.getTenantId().isEmpty()) {
                errors.put("TENANT_ID_REQUIRED", "Tenant ID is required");
            }

            // Validate service type
            if (report.getServiceType() == null || report.getServiceType().isEmpty()) {
                errors.put("SERVICE_TYPE_REQUIRED", "Service type is required");
            } else if (!mdmsService.isValidServiceType(requestInfo, report.getTenantId(), report.getServiceType())) {
                errors.put("INVALID_SERVICE_TYPE", "Invalid service type: " + report.getServiceType());
            }

            // Validate location
            validateLocation(report.getLocation(), errors);

            // Validate evidence (at least one dog photo required)
            validateEvidenceRequired(report.getEvidence(), errors);

            // Validate daily limit
            validateDailyLimit(requestInfo, report, errors);
        }

        if (!errors.isEmpty()) {
            throw new CustomException(errors);
        }
    }

    /**
     * Validate update request.
     */
    public void validateUpdate(DogReportRequest request) {
        log.info("Validating update request");
        Map<String, String> errors = new HashMap<>();

        for (DogReport report : request.getDogReports()) {
            // Validate ID exists
            if (report.getId() == null || report.getId().isEmpty()) {
                errors.put("ID_REQUIRED", "Report ID is required for update");
            }

            // Validate report number exists
            if (report.getReportNumber() == null || report.getReportNumber().isEmpty()) {
                errors.put("REPORT_NUMBER_REQUIRED", "Report number is required for update");
            }

            // Validate workflow action
            if (report.getWorkflow() == null || report.getWorkflow().getAction() == null) {
                errors.put("WORKFLOW_ACTION_REQUIRED", "Workflow action is required");
            }

            // Validate rejection reason if rejecting
            if (report.getWorkflow() != null &&
                    WorkflowService.ACTION_REJECT.equals(report.getWorkflow().getAction())) {
                if (report.getRejectionReason() == null || report.getRejectionReason().isEmpty()) {
                    errors.put("REJECTION_REASON_REQUIRED", "Rejection reason is required");
                }
            }

            // Validate resolution type if resolving
            if (report.getWorkflow() != null &&
                    (WorkflowService.ACTION_MARK_CAPTURED.equals(report.getWorkflow().getAction()) ||
                     WorkflowService.ACTION_MARK_UNABLE_TO_LOCATE.equals(report.getWorkflow().getAction()))) {
                if (report.getResolutionType() == null || report.getResolutionType().isEmpty()) {
                    errors.put("RESOLUTION_TYPE_REQUIRED", "Resolution type is required");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomException(errors);
        }
    }

    /**
     * Validate evidence files (photos).
     */
    public void validateEvidence(DogReportRequest request) {
        log.info("Validating evidence");
        Map<String, String> errors = new HashMap<>();

        for (DogReport report : request.getDogReports()) {
            List<Evidence> evidenceList = report.getEvidence();
            if (evidenceList == null || evidenceList.isEmpty()) {
                continue;
            }

            for (Evidence evidence : evidenceList) {
                // Validate GPS from EXIF
                if (evidence.getEvidenceType() == Evidence.EvidenceType.DOG_PHOTO) {
                    validateGpsMatch(report.getLocation(), evidence, errors);
                    validateImageAge(evidence, errors);
                    validateDuplicate(report, evidence, errors);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomException(errors);
        }
    }

    /**
     * Validate capture evidence for MARK_CAPTURED action.
     */
    public void validateCaptureEvidence(DogReportRequest request) {
        for (DogReport report : request.getDogReports()) {
            if (report.getWorkflow() != null &&
                    WorkflowService.ACTION_MARK_CAPTURED.equals(report.getWorkflow().getAction())) {

                if (report.getCapturePhotoFileStoreId() == null ||
                        report.getCapturePhotoFileStoreId().isEmpty()) {
                    throw new CustomException("CAPTURE_PHOTO_REQUIRED",
                            "Capture photo is required when marking as captured");
                }
            }
        }
    }

    /**
     * Validate user has permission for workflow action.
     */
    public void validatePermissions(DogReportRequest request, List<DogReport> existingReports) {
        log.info("Validating permissions");
        User user = request.getRequestInfo().getUserInfo();
        Set<String> userRoles = getUserRoles(user);

        for (int i = 0; i < request.getDogReports().size(); i++) {
            DogReport report = request.getDogReports().get(i);
            DogReport existing = existingReports.get(i);
            String action = report.getWorkflow().getAction();

            // Check if action is valid for current status
            if (!workflowService.isValidAction(existing.getStatus(), action)) {
                throw new CustomException("INVALID_ACTION",
                        "Action " + action + " is not valid for status " + existing.getStatus());
            }

            // Check role-based permissions
            validateRolePermission(action, userRoles);

            // Additional checks based on action
            if (WorkflowService.ACTION_MARK_CAPTURED.equals(action) ||
                    WorkflowService.ACTION_MARK_UNABLE_TO_LOCATE.equals(action)) {
                // Only assigned officer can resolve
                if (!user.getUuid().equals(existing.getAssignedOfficerId())) {
                    throw new CustomException("NOT_ASSIGNED_OFFICER",
                            "Only assigned officer can mark resolution");
                }
            }
        }
    }

    // --- Private validation methods ---

    private void validateLocation(Location location, Map<String, String> errors) {
        if (location == null) {
            errors.put("LOCATION_REQUIRED", "Location is required");
            return;
        }

        if (location.getLatitude() == null || location.getLongitude() == null) {
            errors.put("GPS_COORDINATES_REQUIRED", "GPS coordinates are required");
        }

        if (location.getLocalityCode() == null || location.getLocalityCode().isEmpty()) {
            errors.put("LOCALITY_REQUIRED", "Locality is required");
        }
    }

    private void validateEvidenceRequired(List<Evidence> evidenceList, Map<String, String> errors) {
        if (evidenceList == null || evidenceList.isEmpty()) {
            errors.put("EVIDENCE_REQUIRED", "At least one photo is required");
            return;
        }

        boolean hasDogPhoto = evidenceList.stream()
                .anyMatch(e -> e.getEvidenceType() == Evidence.EvidenceType.DOG_PHOTO);

        if (!hasDogPhoto) {
            errors.put("DOG_PHOTO_REQUIRED", "Dog photo is required");
        }
    }

    private void validateGpsMatch(Location location, Evidence evidence, Map<String, String> errors) {
        if (evidence.getLatitude() == null || evidence.getLongitude() == null) {
            // EXIF GPS not available - log warning but don't fail
            log.warn("GPS not available in photo EXIF");
            return;
        }

        double distance = calculateDistance(
                location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue(),
                evidence.getLatitude(),
                evidence.getLongitude()
        );

        if (distance > gpsToleranceMeters) {
            errors.put("GPS_MISMATCH",
                    String.format("Photo GPS (%.4f, %.4f) is %.0fm from submitted location",
                            evidence.getLatitude(), evidence.getLongitude(), distance));
        }
    }

    private void validateImageAge(Evidence evidence, Map<String, String> errors) {
        if (evidence.getCaptureTime() == null) {
            return;
        }

        long ageMs = System.currentTimeMillis() - evidence.getCaptureTime();
        long maxAgeMs = imageMaxAgeHours * 60 * 60 * 1000L;

        if (ageMs > maxAgeMs) {
            errors.put("IMAGE_TOO_OLD",
                    "Photo is older than " + imageMaxAgeHours + " hours");
        }
    }

    private void validateDuplicate(DogReport report, Evidence evidence, Map<String, String> errors) {
        if (evidence.getImageHash() == null) {
            return;
        }

        List<DogReport> duplicates = repository.findByImageHash(
                report.getTenantId(), evidence.getImageHash());

        if (!duplicates.isEmpty()) {
            errors.put("DUPLICATE_IMAGE",
                    "Similar photo already submitted in report: " +
                            duplicates.get(0).getReportNumber());
        }
    }

    private void validateDailyLimit(RequestInfo requestInfo, DogReport report, Map<String, String> errors) {
        String reporterId = requestInfo.getUserInfo().getUuid();

        LocalDate today = LocalDate.now();
        Long fromDate = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long toDate = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Integer todayCount = repository.getDailyReportCount(
                report.getTenantId(), reporterId, fromDate, toDate);

        if (todayCount >= dailyReportLimit) {
            errors.put("DAILY_LIMIT_EXCEEDED",
                    "Daily report limit of " + dailyReportLimit + " exceeded");
        }
    }

    private void validateRolePermission(String action, Set<String> userRoles) {
        Map<String, Set<String>> actionRoles = Map.of(
                WorkflowService.ACTION_SUBMIT, Set.of(ROLE_TEACHER),
                WorkflowService.ACTION_VERIFY, Set.of(ROLE_VERIFIER),
                WorkflowService.ACTION_REJECT, Set.of(ROLE_VERIFIER),
                WorkflowService.ACTION_MARK_DUPLICATE, Set.of(ROLE_VERIFIER),
                WorkflowService.ACTION_ASSIGN_MC, Set.of(ROLE_MC_SUPERVISOR),
                WorkflowService.ACTION_START_FIELD_VISIT, Set.of(ROLE_MC_OFFICER),
                WorkflowService.ACTION_MARK_CAPTURED, Set.of(ROLE_MC_OFFICER),
                WorkflowService.ACTION_MARK_UNABLE_TO_LOCATE, Set.of(ROLE_MC_OFFICER)
        );

        Set<String> requiredRoles = actionRoles.get(action);
        if (requiredRoles == null) {
            return; // Unknown action - let workflow service handle
        }

        boolean hasRole = requiredRoles.stream().anyMatch(userRoles::contains);
        if (!hasRole) {
            throw new CustomException("UNAUTHORIZED_ACTION",
                    "User does not have permission for action: " + action);
        }
    }

    private Set<String> getUserRoles(User user) {
        if (user.getRoles() == null) {
            return Set.of();
        }
        return Set.copyOf(user.getRoles().stream()
                .map(role -> role.getCode())
                .toList());
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
