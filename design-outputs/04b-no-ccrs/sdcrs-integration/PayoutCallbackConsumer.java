package org.egov.sdcrs.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.sdcrs.config.SDCRSConfiguration;
import org.egov.sdcrs.service.DogReportService;
import org.egov.sdcrs.service.WorkflowService;
import org.egov.sdcrs.web.models.DogReport;
import org.egov.sdcrs.web.models.payout.PayoutCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer for payout callbacks from UPI Adapter.
 *
 * Listens on: sdcrs-payout-callback
 *
 * Handles:
 * - PAYOUT_SUCCESS: Transitions report from PAYOUT_PENDING → RESOLVED
 * - PAYOUT_FAILED: Transitions report from PAYOUT_PENDING → PAYOUT_FAILED
 * - PAYOUT_REVERSED: Marks report for manual review
 */
@Component
@Slf4j
public class PayoutCallbackConsumer {

    private final ObjectMapper objectMapper;
    private final DogReportService dogReportService;
    private final WorkflowService workflowService;
    private final SDCRSConfiguration config;

    @Autowired
    public PayoutCallbackConsumer(ObjectMapper objectMapper,
                                   DogReportService dogReportService,
                                   WorkflowService workflowService,
                                   SDCRSConfiguration config) {
        this.objectMapper = objectMapper;
        this.dogReportService = dogReportService;
        this.workflowService = workflowService;
        this.config = config;
    }

    @KafkaListener(topics = "${sdcrs.kafka.payout.callback.topic}")
    public void listenPayoutCallback(HashMap<String, Object> record) {
        try {
            PayoutCallback callback = objectMapper.convertValue(record, PayoutCallback.class);
            log.info("Received payout callback for reference: {}, status: {}",
                callback.getReferenceId(), callback.getStatus());

            processCallback(callback);

        } catch (Exception e) {
            log.error("Error processing payout callback: {}", record, e);
            // Don't throw - let Kafka commit offset to avoid infinite retry
            // Failed callbacks should be handled via DLQ or manual intervention
        }
    }

    private void processCallback(PayoutCallback callback) {
        String referenceId = callback.getReferenceId();
        String status = callback.getStatus();

        // Fetch the dog report
        DogReport report = dogReportService.getReportById(referenceId);
        if (report == null) {
            log.error("Dog report not found for payout callback: {}", referenceId);
            return;
        }

        // Create system request info for workflow transition
        RequestInfo systemRequestInfo = createSystemRequestInfo();

        switch (status) {
            case "COMPLETED":
                handlePayoutSuccess(report, callback, systemRequestInfo);
                break;

            case "FAILED":
                handlePayoutFailed(report, callback, systemRequestInfo);
                break;

            case "REVERSED":
                handlePayoutReversed(report, callback, systemRequestInfo);
                break;

            default:
                log.warn("Unknown payout status received: {} for report: {}",
                    status, referenceId);
        }
    }

    /**
     * Payout successful - transition to RESOLVED
     */
    private void handlePayoutSuccess(DogReport report, PayoutCallback callback,
                                      RequestInfo requestInfo) {
        log.info("Processing payout success for report: {}", report.getReportNumber());

        // Update report with payout details
        Map<String, Object> payoutDetails = new HashMap<>();
        payoutDetails.put("payoutId", callback.getPayoutId());
        payoutDetails.put("payoutNumber", callback.getPayoutNumber());
        payoutDetails.put("utr", callback.getUtr());
        payoutDetails.put("processedAt", callback.getProcessedAt());
        payoutDetails.put("amount", callback.getAmount());

        report.getAdditionalDetails().put("payoutDetails", payoutDetails);

        // Transition workflow: PAYOUT_PENDING → RESOLVED
        workflowService.transitionWorkflow(
            report.getTenantId(),
            report.getId(),
            "SDCRS",
            "PAYOUT_SUCCESS",
            requestInfo,
            "Payout completed. UTR: " + callback.getUtr()
        );

        log.info("Report {} transitioned to RESOLVED. UTR: {}",
            report.getReportNumber(), callback.getUtr());
    }

    /**
     * Payout failed - transition to PAYOUT_FAILED for retry
     */
    private void handlePayoutFailed(DogReport report, PayoutCallback callback,
                                     RequestInfo requestInfo) {
        log.info("Processing payout failure for report: {}", report.getReportNumber());

        // Store failure details
        Map<String, Object> failureDetails = new HashMap<>();
        failureDetails.put("payoutId", callback.getPayoutId());
        failureDetails.put("failureReason", callback.getFailureReason());
        failureDetails.put("errorCode", callback.getErrorCode());
        failureDetails.put("failedAt", System.currentTimeMillis());
        failureDetails.put("retryCount", callback.getRetryCount());

        report.getAdditionalDetails().put("payoutFailure", failureDetails);

        // Transition workflow: PAYOUT_PENDING → PAYOUT_FAILED
        workflowService.transitionWorkflow(
            report.getTenantId(),
            report.getId(),
            "SDCRS",
            "PAYOUT_FAILED",
            requestInfo,
            "Payout failed: " + callback.getFailureReason()
        );

        log.warn("Report {} payout failed. Reason: {}",
            report.getReportNumber(), callback.getFailureReason());
    }

    /**
     * Payout reversed - rare case, needs manual intervention
     */
    private void handlePayoutReversed(DogReport report, PayoutCallback callback,
                                       RequestInfo requestInfo) {
        log.warn("Payout reversed for report: {}. Manual intervention required.",
            report.getReportNumber());

        // Store reversal details
        Map<String, Object> reversalDetails = new HashMap<>();
        reversalDetails.put("payoutId", callback.getPayoutId());
        reversalDetails.put("originalUtr", callback.getUtr());
        reversalDetails.put("reversalReason", callback.getFailureReason());
        reversalDetails.put("reversedAt", System.currentTimeMillis());

        report.getAdditionalDetails().put("payoutReversal", reversalDetails);

        // Transition to PAYOUT_FAILED for admin review
        workflowService.transitionWorkflow(
            report.getTenantId(),
            report.getId(),
            "SDCRS",
            "PAYOUT_FAILED",
            requestInfo,
            "Payout reversed after success. Reason: " + callback.getFailureReason()
        );
    }

    /**
     * Creates a system user request info for automated transitions
     */
    private RequestInfo createSystemRequestInfo() {
        User systemUser = User.builder()
            .id(0L)
            .uuid("SYSTEM")
            .userName("SYSTEM")
            .type("SYSTEM")
            .roles(Collections.emptyList())
            .build();

        return RequestInfo.builder()
            .apiId("Rainmaker")
            .ver(".01")
            .action("")
            .did("1")
            .key("")
            .msgId("system-payout-callback")
            .userInfo(systemUser)
            .build();
    }
}
