package org.egov.sdcrs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.sdcrs.config.SDCRSConfiguration;
import org.egov.sdcrs.web.models.DogReport;
import org.egov.sdcrs.web.models.payout.PayoutRequest;
import org.egov.sdcrs.web.models.payout.Payout;
import org.egov.sdcrs.web.models.payout.FundAccount;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to trigger UPI payouts when dog reports reach CAPTURED state.
 *
 * Integration Flow:
 * 1. MC Officer marks report as CAPTURED
 * 2. SDCRS transitions workflow: IN_PROGRESS → CAPTURED
 * 3. This service pushes payout request to Kafka
 * 4. UPI Adapter consumes and processes payment
 * 5. SDCRS receives callback on sdcrs-payout-callback topic
 */
@Service
@Slf4j
public class PayoutTriggerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SDCRSConfiguration config;
    private final ObjectMapper objectMapper;
    private final TeacherService teacherService;

    @Autowired
    public PayoutTriggerService(KafkaTemplate<String, Object> kafkaTemplate,
                                 SDCRSConfiguration config,
                                 ObjectMapper objectMapper,
                                 TeacherService teacherService) {
        this.kafkaTemplate = kafkaTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
        this.teacherService = teacherService;
    }

    /**
     * Triggered when a dog report transitions to CAPTURED state.
     * Creates and pushes payout request to UPI adapter.
     */
    public void triggerPayoutForCapturedReport(DogReport dogReport, RequestInfo requestInfo) {
        log.info("Triggering payout for captured report: {}", dogReport.getReportNumber());

        try {
            // Fetch teacher details (beneficiary)
            var teacher = teacherService.getTeacherById(dogReport.getReporterId(), requestInfo);

            if (teacher == null || teacher.getVpa() == null) {
                log.error("Teacher or VPA not found for report: {}", dogReport.getReportNumber());
                throw new CustomException("TEACHER_VPA_NOT_FOUND",
                    "Teacher VPA is required for payout");
            }

            // Build fund account (VPA details)
            FundAccount fundAccount = FundAccount.builder()
                .accountType("vpa")
                .vpa(teacher.getVpa())
                .beneficiaryName(teacher.getName())
                .mobileNumber(teacher.getMobileNumber())
                .email(teacher.getEmail())
                .build();

            // Build payout request
            Payout payout = Payout.builder()
                .tenantId(dogReport.getTenantId())
                .referenceId(dogReport.getId())
                .referenceType("DOG_REPORT")
                .beneficiaryId(teacher.getId())
                .beneficiaryType("TEACHER")
                .fundAccount(fundAccount)
                .amount(config.getPayoutAmount())  // Default ₹500
                .currency("INR")
                .mode("UPI")
                .purpose("payout")
                .narration(buildNarration(dogReport))
                .additionalDetails(buildAdditionalDetails(dogReport, teacher))
                .build();

            PayoutRequest payoutRequest = PayoutRequest.builder()
                .requestInfo(requestInfo)
                .payouts(Collections.singletonList(payout))
                .build();

            // Push to Kafka for UPI adapter to consume
            kafkaTemplate.send(config.getUpiPayoutCreateTopic(), payoutRequest);

            log.info("Payout request pushed for report: {}, teacher: {}, amount: {}",
                dogReport.getReportNumber(), teacher.getId(), config.getPayoutAmount());

        } catch (Exception e) {
            log.error("Error triggering payout for report: {}", dogReport.getReportNumber(), e);
            throw new CustomException("PAYOUT_TRIGGER_ERROR",
                "Failed to trigger payout: " + e.getMessage());
        }
    }

    private String buildNarration(DogReport dogReport) {
        return String.format("SDCRS Payout for Report %s", dogReport.getReportNumber());
    }

    private Map<String, Object> buildAdditionalDetails(DogReport dogReport, Object teacher) {
        Map<String, Object> details = new HashMap<>();
        details.put("reportNumber", dogReport.getReportNumber());
        details.put("reportType", dogReport.getReportType());
        details.put("capturedAt", System.currentTimeMillis());
        details.put("district", dogReport.getDistrict());
        details.put("locality", dogReport.getLocality());
        return details;
    }
}
