package org.egov.upi.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.upi.repository.rowmapper.PayoutRowMapper;
import org.egov.upi.web.models.Payout;
import org.egov.upi.web.models.PayoutSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class PayoutRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PayoutRowMapper rowMapper;

    @Autowired
    public PayoutRepository(JdbcTemplate jdbcTemplate, PayoutRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    /**
     * Search payouts based on criteria
     */
    public List<Payout> search(PayoutSearchCriteria criteria) {
        StringBuilder query = new StringBuilder(getBaseQuery());
        List<Object> params = new ArrayList<>();

        addWhereClause(query, params, criteria);
        addOrderByClause(query, criteria);
        addPagination(query, params, criteria);

        log.debug("Executing payout search query: {}", query);
        return jdbcTemplate.query(query.toString(), params.toArray(), rowMapper);
    }

    /**
     * Search by provider payout ID
     */
    public List<Payout> searchByProviderPayoutId(String providerPayoutId) {
        String query = getBaseQuery() + " WHERE p.provider_payout_id = ?";
        return jdbcTemplate.query(query, new Object[]{providerPayoutId}, rowMapper);
    }

    /**
     * Count payouts matching criteria
     */
    public Integer count(PayoutSearchCriteria criteria) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM eg_upi_payout p");
        List<Object> params = new ArrayList<>();

        addWhereClause(query, params, criteria);

        return jdbcTemplate.queryForObject(query.toString(), params.toArray(), Integer.class);
    }

    private String getBaseQuery() {
        return """
            SELECT p.id, p.tenant_id, p.payout_number, p.reference_id, p.reference_type,
                   p.beneficiary_id, p.beneficiary_type, p.amount, p.currency, p.mode,
                   p.purpose, p.narration, p.status, p.provider_payout_id, p.utr,
                   p.processed_at, p.failure_reason, p.error_code, p.retry_count,
                   p.max_retries, p.additional_details,
                   p.created_by, p.created_time, p.last_modified_by, p.last_modified_time,
                   fa.id as fa_id, fa.account_type as fa_account_type, fa.vpa as fa_vpa,
                   fa.bank_account_number as fa_bank_account, fa.ifsc_code as fa_ifsc,
                   fa.beneficiary_name as fa_beneficiary_name, fa.mobile_number as fa_mobile,
                   fa.email as fa_email, fa.provider_fund_account_id as fa_provider_fund_id,
                   fa.provider_contact_id as fa_provider_contact_id
            FROM eg_upi_payout p
            LEFT JOIN eg_upi_fund_account fa ON p.id = fa.payout_id
            """;
    }

    private void addWhereClause(StringBuilder query, List<Object> params,
                                 PayoutSearchCriteria criteria) {
        List<String> conditions = new ArrayList<>();

        if (StringUtils.hasText(criteria.getTenantId())) {
            conditions.add("p.tenant_id = ?");
            params.add(criteria.getTenantId());
        }

        if (!CollectionUtils.isEmpty(criteria.getIds())) {
            conditions.add("p.id IN (" + createPlaceholders(criteria.getIds().size()) + ")");
            params.addAll(criteria.getIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getPayoutNumbers())) {
            conditions.add("p.payout_number IN (" +
                createPlaceholders(criteria.getPayoutNumbers().size()) + ")");
            params.addAll(criteria.getPayoutNumbers());
        }

        if (!CollectionUtils.isEmpty(criteria.getReferenceIds())) {
            conditions.add("p.reference_id IN (" +
                createPlaceholders(criteria.getReferenceIds().size()) + ")");
            params.addAll(criteria.getReferenceIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getBeneficiaryIds())) {
            conditions.add("p.beneficiary_id IN (" +
                createPlaceholders(criteria.getBeneficiaryIds().size()) + ")");
            params.addAll(criteria.getBeneficiaryIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getStatus())) {
            conditions.add("p.status IN (" +
                createPlaceholders(criteria.getStatus().size()) + ")");
            criteria.getStatus().forEach(s -> params.add(s.getValue()));
        }

        if (criteria.getFromDate() != null) {
            conditions.add("p.created_time >= ?");
            params.add(criteria.getFromDate());
        }

        if (criteria.getToDate() != null) {
            conditions.add("p.created_time <= ?");
            params.add(criteria.getToDate());
        }

        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }
    }

    private void addOrderByClause(StringBuilder query, PayoutSearchCriteria criteria) {
        String sortBy = StringUtils.hasText(criteria.getSortBy()) ?
            criteria.getSortBy() : "created_time";
        String sortOrder = "ASC".equalsIgnoreCase(criteria.getSortOrder()) ? "ASC" : "DESC";

        query.append(" ORDER BY p.").append(sortBy).append(" ").append(sortOrder);
    }

    private void addPagination(StringBuilder query, List<Object> params,
                                PayoutSearchCriteria criteria) {
        query.append(" LIMIT ? OFFSET ?");
        params.add(criteria.getLimit());
        params.add(criteria.getOffset());
    }

    private String createPlaceholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }
}
