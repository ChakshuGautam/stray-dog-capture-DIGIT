package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.TrackResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TrackingService - Builds sanitized tracking responses for public API.
 * Removes all PII and provides localized status descriptions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final LocalizationService localizationService;
    private final WorkflowService workflowService;

    @Value("${sdcrs.sla.default.hours:72}")
    private Integer defaultSlaHours;

    // SLA hours by status
    private static final Map<String, Integer> SLA_BY_STATUS = Map.of(
            "PENDING_VALIDATION", 1,
            "PENDING_VERIFICATION", 24,
            "VERIFIED", 48,
            "ASSIGNED", 72,
            "IN_PROGRESS", 72,
            "CAPTURED", 24
    );

    /**
     * Build sanitized tracking response from dog report.
     * Removes all PII (reporter name, phone, officer details, etc.)
     */
    public TrackResponse buildTrackResponse(DogReport report, String locale) {
        log.debug("Building track response for report: {}", report.getReportNumber());

        // Get localized messages
        Map<String, String> messages = localizationService.getMessages(
                report.getTenantId(), locale, "sdcrs");

        // Build timeline (sanitized - no names)
        List<TrackResponse.TimelineEntry> timeline = buildTimeline(report, messages);

        // Calculate SLA
        Integer slaHours = getSlaHours(report.getStatus());
        Long estimatedResolution = calculateEstimatedResolution(report, slaHours);
        Integer slaDaysRemaining = calculateSlaDaysRemaining(report.getAuditDetails().getCreatedTime(), estimatedResolution);

        return TrackResponse.builder()
                .reportNumber(report.getReportNumber())
                .trackingId(report.getTrackingId())
                .trackingUrl(report.getTrackingUrl())
                .status(report.getStatus())
                .statusDescription(getStatusDescription(report.getStatus(), messages))
                .serviceType(report.getServiceType())
                .serviceTypeDescription(getServiceTypeDescription(report.getServiceType(), messages))
                // Location - only locality level, no exact coordinates
                .locality(report.getLocation().getLocality())
                .ward(report.getLocation().getWard())
                .city(report.getLocation().getCity())
                // Timeline
                .timeline(timeline)
                // SLA info
                .reportedAt(report.getAuditDetails().getCreatedTime())
                .estimatedResolution(estimatedResolution)
                .slaHours(slaHours)
                .slaDaysRemaining(slaDaysRemaining)
                .build();
    }

    /**
     * Build timeline from workflow history.
     * Sanitized - only shows status changes, no actor names.
     */
    private List<TrackResponse.TimelineEntry> buildTimeline(
            DogReport report, Map<String, String> messages) {

        List<TrackResponse.TimelineEntry> timeline = new ArrayList<>();

        // Add creation entry
        timeline.add(TrackResponse.TimelineEntry.builder()
                .status("SUBMITTED")
                .statusDescription(getStatusDescription("SUBMITTED", messages))
                .timestamp(report.getAuditDetails().getCreatedTime())
                .actorRole("TEACHER")
                .build());

        // Add verification entry if verified
        if (report.getVerifiedAt() != null) {
            timeline.add(TrackResponse.TimelineEntry.builder()
                    .status("VERIFIED")
                    .statusDescription(getStatusDescription("VERIFIED", messages))
                    .timestamp(report.getVerifiedAt())
                    .actorRole("VERIFIER")
                    .build());
        }

        // Add assignment entry if assigned
        if (report.getAssignedAt() != null) {
            timeline.add(TrackResponse.TimelineEntry.builder()
                    .status("ASSIGNED")
                    .statusDescription(getStatusDescription("ASSIGNED", messages))
                    .timestamp(report.getAssignedAt())
                    .actorRole("MC_SUPERVISOR")
                    .build());
        }

        // Add resolution entry if resolved
        if (report.getResolvedAt() != null) {
            String resolutionStatus = report.getResolutionType() != null
                    ? report.getResolutionType() : report.getStatus();
            timeline.add(TrackResponse.TimelineEntry.builder()
                    .status(resolutionStatus)
                    .statusDescription(getStatusDescription(resolutionStatus, messages))
                    .timestamp(report.getResolvedAt())
                    .actorRole("MC_OFFICER")
                    .remarks(sanitizeRemarks(report.getResolutionRemarks()))
                    .build());
        }

        // Add current status if different from last entry
        String currentStatus = report.getStatus();
        if (timeline.isEmpty() || !timeline.get(timeline.size() - 1).getStatus().equals(currentStatus)) {
            timeline.add(TrackResponse.TimelineEntry.builder()
                    .status(currentStatus)
                    .statusDescription(getStatusDescription(currentStatus, messages))
                    .timestamp(report.getAuditDetails().getLastModifiedTime())
                    .build());
        }

        return timeline;
    }

    /**
     * Get localized status description.
     */
    private String getStatusDescription(String status, Map<String, String> messages) {
        String key = "SDCRS_STATUS_" + status;
        return messages.getOrDefault(key, formatStatusDefault(status));
    }

    /**
     * Get localized service type description.
     */
    private String getServiceTypeDescription(String serviceType, Map<String, String> messages) {
        String key = "SDCRS_SERVICE_TYPE_" + serviceType;
        return messages.getOrDefault(key, formatStatusDefault(serviceType));
    }

    /**
     * Format status as readable text if no localization found.
     */
    private String formatStatusDefault(String status) {
        if (status == null) return "";
        return status.replace("_", " ")
                .toLowerCase()
                .substring(0, 1).toUpperCase()
                + status.replace("_", " ").toLowerCase().substring(1);
    }

    /**
     * Sanitize remarks to remove any potential PII.
     */
    private String sanitizeRemarks(String remarks) {
        if (remarks == null) return null;
        // Remove phone numbers
        remarks = remarks.replaceAll("\\d{10}", "***");
        // Remove email patterns
        remarks = remarks.replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "***");
        return remarks;
    }

    /**
     * Get SLA hours for a status.
     */
    private Integer getSlaHours(String status) {
        return SLA_BY_STATUS.getOrDefault(status, defaultSlaHours);
    }

    /**
     * Calculate estimated resolution time.
     */
    private Long calculateEstimatedResolution(DogReport report, Integer slaHours) {
        if (isTerminalStatus(report.getStatus())) {
            return report.getAuditDetails().getLastModifiedTime();
        }

        Long createdTime = report.getAuditDetails().getCreatedTime();
        return Instant.ofEpochMilli(createdTime)
                .plus(slaHours, ChronoUnit.HOURS)
                .toEpochMilli();
    }

    /**
     * Calculate remaining days for SLA.
     */
    private Integer calculateSlaDaysRemaining(Long createdTime, Long estimatedResolution) {
        long now = System.currentTimeMillis();
        if (now >= estimatedResolution) {
            return 0;
        }
        long diff = estimatedResolution - now;
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    /**
     * Check if status is terminal (no further transitions).
     */
    private boolean isTerminalStatus(String status) {
        return status != null && (
                status.equals("RESOLVED") ||
                status.equals("REJECTED") ||
                status.equals("AUTO_REJECTED") ||
                status.equals("DUPLICATE") ||
                status.equals("UNABLE_TO_LOCATE")
        );
    }
}
