package org.egov.upi.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.util.PayoutConstants;
import org.egov.upi.web.models.Payout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PayoutValidationService {

    private final UpiAdapterConfiguration config;
    private final Pattern vpaPattern;

    @Autowired
    public PayoutValidationService(UpiAdapterConfiguration config) {
        this.config = config;
        this.vpaPattern = Pattern.compile(config.getVpaPattern());
    }

    /**
     * Validate payouts for creation
     */
    public void validateCreate(List<Payout> payouts) {
        Map<String, String> errors = new HashMap<>();

        for (int i = 0; i < payouts.size(); i++) {
            Payout payout = payouts.get(i);
            String prefix = "payouts[" + i + "].";

            // Validate tenant ID
            if (!StringUtils.hasText(payout.getTenantId())) {
                errors.put(prefix + "tenantId", "Tenant ID is required");
            }

            // Validate reference ID
            if (!StringUtils.hasText(payout.getReferenceId())) {
                errors.put(prefix + "referenceId", "Reference ID (Dog Report ID) is required");
            }

            // Validate beneficiary ID
            if (!StringUtils.hasText(payout.getBeneficiaryId())) {
                errors.put(prefix + "beneficiaryId", "Beneficiary ID (Teacher ID) is required");
            }

            // Validate amount
            if (payout.getAmount() == null) {
                errors.put(prefix + "amount", "Amount is required");
            } else {
                validateAmount(payout.getAmount(), prefix, errors);
            }

            // Validate fund account
            if (payout.getFundAccount() == null) {
                errors.put(prefix + "fundAccount", "Fund account details are required");
            } else {
                validateFundAccount(payout, prefix, errors);
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomException(errors);
        }
    }

    /**
     * Validate amount within allowed range
     */
    private void validateAmount(BigDecimal amount, String prefix, Map<String, String> errors) {
        if (amount.compareTo(BigDecimal.valueOf(config.getMinPayoutAmount())) < 0) {
            errors.put(prefix + "amount",
                "Amount must be at least " + config.getMinPayoutAmount());
        }
        if (amount.compareTo(BigDecimal.valueOf(config.getMaxPayoutAmount())) > 0) {
            errors.put(prefix + "amount",
                "Amount cannot exceed " + config.getMaxPayoutAmount());
        }
    }

    /**
     * Validate fund account (VPA format)
     */
    private void validateFundAccount(Payout payout, String prefix, Map<String, String> errors) {
        var fundAccount = payout.getFundAccount();

        if (!StringUtils.hasText(fundAccount.getBeneficiaryName())) {
            errors.put(prefix + "fundAccount.beneficiaryName", "Beneficiary name is required");
        }

        // For UPI mode, VPA is required
        if ("UPI".equals(payout.getMode())) {
            if (!StringUtils.hasText(fundAccount.getVpa())) {
                errors.put(prefix + "fundAccount.vpa", "UPI VPA is required for UPI mode");
            } else if (!isValidVpa(fundAccount.getVpa())) {
                errors.put(prefix + "fundAccount.vpa",
                    "Invalid UPI VPA format. Expected format: username@bankhandle");
            }
        } else if ("IMPS".equals(payout.getMode()) || "NEFT".equals(payout.getMode())) {
            // For bank transfer modes, account details required
            if (!StringUtils.hasText(fundAccount.getBankAccountNumber())) {
                errors.put(prefix + "fundAccount.bankAccountNumber",
                    "Bank account number is required for " + payout.getMode() + " mode");
            }
            if (!StringUtils.hasText(fundAccount.getIfscCode())) {
                errors.put(prefix + "fundAccount.ifscCode",
                    "IFSC code is required for " + payout.getMode() + " mode");
            }
        }
    }

    /**
     * Validate UPI VPA format
     */
    private boolean isValidVpa(String vpa) {
        if (vpa == null || vpa.isBlank()) {
            return false;
        }
        return vpaPattern.matcher(vpa.trim()).matches();
    }

    /**
     * Check for duplicate payout requests
     */
    public void checkDuplicate(String referenceId, String beneficiaryId) {
        // This would typically check the database for existing payouts
        // For the same referenceId and beneficiaryId combination
        log.debug("Checking duplicate for referenceId: {}, beneficiaryId: {}",
            referenceId, beneficiaryId);
    }
}
