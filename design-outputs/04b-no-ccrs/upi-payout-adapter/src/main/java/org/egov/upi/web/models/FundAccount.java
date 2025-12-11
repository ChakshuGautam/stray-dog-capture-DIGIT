package org.egov.upi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundAccount {

    @JsonProperty("id")
    private String id;

    @JsonProperty("accountType")
    @Builder.Default
    private String accountType = "vpa";

    @JsonProperty("vpa")
    @NotBlank(message = "UPI VPA is required")
    private String vpa;

    @JsonProperty("bankAccountNumber")
    private String bankAccountNumber;

    @JsonProperty("ifscCode")
    private String ifscCode;

    @JsonProperty("beneficiaryName")
    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("providerFundAccountId")
    private String providerFundAccountId;

    @JsonProperty("providerContactId")
    private String providerContactId;

    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;
}
