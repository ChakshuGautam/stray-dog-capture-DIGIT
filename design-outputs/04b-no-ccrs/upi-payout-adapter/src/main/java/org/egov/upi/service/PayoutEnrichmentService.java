package org.egov.upi.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.web.models.AuditDetails;
import org.egov.upi.web.models.Payout;
import org.egov.upi.web.models.PayoutStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class PayoutEnrichmentService {

    private final UpiAdapterConfiguration config;
    private final AtomicLong sequenceCounter = new AtomicLong(0);

    @Autowired
    public PayoutEnrichmentService(UpiAdapterConfiguration config) {
        this.config = config;
    }

    /**
     * Enrich payouts with generated IDs and audit details for creation
     */
    public void enrichForCreate(List<Payout> payouts, RequestInfo requestInfo) {
        String userId = requestInfo.getUserInfo() != null ?
            requestInfo.getUserInfo().getUuid() : "SYSTEM";
        long currentTime = System.currentTimeMillis();

        for (Payout payout : payouts) {
            // Generate UUID
            if (payout.getId() == null) {
                payout.setId(UUID.randomUUID().toString());
            }

            // Generate payout number
            if (payout.getPayoutNumber() == null) {
                payout.setPayoutNumber(generatePayoutNumber(payout.getTenantId()));
            }

            // Set initial status
            payout.setStatus(PayoutStatus.INITIATED);

            // Set defaults
            if (payout.getMode() == null) {
                payout.setMode(config.getDefaultPayoutMode());
            }
            if (payout.getCurrency() == null) {
                payout.setCurrency(config.getDefaultCurrency());
            }
            if (payout.getRetryCount() == null) {
                payout.setRetryCount(0);
            }
            if (payout.getMaxRetries() == null) {
                payout.setMaxRetries(config.getMaxRetries());
            }

            // Generate narration if not provided
            if (payout.getNarration() == null) {
                payout.setNarration(String.format("SDCRS Payout for %s", payout.getReferenceId()));
            }

            // Set audit details
            AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(userId)
                .createdTime(currentTime)
                .lastModifiedBy(userId)
                .lastModifiedTime(currentTime)
                .build();
            payout.setAuditDetails(auditDetails);
        }
    }

    /**
     * Enrich payout for update operations
     */
    public void enrichForUpdate(Payout payout, RequestInfo requestInfo) {
        String userId = requestInfo.getUserInfo() != null ?
            requestInfo.getUserInfo().getUuid() : "SYSTEM";
        long currentTime = System.currentTimeMillis();

        if (payout.getAuditDetails() != null) {
            payout.getAuditDetails().setLastModifiedBy(userId);
            payout.getAuditDetails().setLastModifiedTime(currentTime);
        }
    }

    /**
     * Generate payout number in format: SDCRS/UPI/2024-25/000001
     */
    private String generatePayoutNumber(String tenantId) {
        LocalDate now = LocalDate.now();
        int year = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        String financialYear = String.format("%d-%02d", year, (year + 1) % 100);
        long sequence = sequenceCounter.incrementAndGet();

        return String.format("SDCRS/UPI/%s/%06d", financialYear, sequence);
    }
}
