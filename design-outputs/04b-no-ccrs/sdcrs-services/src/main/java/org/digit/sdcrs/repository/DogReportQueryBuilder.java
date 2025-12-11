package org.digit.sdcrs.repository;

import org.digit.sdcrs.model.DogReportSearchCriteria;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DogReportQueryBuilder - Builds SQL queries for dog report searches.
 */
@Component
public class DogReportQueryBuilder {

    public static final String BASE_QUERY = """
        SELECT r.id, r.tenant_id, r.report_number, r.tracking_id, r.tracking_url,
               r.service_type, r.description, r.status,
               r.reporter_id, r.reporter_name, r.reporter_phone, r.reporter_school,
               r.assigned_officer_id, r.assigned_officer_name, r.assigned_at,
               r.verified_by, r.verified_at, r.verification_remarks,
               r.resolution_type, r.resolution_remarks, r.resolved_at,
               r.capture_photo_file_store_id,
               r.payout_eligible, r.payout_amount, r.payout_status, r.payout_demand_id,
               r.rejection_reason, r.rejection_remarks, r.image_hash,
               r.additional_details,
               r.createdby, r.createdtime, r.lastmodifiedby, r.lastmodifiedtime,
               l.id as loc_id, l.latitude, l.longitude, l.address, l.locality,
               l.locality_code, l.ward, l.ward_code, l.district, l.district_code,
               l.city, l.pincode, l.gps_latitude, l.gps_longitude, l.gps_accuracy,
               l.gps_timestamp, l.gps_validated, l.gps_validation_result
        FROM eg_sdcrs_report r
        LEFT JOIN eg_sdcrs_location l ON r.id = l.report_id
        """;

    private static final String COUNT_QUERY = """
        SELECT COUNT(DISTINCT r.id)
        FROM eg_sdcrs_report r
        LEFT JOIN eg_sdcrs_location l ON r.id = l.report_id
        """;

    /**
     * Build search query with criteria.
     */
    public String buildSearchQuery(DogReportSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        List<String> conditions = buildConditions(criteria, params);

        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Add ORDER BY
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "createdtime";
        String sortOrder = criteria.getSortOrder() != null ?
                criteria.getSortOrder().name() : "DESC";
        query.append(" ORDER BY r.").append(sortBy).append(" ").append(sortOrder);

        // Add pagination
        if (criteria.getLimit() != null) {
            query.append(" LIMIT :limit");
            params.addValue("limit", criteria.getLimit());
        }
        if (criteria.getOffset() != null) {
            query.append(" OFFSET :offset");
            params.addValue("offset", criteria.getOffset());
        }

        return query.toString();
    }

    /**
     * Build count query with criteria.
     */
    public String buildCountQuery(DogReportSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder query = new StringBuilder(COUNT_QUERY);
        List<String> conditions = buildConditions(criteria, params);

        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return query.toString();
    }

    /**
     * Build WHERE conditions from search criteria.
     */
    private List<String> buildConditions(DogReportSearchCriteria criteria, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();

        if (criteria.getTenantId() != null) {
            conditions.add("r.tenant_id = :tenantId");
            params.addValue("tenantId", criteria.getTenantId());
        }

        if (criteria.getIds() != null && !criteria.getIds().isEmpty()) {
            conditions.add("r.id IN (:ids)");
            params.addValue("ids", criteria.getIds());
        }

        if (criteria.getReportNumber() != null) {
            conditions.add("r.report_number = :reportNumber");
            params.addValue("reportNumber", criteria.getReportNumber());
        }

        if (criteria.getTrackingId() != null) {
            conditions.add("r.tracking_id = :trackingId");
            params.addValue("trackingId", criteria.getTrackingId());
        }

        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            conditions.add("r.status IN (:statuses)");
            params.addValue("statuses", criteria.getStatus());
        }

        if (criteria.getServiceType() != null) {
            conditions.add("r.service_type = :serviceType");
            params.addValue("serviceType", criteria.getServiceType());
        }

        if (criteria.getReporterId() != null) {
            conditions.add("r.reporter_id = :reporterId");
            params.addValue("reporterId", criteria.getReporterId());
        }

        if (criteria.getAssignedOfficerId() != null) {
            conditions.add("r.assigned_officer_id = :assignedOfficerId");
            params.addValue("assignedOfficerId", criteria.getAssignedOfficerId());
        }

        if (criteria.getLocalityCode() != null) {
            conditions.add("l.locality_code = :localityCode");
            params.addValue("localityCode", criteria.getLocalityCode());
        }

        if (criteria.getWardCode() != null) {
            conditions.add("l.ward_code = :wardCode");
            params.addValue("wardCode", criteria.getWardCode());
        }

        if (criteria.getDistrictCode() != null) {
            conditions.add("l.district_code = :districtCode");
            params.addValue("districtCode", criteria.getDistrictCode());
        }

        if (criteria.getFromDate() != null) {
            conditions.add("r.createdtime >= :fromDate");
            params.addValue("fromDate", criteria.getFromDate());
        }

        if (criteria.getToDate() != null) {
            conditions.add("r.createdtime <= :toDate");
            params.addValue("toDate", criteria.getToDate());
        }

        if (criteria.getPayoutEligible() != null) {
            conditions.add("r.payout_eligible = :payoutEligible");
            params.addValue("payoutEligible", criteria.getPayoutEligible());
        }

        if (criteria.getPayoutStatus() != null) {
            conditions.add("r.payout_status = :payoutStatus");
            params.addValue("payoutStatus", criteria.getPayoutStatus());
        }

        return conditions;
    }
}
