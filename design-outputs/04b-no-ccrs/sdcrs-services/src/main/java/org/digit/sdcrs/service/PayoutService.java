package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PayoutService - Handles teacher payout processing.
 *
 * Flow (as per SDCRS_Payout sequence diagram):
 * 1. Fetch payout configuration from MDMS
 * 2. Retrieve teacher payment details
 * 3. Check monthly payout cap
 * 4. Create demand via Billing Service
 * 5. Handle payment completion events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final RestTemplate restTemplate;
    private final SDCRSConfiguration config;
    private final MDMSService mdmsService;

    // Payout statuses
    public static final String PAYOUT_STATUS_PENDING = "PENDING";
    public static final String PAYOUT_STATUS_PROCESSING = "PROCESSING";
    public static final String PAYOUT_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYOUT_STATUS_CAP_EXCEEDED = "CAP_EXCEEDED";
    public static final String PAYOUT_STATUS_FAILED = "FAILED";

    /**
     * Process payout for a captured dog report.
     * Called when report status changes to CAPTURED.
     */
    public void processPayout(DogReportRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();

        for (DogReport report : request.getDogReports()) {
            if (Boolean.TRUE.equals(report.getPayoutEligible())) {
                processPayoutForReport(requestInfo, report);
            }
        }
    }

    /**
     * Process payout for a single report.
     */
    private void processPayoutForReport(RequestInfo requestInfo, DogReport report) {
        log.info("Processing payout for report: {}", report.getReportNumber());

        try {
            // Step 1: Get payout configuration
            PayoutConfig payoutConfig = getPayoutConfig(report.getTenantId());

            // Step 2: Check monthly cap
            BigDecimal monthlyTotal = getMonthlyPayoutTotal(
                    report.getTenantId(), report.getReporterId());

            BigDecimal remainingAllowance = payoutConfig.getMonthlyCap()
                    .subtract(monthlyTotal);

            if (remainingAllowance.compareTo(payoutConfig.getAmount()) < 0) {
                // Monthly cap exceeded
                log.info("Monthly cap exceeded for reporter: {}", report.getReporterId());
                report.setPayoutStatus(PAYOUT_STATUS_CAP_EXCEEDED);
                return;
            }

            // Step 3: Set payout amount
            report.setPayoutAmount(payoutConfig.getAmount());
            report.setPayoutStatus(PAYOUT_STATUS_PENDING);

            // Step 4: Create demand via Billing Service
            String demandId = createDemand(requestInfo, report, payoutConfig);
            report.setPayoutDemandId(demandId);
            report.setPayoutStatus(PAYOUT_STATUS_PROCESSING);

            log.info("Payout demand created: {} for report: {}",
                    demandId, report.getReportNumber());

        } catch (Exception e) {
            log.error("Error processing payout for report: {}", report.getReportNumber(), e);
            report.setPayoutStatus(PAYOUT_STATUS_FAILED);
        }
    }

    /**
     * Handle payment completion event from Billing Service.
     */
    public void handlePaymentCompletion(String demandId, String status) {
        log.info("Payment completion event: demandId={}, status={}", demandId, status);

        // Update report payout status based on payment result
        // This would typically be handled through a Kafka consumer
    }

    /**
     * Get payout configuration from MDMS.
     */
    private PayoutConfig getPayoutConfig(String tenantId) {
        // Fetch from MDMS - simplified for pseudo-code
        return PayoutConfig.builder()
                .amount(new BigDecimal("500"))     // Rs. 500 per capture
                .monthlyCap(new BigDecimal("5000")) // Rs. 5000 monthly cap
                .dailyLimit(5)                      // 5 reports per day
                .taxHeadCode("SDCRS_PAYOUT")
                .businessService("SDCRS")
                .build();
    }

    /**
     * Get total payout for a reporter in current month.
     */
    private BigDecimal getMonthlyPayoutTotal(String tenantId, String reporterId) {
        // Query database for completed payouts in current month
        // Simplified for pseudo-code
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        Long fromDate = startOfMonth.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        Long toDate = System.currentTimeMillis();

        // This would be a repository call
        // return repository.getPayoutTotal(tenantId, reporterId, fromDate, toDate, PAYOUT_STATUS_COMPLETED);

        return BigDecimal.ZERO; // Placeholder
    }

    /**
     * Create demand in Billing Service.
     */
    private String createDemand(RequestInfo requestInfo, DogReport report,
                               PayoutConfig payoutConfig) {
        String url = config.getBillingHost() + config.getDemandCreatePath();

        Map<String, Object> demandRequest = new HashMap<>();
        demandRequest.put("RequestInfo", requestInfo);
        demandRequest.put("Demands", List.of(
                Map.of(
                        "tenantId", report.getTenantId(),
                        "consumerCode", report.getReportNumber(),
                        "consumerType", "SDCRS_PAYOUT",
                        "businessService", payoutConfig.getBusinessService(),
                        "payer", Map.of(
                                "uuid", report.getReporterId(),
                                "name", report.getReporterName(),
                                "mobileNumber", report.getReporterPhone()
                        ),
                        "demandDetails", List.of(
                                Map.of(
                                        "taxHeadMasterCode", payoutConfig.getTaxHeadCode(),
                                        "taxAmount", payoutConfig.getAmount()
                                )
                        )
                )
        ));

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    url, demandRequest, Map.class);

            if (response != null && response.containsKey("Demands")) {
                List<Map<String, Object>> demands = (List<Map<String, Object>>) response.get("Demands");
                if (!demands.isEmpty()) {
                    return (String) demands.get(0).get("id");
                }
            }
        } catch (Exception e) {
            log.error("Error creating demand", e);
            throw new CustomException("DEMAND_CREATE_ERROR", "Error creating demand: " + e.getMessage());
        }

        throw new CustomException("DEMAND_CREATE_ERROR", "Failed to create demand");
    }

    /**
     * Payout configuration model.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PayoutConfig {
        private BigDecimal amount;
        private BigDecimal monthlyCap;
        private Integer dailyLimit;
        private String taxHeadCode;
        private String businessService;
    }
}
