package org.digit.sdcrs.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportSearchCriteria;
import org.digit.sdcrs.repository.rowmapper.DogReportRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * DogReportRepository - Database access layer for dog reports.
 * Uses JDBC template for direct database queries.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DogReportRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final DogReportRowMapper rowMapper;
    private final DogReportQueryBuilder queryBuilder;

    /**
     * Search dog reports with criteria.
     */
    public List<DogReport> search(DogReportSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query = queryBuilder.buildSearchQuery(criteria, params);

        log.debug("Executing search query: {}", query);
        return namedJdbcTemplate.query(query, params, rowMapper);
    }

    /**
     * Count total reports matching criteria.
     */
    public Integer count(DogReportSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query = queryBuilder.buildCountQuery(criteria, params);

        log.debug("Executing count query: {}", query);
        return namedJdbcTemplate.queryForObject(query, params, Integer.class);
    }

    /**
     * Find report by report number or tracking ID.
     * Used for public tracking endpoint.
     */
    public DogReport findByTrackingIdentifier(String reportNumber, String trackingId) {
        StringBuilder query = new StringBuilder(DogReportQueryBuilder.BASE_QUERY);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (reportNumber != null && !reportNumber.isEmpty()) {
            query.append(" WHERE r.report_number = :reportNumber");
            params.addValue("reportNumber", reportNumber);
        } else if (trackingId != null && !trackingId.isEmpty()) {
            query.append(" WHERE r.tracking_id = :trackingId");
            params.addValue("trackingId", trackingId);
        } else {
            return null;
        }

        log.debug("Executing tracking query: {}", query);
        List<DogReport> results = namedJdbcTemplate.query(query.toString(), params, rowMapper);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find reports by IDs.
     */
    public List<DogReport> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        String query = DogReportQueryBuilder.BASE_QUERY + " WHERE r.id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);

        return namedJdbcTemplate.query(query, params, rowMapper);
    }

    /**
     * Check if image hash already exists (duplicate detection).
     */
    public List<DogReport> findByImageHash(String tenantId, String imageHash) {
        String query = DogReportQueryBuilder.BASE_QUERY +
                " WHERE r.tenant_id = :tenantId AND r.image_hash = :imageHash" +
                " AND r.status NOT IN ('REJECTED', 'AUTO_REJECTED', 'DUPLICATE')";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tenantId", tenantId);
        params.addValue("imageHash", imageHash);

        return namedJdbcTemplate.query(query, params, rowMapper);
    }

    /**
     * Get monthly payout total for a reporter.
     */
    public java.math.BigDecimal getMonthlyPayoutTotal(String tenantId, String reporterId,
                                                       Long fromDate, Long toDate) {
        String query = """
            SELECT COALESCE(SUM(r.payout_amount), 0)
            FROM eg_sdcrs_report r
            WHERE r.tenant_id = :tenantId
              AND r.reporter_id = :reporterId
              AND r.payout_status = 'COMPLETED'
              AND r.createdtime >= :fromDate
              AND r.createdtime <= :toDate
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tenantId", tenantId);
        params.addValue("reporterId", reporterId);
        params.addValue("fromDate", fromDate);
        params.addValue("toDate", toDate);

        return namedJdbcTemplate.queryForObject(query, params, java.math.BigDecimal.class);
    }

    /**
     * Get daily report count for a reporter.
     */
    public Integer getDailyReportCount(String tenantId, String reporterId, Long fromDate, Long toDate) {
        String query = """
            SELECT COUNT(*)
            FROM eg_sdcrs_report r
            WHERE r.tenant_id = :tenantId
              AND r.reporter_id = :reporterId
              AND r.createdtime >= :fromDate
              AND r.createdtime <= :toDate
              AND r.status NOT IN ('AUTO_REJECTED', 'REJECTED', 'DUPLICATE')
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("tenantId", tenantId);
        params.addValue("reporterId", reporterId);
        params.addValue("fromDate", fromDate);
        params.addValue("toDate", toDate);

        return namedJdbcTemplate.queryForObject(query, params, Integer.class);
    }
}
