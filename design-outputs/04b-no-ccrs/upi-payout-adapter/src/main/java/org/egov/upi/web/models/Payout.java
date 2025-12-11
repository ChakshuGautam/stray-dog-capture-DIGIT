package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @JsonProperty("payoutNumber")
    private String payoutNumber;

    @JsonProperty("referenceId")
    @NotBlank(message = "Reference ID (Dog Report ID) is required")
    private String referenceId;

    @JsonProperty("referenceType")
    @Builder.Default
    private String referenceType = "DOG_REPORT";

    @JsonProperty("beneficiaryId")
    @NotBlank(message = "Beneficiary ID (Teacher ID) is required")
    private String beneficiaryId;

    @JsonProperty("beneficiaryType")
    @Builder.Default
    private String beneficiaryType = "INDIVIDUAL";

    @JsonProperty("fundAccount")
    @Valid
    @NotNull(message = "Fund account details are required")
    private FundAccount fundAccount;

    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @JsonProperty("currency")
    @Builder.Default
    private String currency = "INR";

    @JsonProperty("mode")
    @Builder.Default
    private String mode = "UPI";

    @JsonProperty("purpose")
    @Builder.Default
    private String purpose = "payout";

    @JsonProperty("narration")
    private String narration;

    @JsonProperty("status")
    private PayoutStatus status;

    @JsonProperty("providerPayoutId")
    private String providerPayoutId;

    @JsonProperty("utr")
    private String utr;

    @JsonProperty("processedAt")
    private Long processedAt;

    @JsonProperty("failureReason")
    private String failureReason;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("retryCount")
    @Builder.Default
    private Integer retryCount = 0;

    @JsonProperty("maxRetries")
    @Builder.Default
    private Integer maxRetries = 3;

    @JsonProperty("additionalDetails")
    private Object additionalDetails;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
