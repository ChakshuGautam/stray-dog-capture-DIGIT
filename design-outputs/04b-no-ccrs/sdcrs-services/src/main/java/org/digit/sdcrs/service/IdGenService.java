package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * IdGenService - Integration with DIGIT IDGen Service.
 * Generates unique IDs for:
 * - Report numbers (DJ-SDCRS-YYYY-NNNNNN)
 * - Tracking IDs (ABC123)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdGenService {

    private final RestTemplate restTemplate;
    private final SDCRSConfiguration config;

    // IDGen format names (configured in MDMS/IDGen)
    private static final String FORMAT_REPORT_NUMBER = "sdcrs.report.number";
    private static final String FORMAT_TRACKING_ID = "sdcrs.tracking.id";

    /**
     * Generate report number.
     * Format: DJ-SDCRS-[YYYY]-[NNNNNN]
     * Example: DJ-SDCRS-2024-000123
     */
    public String generateReportNumber(RequestInfo requestInfo, String tenantId) {
        return generateId(requestInfo, tenantId, FORMAT_REPORT_NUMBER);
    }

    /**
     * Generate tracking ID.
     * Format: [A-Z]{3}[0-9]{3}
     * Example: ABC123
     */
    public String generateTrackingId(RequestInfo requestInfo, String tenantId) {
        return generateId(requestInfo, tenantId, FORMAT_TRACKING_ID);
    }

    /**
     * Generate ID using IDGen service.
     */
    private String generateId(RequestInfo requestInfo, String tenantId, String format) {
        log.debug("Generating ID: tenantId={}, format={}", tenantId, format);

        String url = config.getIdGenHost() + config.getIdGenPath();

        Map<String, Object> idRequest = Map.of(
                "RequestInfo", requestInfo,
                "idRequests", List.of(
                        Map.of(
                                "tenantId", tenantId,
                                "idName", format,
                                "format", format,
                                "count", 1
                        )
                )
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, idRequest, Map.class);

            if (response != null && response.containsKey("idResponses")) {
                List<Map<String, String>> idResponses =
                        (List<Map<String, String>>) response.get("idResponses");
                if (!idResponses.isEmpty()) {
                    return idResponses.get(0).get("id");
                }
            }
        } catch (Exception e) {
            log.error("Error generating ID", e);
            throw new CustomException("IDGEN_ERROR", "Error generating ID: " + e.getMessage());
        }

        throw new CustomException("IDGEN_ERROR", "Failed to generate ID");
    }

    /**
     * Generate multiple IDs at once.
     */
    public List<String> generateIds(RequestInfo requestInfo, String tenantId,
                                    String format, int count) {
        log.debug("Generating {} IDs: tenantId={}, format={}", count, tenantId, format);

        String url = config.getIdGenHost() + config.getIdGenPath();

        Map<String, Object> idRequest = Map.of(
                "RequestInfo", requestInfo,
                "idRequests", List.of(
                        Map.of(
                                "tenantId", tenantId,
                                "idName", format,
                                "format", format,
                                "count", count
                        )
                )
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, idRequest, Map.class);

            if (response != null && response.containsKey("idResponses")) {
                List<Map<String, String>> idResponses =
                        (List<Map<String, String>>) response.get("idResponses");
                return idResponses.stream()
                        .map(r -> r.get("id"))
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error generating IDs", e);
            throw new CustomException("IDGEN_ERROR", "Error generating IDs: " + e.getMessage());
        }

        throw new CustomException("IDGEN_ERROR", "Failed to generate IDs");
    }
}
