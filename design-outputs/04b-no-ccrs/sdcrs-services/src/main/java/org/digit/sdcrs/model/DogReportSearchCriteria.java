package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * DogReportSearchCriteria - Search criteria for querying dog reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DogReportSearchCriteria {

    @Size(max = 64)
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("ids")
    private List<String> ids;

    @Size(max = 64)
    @JsonProperty("reportNumber")
    private String reportNumber;

    @Size(max = 8)
    @JsonProperty("trackingId")
    private String trackingId;

    @JsonProperty("status")
    private List<String> status;

    @Size(max = 64)
    @JsonProperty("serviceType")
    private String serviceType;

    @Size(max = 64)
    @JsonProperty("reporterId")
    private String reporterId;

    @Size(max = 64)
    @JsonProperty("assignedOfficerId")
    private String assignedOfficerId;

    @Size(max = 64)
    @JsonProperty("localityCode")
    private String localityCode;

    @Size(max = 64)
    @JsonProperty("wardCode")
    private String wardCode;

    @Size(max = 64)
    @JsonProperty("districtCode")
    private String districtCode;

    @JsonProperty("fromDate")
    private Long fromDate;

    @JsonProperty("toDate")
    private Long toDate;

    @JsonProperty("payoutEligible")
    private Boolean payoutEligible;

    @JsonProperty("payoutStatus")
    private String payoutStatus;

    // Pagination
    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("sortBy")
    private String sortBy;

    @JsonProperty("sortOrder")
    private SortOrder sortOrder;

    public enum SortOrder {
        ASC, DESC
    }

    public boolean isEmpty() {
        return (ids == null || ids.isEmpty()) &&
               reportNumber == null &&
               trackingId == null &&
               (status == null || status.isEmpty()) &&
               serviceType == null &&
               reporterId == null &&
               assignedOfficerId == null &&
               localityCode == null &&
               wardCode == null &&
               districtCode == null &&
               fromDate == null &&
               toDate == null;
    }
}
