package org.digit.sdcrs.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * SDCRSConfiguration - Centralized configuration properties for SDCRS service.
 * All external service URLs, Kafka topics, and business rules are configured here.
 */
@Data
@Component
@Configuration
public class SDCRSConfiguration {

    // ========== DIGIT Core Service Hosts ==========

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;

    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.workflow.host}")
    private String workflowHost;

    @Value("${egov.workflow.transition.path}")
    private String workflowTransitionPath;

    @Value("${egov.workflow.process.path}")
    private String workflowProcessPath;

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchPath;

    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.search.path}")
    private String localizationSearchPath;

    @Value("${egov.filestore.host}")
    private String filestoreHost;

    @Value("${egov.filestore.upload.path}")
    private String filestoreUploadPath;

    @Value("${egov.filestore.search.path}")
    private String filestoreSearchPath;

    @Value("${egov.notification.host}")
    private String notificationHost;

    @Value("${egov.notification.sms.path}")
    private String smsPath;

    @Value("${egov.billing.host}")
    private String billingHost;

    @Value("${egov.billing.demand.create.path}")
    private String demandCreatePath;

    @Value("${egov.billing.demand.search.path}")
    private String demandSearchPath;

    @Value("${egov.url.shortener.host}")
    private String urlShortenerHost;

    @Value("${egov.url.shortener.path}")
    private String urlShortenerPath;

    // ========== IDGen Configuration ==========

    @Value("${sdcrs.idgen.report.name}")
    private String reportIdGenName;

    @Value("${sdcrs.idgen.report.format}")
    private String reportIdGenFormat;

    @Value("${sdcrs.idgen.tracking.name}")
    private String trackingIdGenName;

    @Value("${sdcrs.idgen.tracking.format}")
    private String trackingIdGenFormat;

    // ========== Workflow Configuration ==========

    @Value("${sdcrs.workflow.business.service}")
    private String workflowBusinessService;

    @Value("${sdcrs.workflow.module.name}")
    private String workflowModuleName;

    // ========== Kafka Topics ==========

    @Value("${sdcrs.kafka.create.topic}")
    private String createTopic;

    @Value("${sdcrs.kafka.update.topic}")
    private String updateTopic;

    @Value("${sdcrs.kafka.validation.topic}")
    private String validationTopic;

    @Value("${sdcrs.kafka.notification.topic}")
    private String notificationTopic;

    @Value("${sdcrs.kafka.payout-trigger.topic}")
    private String payoutTriggerTopic;

    @Value("${sdcrs.kafka.payout-update.topic}")
    private String payoutUpdateTopic;

    @Value("${sdcrs.kafka.payment-completed.topic}")
    private String paymentCompletedTopic;

    // ========== Validation Settings ==========

    @Value("${sdcrs.validation.gps.tolerance.meters:100}")
    private Double gpsToleranceMeters;

    @Value("${sdcrs.validation.image.max.age.hours:48}")
    private Integer imageMaxAgeHours;

    @Value("${sdcrs.validation.duplicate.threshold:0.90}")
    private Double duplicateThreshold;

    @Value("${sdcrs.validation.daily.report.limit:5}")
    private Integer dailyReportLimit;

    // ========== Payout Configuration ==========

    @Value("${sdcrs.payout.amount:500}")
    private BigDecimal payoutAmount;

    @Value("${sdcrs.payout.monthly.cap:5000}")
    private BigDecimal payoutMonthlyCap;

    @Value("${sdcrs.payout.tax.head.code}")
    private String payoutTaxHeadCode;

    @Value("${sdcrs.payout.business.service}")
    private String payoutBusinessService;

    // ========== Tracking Configuration ==========

    @Value("${sdcrs.tracking.url.base}")
    private String trackingUrlBase;

    @Value("${sdcrs.tracking.short.url.enabled:true}")
    private Boolean trackingShortUrlEnabled;

    // ========== Search Configuration ==========

    @Value("${sdcrs.search.max.limit:100}")
    private Integer searchMaxLimit;

    @Value("${sdcrs.search.default.limit:10}")
    private Integer searchDefaultLimit;

    @Value("${sdcrs.search.default.offset:0}")
    private Integer searchDefaultOffset;

    // ========== MDMS Configuration ==========

    @Value("${sdcrs.mdms.module.name:SDCRS}")
    private String mdmsModuleName;

    // ========== Fraud Prevention (Legacy - to be replaced by MDMS) ==========

    @Value("${sdcrs.fraud.cooldown.days:7}")
    @Deprecated // Use FraudRules.json from MDMS instead
    private Integer fraudCooldownDays;

    @Value("${sdcrs.fraud.suspension.days:30}")
    @Deprecated // Use FraudRules.json from MDMS instead
    private Integer fraudSuspensionDays;

    @Value("${sdcrs.fraud.warning.threshold:3}")
    @Deprecated // Use FraudRules.json from MDMS instead
    private Integer fraudWarningThreshold;

    // ========== Fraud Detection Service (External Validators) ==========

    @Value("${sdcrs.fraud.ai.service.url:}")
    private String aiServiceUrl;

    @Value("${sdcrs.fraud.yolo.service.url:}")
    private String yoloServiceUrl;

    @Value("${sdcrs.fraud.face.api.url:}")
    private String faceApiUrl;

    @Value("${sdcrs.fraud.iqa.service.url:}")
    private String iqaServiceUrl;

    @Value("${sdcrs.fraud.anomaly.service.url:}")
    private String anomalyServiceUrl;

    @Value("${sdcrs.fraud.gps.validation.url:}")
    private String gpsValidationUrl;

    @Value("${sdcrs.fraud.external.timeout.connect.ms:5000}")
    private Integer externalTimeoutConnectMs;

    @Value("${sdcrs.fraud.external.timeout.read.ms:30000}")
    private Integer externalTimeoutReadMs;

    @Value("${sdcrs.fraud.external.retry.max:2}")
    private Integer externalRetryMax;

    @Value("${sdcrs.fraud.circuit.breaker.threshold:5}")
    private Integer circuitBreakerThreshold;

    @Value("${sdcrs.fraud.circuit.breaker.reset.ms:60000}")
    private Integer circuitBreakerResetMs;

    // ========== MDMS Paths ==========

    public String getMdmsHost() {
        return mdmsHost;
    }

    public String getMdmsSearchPath() {
        return mdmsSearchEndpoint;
    }

    // ========== Timezone ==========

    @Value("${app.timezone:Asia/Kolkata}")
    private String timezone;
}
