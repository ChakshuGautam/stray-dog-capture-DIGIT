package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.digit.sdcrs.model.DogReport;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * NotificationService - Sends SMS/Email notifications for status changes.
 * Integrates with DIGIT Notification Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RestTemplate restTemplate;
    private final SDCRSConfiguration config;
    private final LocalizationService localizationService;

    // SMS template codes (configured in Localization service)
    private static final String SMS_REPORT_CREATED = "SDCRS_SMS_REPORT_CREATED";
    private static final String SMS_REPORT_VERIFIED = "SDCRS_SMS_REPORT_VERIFIED";
    private static final String SMS_REPORT_REJECTED = "SDCRS_SMS_REPORT_REJECTED";
    private static final String SMS_REPORT_ASSIGNED = "SDCRS_SMS_REPORT_ASSIGNED";
    private static final String SMS_REPORT_CAPTURED = "SDCRS_SMS_REPORT_CAPTURED";
    private static final String SMS_PAYOUT_COMPLETED = "SDCRS_SMS_PAYOUT_COMPLETED";
    private static final String SMS_PAYOUT_CAP_EXCEEDED = "SDCRS_SMS_PAYOUT_CAP_EXCEEDED";

    /**
     * Send notification based on report status.
     */
    public void sendStatusNotification(RequestInfo requestInfo, DogReport report) {
        log.info("Sending notification for report: {} status: {}",
                report.getReportNumber(), report.getStatus());

        String templateCode = getTemplateCode(report.getStatus(), report);
        if (templateCode == null) {
            log.debug("No notification template for status: {}", report.getStatus());
            return;
        }

        // Get localized message template
        String message = localizationService.getMessage(
                report.getTenantId(),
                "en_IN", // Default locale
                "sdcrs",
                templateCode,
                report.getReportNumber(),
                report.getTrackingId(),
                report.getTrackingUrl()
        );

        // Send SMS to reporter
        sendSms(report.getReporterPhone(), message);
    }

    /**
     * Send payout notification to teacher.
     */
    public void sendPayoutNotification(DogReport report, String payoutStatus) {
        log.info("Sending payout notification for report: {} status: {}",
                report.getReportNumber(), payoutStatus);

        String templateCode = switch (payoutStatus) {
            case "COMPLETED" -> SMS_PAYOUT_COMPLETED;
            case "CAP_EXCEEDED" -> SMS_PAYOUT_CAP_EXCEEDED;
            default -> null;
        };

        if (templateCode == null) return;

        String message = localizationService.getMessage(
                report.getTenantId(),
                "en_IN",
                "sdcrs",
                templateCode,
                report.getReportNumber(),
                report.getPayoutAmount() != null ? report.getPayoutAmount().toString() : "0"
        );

        sendSms(report.getReporterPhone(), message);
    }

    /**
     * Send SMS via DIGIT Notification Service.
     */
    private void sendSms(String mobileNumber, String message) {
        if (mobileNumber == null || message == null) {
            log.warn("Cannot send SMS: missing phone or message");
            return;
        }

        try {
            String url = config.getNotificationHost() + config.getSmsPath();

            Map<String, Object> smsRequest = Map.of(
                    "mobileNumber", mobileNumber,
                    "message", message
            );

            restTemplate.postForObject(url, Map.of(
                    "smsRequest", smsRequest
            ), String.class);

            log.info("SMS sent to: {}", maskPhone(mobileNumber));
        } catch (Exception e) {
            log.error("Error sending SMS", e);
        }
    }

    /**
     * Get template code based on status.
     */
    private String getTemplateCode(String status, DogReport report) {
        return switch (status) {
            case "PENDING_VALIDATION" -> SMS_REPORT_CREATED;
            case "PENDING_VERIFICATION" -> null; // No notification for internal transition
            case "VERIFIED" -> SMS_REPORT_VERIFIED;
            case "REJECTED", "AUTO_REJECTED", "DUPLICATE" -> SMS_REPORT_REJECTED;
            case "ASSIGNED" -> SMS_REPORT_ASSIGNED;
            case "CAPTURED" -> SMS_REPORT_CAPTURED;
            case "RESOLVED" -> SMS_PAYOUT_COMPLETED;
            default -> null;
        };
    }

    /**
     * Mask phone number for logging.
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }

    /**
     * Send notification to assigned MC officer.
     */
    public void sendAssignmentNotification(DogReport report) {
        if (report.getAssignedOfficerId() == null) return;

        // Would fetch officer phone from User service
        // Simplified for pseudo-code
        log.info("Assignment notification would be sent to officer: {}",
                report.getAssignedOfficerId());
    }
}
