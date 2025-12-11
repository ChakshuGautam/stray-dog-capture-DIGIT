package org.digit.sdcrs.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.*;
import org.digit.sdcrs.service.IdGenService;
import org.digit.sdcrs.service.UrlShortenerService;
import org.digit.sdcrs.service.UserService;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DogReportEnrichment - Enriches dog report requests with system-generated data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DogReportEnrichment {

    private final IdGenService idGenService;
    private final UserService userService;
    private final UrlShortenerService urlShortenerService;

    @Value("${sdcrs.tracking.base-url}")
    private String trackingBaseUrl;

    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_VERIFIER = "VERIFIER";
    private static final String ROLE_MC_OFFICER = "MC_OFFICER";
    private static final String ROLE_MC_SUPERVISOR = "MC_SUPERVISOR";

    /**
     * Enrich create request with IDs, user details, audit info.
     *
     * Steps:
     * 1. Generate unique ID for each report
     * 2. Generate report number via IDGen (DJ-SDCRS-YYYY-NNNNNN)
     * 3. Generate tracking ID via IDGen (ABC123)
     * 4. Generate tracking URL via URL Shortener
     * 5. Enrich reporter details from User service
     * 6. Set audit details
     */
    public void enrichCreate(DogReportRequest request) {
        log.info("Enriching create request");
        RequestInfo requestInfo = request.getRequestInfo();
        User user = requestInfo.getUserInfo();
        long currentTime = System.currentTimeMillis();

        for (DogReport report : request.getDogReports()) {
            // Generate UUID
            report.setId(UUID.randomUUID().toString());

            // Generate location ID if not present
            if (report.getLocation() != null && report.getLocation().getId() == null) {
                report.getLocation().setId(UUID.randomUUID().toString());
            }

            // Generate evidence IDs
            if (report.getEvidence() != null) {
                for (Evidence evidence : report.getEvidence()) {
                    if (evidence.getId() == null) {
                        evidence.setId(UUID.randomUUID().toString());
                    }
                    evidence.setUploadedAt(currentTime);
                    evidence.setUploadedBy(user.getUuid());
                }
            }

            // Generate report number
            String reportNumber = idGenService.generateReportNumber(
                    requestInfo, report.getTenantId());
            report.setReportNumber(reportNumber);

            // Generate tracking ID
            String trackingId = idGenService.generateTrackingId(
                    requestInfo, report.getTenantId());
            report.setTrackingId(trackingId);

            // Generate tracking URL
            String longUrl = trackingBaseUrl + "?trackingId=" + trackingId;
            String shortUrl = urlShortenerService.shortenUrl(longUrl);
            report.setTrackingUrl(shortUrl);

            // Enrich reporter details
            report.setReporterId(user.getUuid());
            report.setReporterName(user.getName());
            report.setReporterPhone(user.getMobileNumber());

            // Fetch additional user details (school, etc.)
            enrichReporterDetails(requestInfo, report);

            // Set audit details
            AuditDetails auditDetails = AuditDetails.builder()
                    .createdBy(user.getUuid())
                    .createdTime(currentTime)
                    .lastModifiedBy(user.getUuid())
                    .lastModifiedTime(currentTime)
                    .build();
            report.setAuditDetails(auditDetails);

            // Set initial status
            report.setStatus("PENDING_VALIDATION");
            report.setPayoutEligible(false);

            log.info("Enriched report: {} with tracking: {}",
                    report.getReportNumber(), report.getTrackingId());
        }
    }

    /**
     * Enrich update request with audit info and action-specific data.
     */
    public void enrichUpdate(DogReportRequest request, List<DogReport> existingReports) {
        log.info("Enriching update request");
        RequestInfo requestInfo = request.getRequestInfo();
        User user = requestInfo.getUserInfo();
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < request.getDogReports().size(); i++) {
            DogReport report = request.getDogReports().get(i);
            DogReport existing = existingReports.get(i);

            // Preserve immutable fields from existing report
            report.setId(existing.getId());
            report.setTenantId(existing.getTenantId());
            report.setReportNumber(existing.getReportNumber());
            report.setTrackingId(existing.getTrackingId());
            report.setTrackingUrl(existing.getTrackingUrl());
            report.setReporterId(existing.getReporterId());
            report.setReporterName(existing.getReporterName());
            report.setReporterPhone(existing.getReporterPhone());
            report.setLocation(existing.getLocation());
            report.setEvidence(existing.getEvidence());

            // Update audit details
            AuditDetails auditDetails = existing.getAuditDetails();
            auditDetails.setLastModifiedBy(user.getUuid());
            auditDetails.setLastModifiedTime(currentTime);
            report.setAuditDetails(auditDetails);

            // Enrich based on workflow action
            String action = report.getWorkflow().getAction();
            enrichByAction(report, existing, user, currentTime, action);
        }
    }

    /**
     * Enrich search criteria with role-based filtering.
     *
     * - TEACHER: Can only see own reports
     * - VERIFIER: Can see reports in PENDING_VERIFICATION
     * - MC_OFFICER: Can see assigned reports
     * - MC_SUPERVISOR: Can see reports in ward/district
     */
    public void enrichSearchCriteria(RequestInfo requestInfo, DogReportSearchCriteria criteria) {
        User user = requestInfo.getUserInfo();
        Set<String> roles = getUserRoles(user);

        if (roles.contains(ROLE_TEACHER)) {
            // Teachers can only see their own reports
            criteria.setReporterId(user.getUuid());
        } else if (roles.contains(ROLE_MC_OFFICER)) {
            // MC Officers can see assigned reports
            criteria.setAssignedOfficerId(user.getUuid());
        } else if (roles.contains(ROLE_VERIFIER)) {
            // Verifiers see reports in PENDING_VERIFICATION
            if (criteria.getStatus() == null || criteria.getStatus().isEmpty()) {
                criteria.setStatus(List.of("PENDING_VERIFICATION"));
            }
        } else if (roles.contains(ROLE_MC_SUPERVISOR)) {
            // Supervisors can see all reports in their jurisdiction
            // Ward/district filtering would be based on user's assigned boundaries
            // This is simplified - actual implementation would fetch user's boundaries
        }
        // Admins and other roles can see all reports (no filtering)

        // Set default pagination
        if (criteria.getLimit() == null) {
            criteria.setLimit(100);
        }
        if (criteria.getOffset() == null) {
            criteria.setOffset(0);
        }
    }

    // --- Private enrichment methods ---

    private void enrichReporterDetails(RequestInfo requestInfo, DogReport report) {
        try {
            // Fetch user details to get school information
            User userDetails = userService.getUserById(requestInfo, report.getReporterId());
            if (userDetails != null && userDetails.getTenants() != null) {
                // Get school from user's tenant data (simplified)
                // Actual implementation would look up user's department/organization
            }
        } catch (Exception e) {
            log.warn("Could not fetch additional reporter details", e);
        }
    }

    private void enrichByAction(DogReport report, DogReport existing, User user,
                                 long currentTime, String action) {
        switch (action) {
            case "VERIFY" -> {
                report.setVerifiedBy(user.getUuid());
                report.setVerifiedAt(currentTime);
            }
            case "REJECT", "MARK_DUPLICATE" -> {
                report.setVerifiedBy(user.getUuid());
                report.setVerifiedAt(currentTime);
                // Rejection reason should already be set in request
            }
            case "ASSIGN_MC" -> {
                report.setAssignedAt(currentTime);
                // Assigned officer should be set in request via workflow.assignees
                if (report.getWorkflow().getAssignes() != null &&
                        !report.getWorkflow().getAssignes().isEmpty()) {
                    report.setAssignedOfficerId(report.getWorkflow().getAssignes().get(0));
                    enrichAssigneeDetails(report);
                }
            }
            case "MARK_CAPTURED" -> {
                report.setResolvedAt(currentTime);
                report.setPayoutEligible(true);
                // Payout amount will be set by PayoutService
            }
            case "MARK_UNABLE_TO_LOCATE" -> {
                report.setResolvedAt(currentTime);
                report.setPayoutEligible(false);
            }
            default -> {
                // Other actions - no special enrichment needed
            }
        }
    }

    private void enrichAssigneeDetails(DogReport report) {
        // Fetch assignee name from User service
        // Simplified for pseudo-code
        try {
            // User assignee = userService.getUserById(requestInfo, report.getAssignedOfficerId());
            // report.setAssignedOfficerName(assignee.getName());
        } catch (Exception e) {
            log.warn("Could not fetch assignee details", e);
        }
    }

    private Set<String> getUserRoles(User user) {
        if (user.getRoles() == null) {
            return Set.of();
        }
        return user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}
