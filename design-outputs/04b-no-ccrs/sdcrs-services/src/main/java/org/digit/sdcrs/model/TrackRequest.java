package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

/**
 * TrackRequest - Request for public tracking endpoint (no auth required).
 * Must provide either reportNumber OR trackingId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackRequest {

    @Size(max = 64)
    @JsonProperty("tenantId")
    private String tenantId;

    @Size(max = 64)
    @JsonProperty("reportNumber")
    private String reportNumber;

    @Size(max = 8)
    @JsonProperty("trackingId")
    private String trackingId;

    @Size(max = 10)
    @JsonProperty("locale")
    private String locale;

    public boolean isValid() {
        return (reportNumber != null && !reportNumber.isEmpty()) ||
               (trackingId != null && !trackingId.isEmpty());
    }
}
