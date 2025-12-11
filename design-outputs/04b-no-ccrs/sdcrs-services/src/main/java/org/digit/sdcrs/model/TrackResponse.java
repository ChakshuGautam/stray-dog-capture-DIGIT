package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TrackResponse - Sanitized response for public tracking (no PII).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackResponse {

    @JsonProperty("reportNumber")
    private String reportNumber;

    @JsonProperty("trackingId")
    private String trackingId;

    @JsonProperty("trackingUrl")
    private String trackingUrl;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusDescription")
    private String statusDescription;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("serviceTypeDescription")
    private String serviceTypeDescription;

    // Location - only locality level, no exact coordinates
    @JsonProperty("locality")
    private String locality;

    @JsonProperty("ward")
    private String ward;

    @JsonProperty("city")
    private String city;

    // Timeline of status changes
    @JsonProperty("timeline")
    private List<TimelineEntry> timeline;

    // SLA information
    @JsonProperty("reportedAt")
    private Long reportedAt;

    @JsonProperty("estimatedResolution")
    private Long estimatedResolution;

    @JsonProperty("slaHours")
    private Integer slaHours;

    @JsonProperty("slaDaysRemaining")
    private Integer slaDaysRemaining;

    /**
     * TimelineEntry - Single entry in the report timeline.
     * No PII - only status changes with timestamps.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEntry {

        @JsonProperty("status")
        private String status;

        @JsonProperty("statusDescription")
        private String statusDescription;

        @JsonProperty("timestamp")
        private Long timestamp;

        @JsonProperty("remarks")
        private String remarks;

        // Actor role only, no name/phone
        @JsonProperty("actorRole")
        private String actorRole;
    }
}
