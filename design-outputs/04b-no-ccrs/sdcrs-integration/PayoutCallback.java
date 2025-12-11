package org.egov.sdcrs.web.models.payout;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Callback model received from UPI Payout Adapter.
 * Pushed to sdcrs-payout-callback Kafka topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutCallback {

    @JsonProperty("payoutId")
    private String payoutId;

    @JsonProperty("payoutNumber")
    private String payoutNumber;

    @JsonProperty("referenceId")
    private String referenceId;  // Dog Report ID

    @JsonProperty("referenceType")
    private String referenceType;

    @JsonProperty("beneficiaryId")
    private String beneficiaryId;  // Teacher ID

    @JsonProperty("status")
    private String status;  // COMPLETED, FAILED, REVERSED

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("utr")
    private String utr;  // Unique Transaction Reference (for successful payments)

    @JsonProperty("providerPayoutId")
    private String providerPayoutId;  // Razorpay payout ID

    @JsonProperty("processedAt")
    private Long processedAt;

    @JsonProperty("failureReason")
    private String failureReason;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("retryCount")
    private Integer retryCount;

    @JsonProperty("tenantId")
    private String tenantId;
}
