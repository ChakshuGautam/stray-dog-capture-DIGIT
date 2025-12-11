package org.digit.sdcrs.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.Location;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DogReportRowMapper - Maps database rows to DogReport objects.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DogReportRowMapper implements RowMapper<DogReport> {

    private final ObjectMapper objectMapper;

    @Override
    public DogReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Build location
        Location location = Location.builder()
                .id(rs.getString("loc_id"))
                .latitude(getBigDecimal(rs, "latitude"))
                .longitude(getBigDecimal(rs, "longitude"))
                .address(rs.getString("address"))
                .locality(rs.getString("locality"))
                .localityCode(rs.getString("locality_code"))
                .ward(rs.getString("ward"))
                .wardCode(rs.getString("ward_code"))
                .district(rs.getString("district"))
                .districtCode(rs.getString("district_code"))
                .city(rs.getString("city"))
                .pincode(rs.getString("pincode"))
                .gpsLatitude(getBigDecimal(rs, "gps_latitude"))
                .gpsLongitude(getBigDecimal(rs, "gps_longitude"))
                .gpsAccuracy(getBigDecimal(rs, "gps_accuracy"))
                .gpsTimestamp(getLong(rs, "gps_timestamp"))
                .gpsValidated(rs.getBoolean("gps_validated"))
                .gpsValidationResult(rs.getString("gps_validation_result"))
                .build();

        // Build audit details
        AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(rs.getString("createdby"))
                .createdTime(rs.getLong("createdtime"))
                .lastModifiedBy(rs.getString("lastmodifiedby"))
                .lastModifiedTime(rs.getLong("lastmodifiedtime"))
                .build();

        // Parse additional details JSON
        Object additionalDetails = null;
        String additionalDetailsJson = rs.getString("additional_details");
        if (additionalDetailsJson != null) {
            try {
                additionalDetails = objectMapper.readValue(additionalDetailsJson, Object.class);
            } catch (Exception e) {
                log.error("Error parsing additional details", e);
            }
        }

        // Build dog report
        return DogReport.builder()
                .id(rs.getString("id"))
                .tenantId(rs.getString("tenant_id"))
                .reportNumber(rs.getString("report_number"))
                .trackingId(rs.getString("tracking_id"))
                .trackingUrl(rs.getString("tracking_url"))
                .serviceType(rs.getString("service_type"))
                .description(rs.getString("description"))
                .status(rs.getString("status"))
                .location(location)
                // Reporter
                .reporterId(rs.getString("reporter_id"))
                .reporterName(rs.getString("reporter_name"))
                .reporterPhone(rs.getString("reporter_phone"))
                .reporterSchool(rs.getString("reporter_school"))
                // Assignment
                .assignedOfficerId(rs.getString("assigned_officer_id"))
                .assignedOfficerName(rs.getString("assigned_officer_name"))
                .assignedAt(getLong(rs, "assigned_at"))
                // Verification
                .verifiedBy(rs.getString("verified_by"))
                .verifiedAt(getLong(rs, "verified_at"))
                .verificationRemarks(rs.getString("verification_remarks"))
                // Resolution
                .resolutionType(rs.getString("resolution_type"))
                .resolutionRemarks(rs.getString("resolution_remarks"))
                .resolvedAt(getLong(rs, "resolved_at"))
                .capturePhotoFileStoreId(rs.getString("capture_photo_file_store_id"))
                // Payout
                .payoutEligible(rs.getBoolean("payout_eligible"))
                .payoutAmount(getBigDecimal(rs, "payout_amount"))
                .payoutStatus(rs.getString("payout_status"))
                .payoutDemandId(rs.getString("payout_demand_id"))
                // Rejection
                .rejectionReason(rs.getString("rejection_reason"))
                .rejectionRemarks(rs.getString("rejection_remarks"))
                // Duplicate detection
                .imageHash(rs.getString("image_hash"))
                // Audit
                .auditDetails(auditDetails)
                .additionalDetails(additionalDetails)
                .build();
    }

    private BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return rs.wasNull() ? null : value;
    }

    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
