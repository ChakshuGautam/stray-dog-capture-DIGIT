package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutSearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("payoutNumbers")
    private List<String> payoutNumbers;

    @JsonProperty("referenceIds")
    private List<String> referenceIds;

    @JsonProperty("beneficiaryIds")
    private List<String> beneficiaryIds;

    @JsonProperty("status")
    private List<PayoutStatus> status;

    @JsonProperty("fromDate")
    private Long fromDate;

    @JsonProperty("toDate")
    private Long toDate;

    @JsonProperty("limit")
    @Builder.Default
    private Integer limit = 100;

    @JsonProperty("offset")
    @Builder.Default
    private Integer offset = 0;

    @JsonProperty("sortBy")
    private String sortBy;

    @JsonProperty("sortOrder")
    @Builder.Default
    private String sortOrder = "DESC";
}
