package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {

    @JsonProperty("entity")
    private String entity;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("event")
    private String event;

    @JsonProperty("contains")
    private String[] contains;

    @JsonProperty("payload")
    private WebhookPayloadData payload;

    @JsonProperty("created_at")
    private Long createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookPayloadData {

        @JsonProperty("payout")
        private PayoutEntity payout;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayoutEntity {

        @JsonProperty("entity")
        private PayoutDetails entity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayoutDetails {

        @JsonProperty("id")
        private String id;

        @JsonProperty("entity")
        private String entity;

        @JsonProperty("fund_account_id")
        private String fundAccountId;

        @JsonProperty("amount")
        private Long amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("notes")
        private Map<String, String> notes;

        @JsonProperty("fees")
        private Long fees;

        @JsonProperty("tax")
        private Long tax;

        @JsonProperty("status")
        private String status;

        @JsonProperty("utr")
        private String utr;

        @JsonProperty("mode")
        private String mode;

        @JsonProperty("purpose")
        private String purpose;

        @JsonProperty("reference_id")
        private String referenceId;

        @JsonProperty("narration")
        private String narration;

        @JsonProperty("batch_id")
        private String batchId;

        @JsonProperty("failure_reason")
        private String failureReason;

        @JsonProperty("created_at")
        private Long createdAt;

        @JsonProperty("processed_at")
        private Long processedAt;

        @JsonProperty("error")
        private ErrorDetails error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetails {

        @JsonProperty("source")
        private String source;

        @JsonProperty("reason")
        private String reason;

        @JsonProperty("description")
        private String description;
    }
}
