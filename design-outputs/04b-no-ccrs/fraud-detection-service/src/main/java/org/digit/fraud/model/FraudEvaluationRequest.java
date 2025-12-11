package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;
import java.util.Map;

/**
 * FraudEvaluationRequest - Generic request for fraud evaluation.
 * Can be used by any DIGIT module (SDCRS, TL, PGR, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvaluationRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    private String tenantId;

    /**
     * Source module requesting evaluation (e.g., "SDCRS", "TL", "PGR")
     */
    private String sourceModule;

    /**
     * Business ID of the entity being evaluated (e.g., report number, application number)
     */
    private String businessId;

    /**
     * Type of entity (e.g., "DOG_REPORT", "TRADE_LICENSE", "COMPLAINT")
     */
    private String entityType;

    /**
     * Applicant/user information for velocity and pattern checks
     */
    private ApplicantInfo applicant;

    /**
     * Location data for geo-based validation
     */
    private LocationData locationData;

    /**
     * Evidence/attachments for image-based validation
     */
    private List<EvidenceData> evidences;

    /**
     * Additional context-specific data
     */
    private Map<String, Object> additionalData;

    /**
     * Which rule categories to evaluate (null = all)
     */
    private List<String> ruleCategories;

    /**
     * Whether to run only INTERNAL rules (faster, synchronous)
     */
    private boolean internalOnly;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantInfo {
        private String id;
        private String name;
        private String mobileNumber;
        private String email;
        private String profilePhotoId;
        private Integer recentSubmissionCount;
        private Long avgIntervalMinutes;
        private List<LocationData> recentLocations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private Double latitude;
        private Double longitude;
        private Double altitude;
        private Double accuracy;
        private Double speed;
        private Double bearing;
        private Long timestamp;
        private String localityCode;
        private String boundaryCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceData {
        private String fileStoreId;
        private String purpose;  // DOG_PHOTO, SELFIE, DOCUMENT, etc.
        private String imageHash;
        private Double latitude;
        private Double longitude;
        private Long captureTime;
        private Map<String, Object> metadata;
    }
}
