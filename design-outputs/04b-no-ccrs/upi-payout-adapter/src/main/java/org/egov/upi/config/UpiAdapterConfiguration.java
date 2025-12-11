package org.egov.upi.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Data
public class UpiAdapterConfiguration {

    // Provider Configuration
    @Value("${upi.provider.name:RAZORPAY}")
    private String providerName;

    @Value("${upi.provider.keyId}")
    private String providerKeyId;

    @Value("${upi.provider.keySecret}")
    private String providerKeySecret;

    @Value("${upi.provider.baseUrl:https://api.razorpay.com/v1}")
    private String providerBaseUrl;

    @Value("${upi.provider.timeout:30000}")
    private Integer providerTimeout;

    // Payout Configuration
    @Value("${upi.payout.mode:UPI}")
    private String defaultPayoutMode;

    @Value("${upi.payout.purpose:payout}")
    private String defaultPayoutPurpose;

    @Value("${upi.payout.currency:INR}")
    private String defaultCurrency;

    @Value("${upi.payout.maxRetries:3}")
    private Integer maxRetries;

    @Value("${upi.payout.retryDelayMs:5000}")
    private Long retryDelayMs;

    // Number Generation
    @Value("${upi.payout.numberFormat:SDCRS/UPI/[fy:yyyy-yy]/[SEQ]}")
    private String payoutNumberFormat;

    // Webhook Configuration
    @Value("${upi.webhook.secret}")
    private String webhookSecret;

    @Value("${upi.webhook.enabled:true}")
    private Boolean webhookEnabled;

    // Kafka Topics
    @Value("${upi.kafka.topic.create:upi-payout-create}")
    private String createPayoutTopic;

    @Value("${upi.kafka.topic.status:upi-payout-status}")
    private String statusUpdateTopic;

    @Value("${upi.kafka.topic.error:upi-payout-error}")
    private String errorTopic;

    @Value("${upi.kafka.topic.persist:upi-payout-persist}")
    private String persistTopic;

    // SDCRS Callback
    @Value("${sdcrs.callback.topic:sdcrs-payout-callback}")
    private String sdcrsCallbackTopic;

    // Validation
    @Value("${upi.validation.vpaPattern:^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$}")
    private String vpaPattern;

    @Value("${upi.validation.minAmount:1}")
    private Integer minPayoutAmount;

    @Value("${upi.validation.maxAmount:100000}")
    private Integer maxPayoutAmount;

    // Idempotency
    @Value("${upi.idempotency.enabled:true}")
    private Boolean idempotencyEnabled;

    @Value("${upi.idempotency.ttlMinutes:1440}")
    private Integer idempotencyTtlMinutes;

    @PostConstruct
    public void validateConfiguration() {
        if (providerKeyId == null || providerKeyId.isBlank()) {
            throw new IllegalStateException("UPI provider key ID is not configured");
        }
        if (providerKeySecret == null || providerKeySecret.isBlank()) {
            throw new IllegalStateException("UPI provider key secret is not configured");
        }
    }
}
