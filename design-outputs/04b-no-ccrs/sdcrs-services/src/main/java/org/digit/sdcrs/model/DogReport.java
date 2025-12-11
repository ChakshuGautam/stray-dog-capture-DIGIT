package org.digit.sdcrs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.models.Workflow;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * DogReport - Main entity for stray dog capture reports.
 * Maps to eg_sdcrs_report table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DogReport {

    @JsonProperty("id")
    private String id;

    @NotNull
    @Size(max = 64)
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("reportNumber")
    private String reportNumber;

    @Size(max = 8)
    @JsonProperty("trackingId")
    private String trackingId;

    @Size(max = 256)
    @JsonProperty("trackingUrl")
    private String trackingUrl;

    @NotNull
    @Size(max = 64)
    @JsonProperty("serviceType")
    private String serviceType;  // STRAY_DOG_AGGRESSIVE, STRAY_DOG_INJURED, etc.

    @Size(max = 512)
    @JsonProperty("description")
    private String description;

    @Valid
    @NotNull
    @JsonProperty("location")
    private Location location;

    @Valid
    @JsonProperty("evidence")
    private List<Evidence> evidence;

    // Reporter details (enriched from User service)
    @JsonProperty("reporterId")
    private String reporterId;

    @JsonProperty("reporterName")
    private String reporterName;

    @JsonProperty("reporterPhone")
    private String reporterPhone;

    @JsonProperty("reporterSchool")
    private String reporterSchool;

    // Assignment details
    @JsonProperty("assignedOfficerId")
    private String assignedOfficerId;

    @JsonProperty("assignedOfficerName")
    private String assignedOfficerName;

    @JsonProperty("assignedAt")
    private Long assignedAt;

    // Verification details
    @JsonProperty("verifiedBy")
    private String verifiedBy;

    @JsonProperty("verifiedAt")
    private Long verifiedAt;

    @JsonProperty("verificationRemarks")
    private String verificationRemarks;

    // Resolution details
    @JsonProperty("resolutionType")
    private String resolutionType;  // CAPTURED, UNABLE_TO_LOCATE, etc.

    @JsonProperty("resolutionRemarks")
    private String resolutionRemarks;

    @JsonProperty("resolvedAt")
    private Long resolvedAt;

    @JsonProperty("capturePhotoFileStoreId")
    private String capturePhotoFileStoreId;

    // Payout details
    @JsonProperty("payoutEligible")
    private Boolean payoutEligible;

    @JsonProperty("payoutAmount")
    private BigDecimal payoutAmount;

    @JsonProperty("payoutStatus")
    private String payoutStatus;  // PENDING, PROCESSING, COMPLETED, CAP_EXCEEDED, FAILED

    @JsonProperty("payoutDemandId")
    private String payoutDemandId;

    // Rejection details
    @JsonProperty("rejectionReason")
    private String rejectionReason;

    @JsonProperty("rejectionRemarks")
    private String rejectionRemarks;

    // Image hash for duplicate detection
    @JsonProperty("imageHash")
    private String imageHash;

    // Workflow status
    @NotNull
    @JsonProperty("status")
    private String status;

    @JsonProperty("workflow")
    private Workflow workflow;

    // Audit fields
    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    // Additional data as JSONB
    @JsonProperty("additionalDetails")
    private Object additionalDetails;
}
