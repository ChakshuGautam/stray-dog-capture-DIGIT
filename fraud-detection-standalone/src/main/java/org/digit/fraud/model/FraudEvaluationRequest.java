package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudEvaluationRequest {

    private String applicationId;
    private String tenantId;
    private String moduleCode;
    private ApplicantInfo applicantInfo;
    private LocationData locationData;
    private List<EvidenceData> evidences;
    private Map<String, Object> additionalData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApplicantInfo {
        private String applicantId;
        private String userUuid;
        private String mobileNumber;
        private String email;
        private String name;
        private String deviceId;
        private String ipAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationData {
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String address;
        private String locality;
        private String ward;
        private String district;
        private Long timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvidenceData {
        private String fileStoreId;
        private String purpose;
        private String contentType;
        private String contentHash;
        private EvidenceMetadata metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvidenceMetadata {
        private Double gpsLatitude;
        private Double gpsLongitude;
        private Long timestamp;
        private String deviceId;
        private String deviceModel;
        private String osVersion;
        private Boolean exifPresent;
        private Integer width;
        private Integer height;
        private String format;
    }
}
