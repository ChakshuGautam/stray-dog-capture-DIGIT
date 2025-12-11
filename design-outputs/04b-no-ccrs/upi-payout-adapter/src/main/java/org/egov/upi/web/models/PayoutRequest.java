package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest {

    @JsonProperty("RequestInfo")
    @NotNull(message = "RequestInfo is required")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("payouts")
    @NotNull(message = "At least one payout is required")
    @Valid
    private List<Payout> payouts;
}
