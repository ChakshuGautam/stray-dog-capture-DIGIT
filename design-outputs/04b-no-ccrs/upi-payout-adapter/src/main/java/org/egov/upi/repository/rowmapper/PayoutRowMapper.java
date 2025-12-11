package org.egov.upi.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.upi.web.models.AuditDetails;
import org.egov.upi.web.models.FundAccount;
import org.egov.upi.web.models.Payout;
import org.egov.upi.web.models.PayoutStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class PayoutRowMapper implements RowMapper<Payout> {

    private final ObjectMapper objectMapper;

    @Autowired
    public PayoutRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Payout mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Map Fund Account
        FundAccount fundAccount = FundAccount.builder()
            .id(rs.getString("fa_id"))
            .accountType(rs.getString("fa_account_type"))
            .vpa(rs.getString("fa_vpa"))
            .bankAccountNumber(rs.getString("fa_bank_account"))
            .ifscCode(rs.getString("fa_ifsc"))
            .beneficiaryName(rs.getString("fa_beneficiary_name"))
            .mobileNumber(rs.getString("fa_mobile"))
            .email(rs.getString("fa_email"))
            .providerFundAccountId(rs.getString("fa_provider_fund_id"))
            .providerContactId(rs.getString("fa_provider_contact_id"))
            .build();

        // Map Audit Details
        AuditDetails auditDetails = AuditDetails.builder()
            .createdBy(rs.getString("created_by"))
            .createdTime(rs.getLong("created_time"))
            .lastModifiedBy(rs.getString("last_modified_by"))
            .lastModifiedTime(rs.getLong("last_modified_time"))
            .build();

        // Parse additional details JSON
        Object additionalDetails = null;
        String additionalDetailsJson = rs.getString("additional_details");
        if (additionalDetailsJson != null) {
            try {
                additionalDetails = objectMapper.readValue(additionalDetailsJson, Object.class);
            } catch (Exception e) {
                log.warn("Failed to parse additional_details JSON", e);
            }
        }

        // Map Payout
        return Payout.builder()
            .id(rs.getString("id"))
            .tenantId(rs.getString("tenant_id"))
            .payoutNumber(rs.getString("payout_number"))
            .referenceId(rs.getString("reference_id"))
            .referenceType(rs.getString("reference_type"))
            .beneficiaryId(rs.getString("beneficiary_id"))
            .beneficiaryType(rs.getString("beneficiary_type"))
            .fundAccount(fundAccount)
            .amount(rs.getBigDecimal("amount"))
            .currency(rs.getString("currency"))
            .mode(rs.getString("mode"))
            .purpose(rs.getString("purpose"))
            .narration(rs.getString("narration"))
            .status(PayoutStatus.fromValue(rs.getString("status")))
            .providerPayoutId(rs.getString("provider_payout_id"))
            .utr(rs.getString("utr"))
            .processedAt(rs.getLong("processed_at") > 0 ? rs.getLong("processed_at") : null)
            .failureReason(rs.getString("failure_reason"))
            .errorCode(rs.getString("error_code"))
            .retryCount(rs.getInt("retry_count"))
            .maxRetries(rs.getInt("max_retries"))
            .additionalDetails(additionalDetails)
            .auditDetails(auditDetails)
            .build();
    }
}
