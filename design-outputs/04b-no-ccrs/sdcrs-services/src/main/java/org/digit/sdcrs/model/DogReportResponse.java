package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

/**
 * DogReportResponse - Response wrapper for dog report API operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DogReportResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("DogReports")
    private List<DogReport> dogReports;

    @JsonProperty("TotalCount")
    private Integer totalCount;
}
