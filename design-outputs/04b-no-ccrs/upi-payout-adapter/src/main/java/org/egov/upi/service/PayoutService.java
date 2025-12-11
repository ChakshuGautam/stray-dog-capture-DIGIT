package org.egov.upi.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.external.RazorpayPayoutClient;
import org.egov.upi.kafka.PayoutProducer;
import org.egov.upi.repository.PayoutRepository;
import org.egov.upi.util.PayoutConstants;
import org.egov.upi.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PayoutService {

    private final PayoutValidationService validationService;
    private final PayoutEnrichmentService enrichmentService;
    private final RazorpayPayoutClient razorpayClient;
    private final PayoutRepository repository;
    private final PayoutProducer producer;
    private final UpiAdapterConfiguration config;

    @Autowired
    public PayoutService(PayoutValidationService validationService,
                         PayoutEnrichmentService enrichmentService,
                         RazorpayPayoutClient razorpayClient,
                         PayoutRepository repository,
                         PayoutProducer producer,
                         UpiAdapterConfiguration config) {
        this.validationService = validationService;
        this.enrichmentService = enrichmentService;
        this.razorpayClient = razorpayClient;
        this.repository = repository;
        this.producer = producer;
        this.config = config;
    }

    /**
     * Create new payout requests
     */
    @Transactional
    public List<Payout> createPayouts(PayoutRequest request) {
        log.info("Creating {} payout(s)", request.getPayouts().size());

        List<Payout> payouts = request.getPayouts();
        RequestInfo requestInfo = request.getRequestInfo();

        // Validate
        validationService.validateCreate(payouts);

        // Check for duplicates
        for (Payout payout : payouts) {
            validationService.checkDuplicate(payout.getReferenceId(), payout.getBeneficiaryId());
        }

        // Enrich
        enrichmentService.enrichForCreate(payouts, requestInfo);

        // Process each payout with provider
        for (Payout payout : payouts) {
            processPayoutWithProvider(payout);
        }

        // Persist via Kafka
        producer.pushToPersist(payouts);

        log.info("Successfully created {} payout(s)", payouts.size());
        return payouts;
    }

    /**
     * Process payout with Razorpay
     */
    private void processPayoutWithProvider(Payout payout) {
        try {
            FundAccount fundAccount = payout.getFundAccount();

            // Step 1: Create contact (if not exists)
            String contactId = fundAccount.getProviderContactId();
            if (contactId == null) {
                contactId = razorpayClient.createContact(
                    fundAccount.getBeneficiaryName(),
                    fundAccount.getEmail(),
                    fundAccount.getMobileNumber(),
                    payout.getBeneficiaryId()
                );
                fundAccount.setProviderContactId(contactId);
            }

            // Step 2: Create fund account (if not exists)
            String fundAccountId = fundAccount.getProviderFundAccountId();
            if (fundAccountId == null) {
                fundAccountId = razorpayClient.createFundAccount(
                    contactId,
                    fundAccount.getVpa(),
                    fundAccount.getBeneficiaryName()
                );
                fundAccount.setProviderFundAccountId(fundAccountId);
            }

            // Step 3: Create payout
            Map<String, String> notes = new HashMap<>();
            notes.put("tenant_id", payout.getTenantId());
            notes.put("reference_id", payout.getReferenceId());
            notes.put("beneficiary_id", payout.getBeneficiaryId());
            notes.put("payout_number", payout.getPayoutNumber());

            RazorpayPayoutClient.PayoutResult result = razorpayClient.createPayout(
                fundAccountId,
                payout.getAmount(),
                payout.getMode(),
                config.getDefaultPayoutPurpose(),
                payout.getNarration(),
                payout.getPayoutNumber(),
                notes
            );

            // Update payout with provider response
            payout.setProviderPayoutId(result.getPayoutId());
            payout.setStatus(mapProviderStatus(result.getStatus()));
            if (result.getUtr() != null) {
                payout.setUtr(result.getUtr());
            }

            log.info("Payout {} created with provider. Status: {}",
                payout.getPayoutNumber(), payout.getStatus());

        } catch (Exception e) {
            log.error("Error processing payout {} with provider: {}",
                payout.getPayoutNumber(), e.getMessage());
            payout.setStatus(PayoutStatus.FAILED);
            payout.setFailureReason(e.getMessage());
            payout.setErrorCode(PayoutConstants.ERR_PROVIDER_ERROR);
        }
    }

    /**
     * Search payouts
     */
    public List<Payout> searchPayouts(PayoutSearchRequest request) {
        log.info("Searching payouts with criteria: {}", request.getCriteria());
        return repository.search(request.getCriteria());
    }

    /**
     * Update payout status (called from webhook or status polling)
     */
    @Transactional
    public Payout updatePayoutStatus(String payoutId, String providerStatus,
                                      String utr, String failureReason,
                                      RequestInfo requestInfo) {
        log.info("Updating payout status for provider ID: {}", payoutId);

        // Find payout by provider ID
        PayoutSearchCriteria criteria = PayoutSearchCriteria.builder()
            .ids(List.of(payoutId))
            .build();
        List<Payout> payouts = repository.searchByProviderPayoutId(payoutId);

        if (payouts.isEmpty()) {
            throw new CustomException(PayoutConstants.ERR_PAYOUT_NOT_FOUND,
                "Payout not found for provider ID: " + payoutId);
        }

        Payout payout = payouts.get(0);
        PayoutStatus oldStatus = payout.getStatus();
        PayoutStatus newStatus = mapProviderStatus(providerStatus);

        // Update status
        payout.setStatus(newStatus);
        if (utr != null) {
            payout.setUtr(utr);
            payout.setProcessedAt(System.currentTimeMillis());
        }
        if (failureReason != null) {
            payout.setFailureReason(failureReason);
        }

        enrichmentService.enrichForUpdate(payout, requestInfo);

        // Persist update
        producer.pushToPersist(List.of(payout));

        // Notify SDCRS of status change
        if (oldStatus != newStatus) {
            notifyStatusChange(payout);
        }

        log.info("Payout {} status updated: {} -> {}",
            payout.getPayoutNumber(), oldStatus, newStatus);

        return payout;
    }

    /**
     * Retry a failed payout
     */
    @Transactional
    public Payout retryPayout(String payoutId, RequestInfo requestInfo) {
        PayoutSearchCriteria criteria = PayoutSearchCriteria.builder()
            .ids(List.of(payoutId))
            .build();
        List<Payout> payouts = repository.search(criteria);

        if (payouts.isEmpty()) {
            throw new CustomException(PayoutConstants.ERR_PAYOUT_NOT_FOUND,
                "Payout not found: " + payoutId);
        }

        Payout payout = payouts.get(0);

        if (payout.getStatus() != PayoutStatus.FAILED) {
            throw new CustomException("INVALID_STATUS",
                "Only failed payouts can be retried");
        }

        if (payout.getRetryCount() >= payout.getMaxRetries()) {
            throw new CustomException("MAX_RETRIES_EXCEEDED",
                "Maximum retry attempts exceeded");
        }

        // Increment retry count
        payout.setRetryCount(payout.getRetryCount() + 1);
        payout.setStatus(PayoutStatus.INITIATED);
        payout.setFailureReason(null);
        payout.setErrorCode(null);

        enrichmentService.enrichForUpdate(payout, requestInfo);

        // Retry with provider
        processPayoutWithProvider(payout);

        // Persist
        producer.pushToPersist(List.of(payout));

        return payout;
    }

    /**
     * Map provider status to internal status
     */
    private PayoutStatus mapProviderStatus(String providerStatus) {
        if (providerStatus == null) {
            return PayoutStatus.INITIATED;
        }

        return switch (providerStatus.toLowerCase()) {
            case "queued", "pending" -> PayoutStatus.INITIATED;
            case "processing" -> PayoutStatus.PROCESSING;
            case "processed" -> PayoutStatus.COMPLETED;
            case "reversed" -> PayoutStatus.REVERSED;
            case "failed", "cancelled", "rejected" -> PayoutStatus.FAILED;
            default -> PayoutStatus.PROCESSING;
        };
    }

    /**
     * Notify SDCRS service of payout status change
     */
    private void notifyStatusChange(Payout payout) {
        // Push to SDCRS callback topic
        producer.pushToSdcrsCallback(payout);
    }
}
