package org.digit.sdcrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import org.digit.sdcrs.model.fraud.FraudRule;
import org.digit.sdcrs.model.fraud.ExternalValidator;

/**
 * MDMSService - Integration with DIGIT MDMS Service.
 * Fetches master data: ServiceType, PayoutConfig, RejectionReason, ResolutionType.
 * Also fetches fraud detection config: FraudRules, ExternalValidators.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MDMSService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SDCRSConfiguration config;

    private static final String MODULE_NAME = "SDCRS";
    private static final String FRAUD_MODULE_NAME = "FRAUD-DETECTION";

    // Master data names - SDCRS module
    public static final String MASTER_SERVICE_TYPE = "ServiceType";
    public static final String MASTER_PAYOUT_CONFIG = "PayoutConfig";
    public static final String MASTER_REJECTION_REASON = "RejectionReason";
    public static final String MASTER_RESOLUTION_TYPE = "ResolutionType";

    // Master data names - FRAUD-DETECTION module
    public static final String MASTER_FRAUD_RULES = "FraudRules";
    public static final String MASTER_EXTERNAL_VALIDATORS = "ExternalValidators";

    /**
     * Get MDMS data for SDCRS module.
     *
     * @param requestInfo Standard request info
     * @param tenantId Tenant ID
     * @param masterNames List of master data names to fetch
     * @return Map of master name to list of master data objects
     */
    @Cacheable(value = "mdms", key = "#tenantId + '_' + #masterNames.toString()")
    public Map<String, List<Map<String, Object>>> getMdmsData(
            RequestInfo requestInfo, String tenantId, List<String> masterNames) {

        log.debug("Fetching MDMS data for tenant: {}, masters: {}", tenantId, masterNames);

        String url = config.getMdmsHost() + config.getMdmsSearchPath();

        // Build MDMS request
        Map<String, Object> mdmsRequest = buildMdmsRequest(requestInfo, tenantId, masterNames);

        try {
            String response = restTemplate.postForObject(url, mdmsRequest, String.class);
            return parseMdmsResponse(response);
        } catch (Exception e) {
            log.error("Error fetching MDMS data", e);
            throw new CustomException("MDMS_ERROR", "Error fetching master data: " + e.getMessage());
        }
    }

    /**
     * Build MDMS search request.
     */
    private Map<String, Object> buildMdmsRequest(RequestInfo requestInfo,
                                                  String tenantId, List<String> masterNames) {
        List<Map<String, Object>> masterDetails = new ArrayList<>();
        for (String masterName : masterNames) {
            masterDetails.add(Map.of(
                    "name", masterName,
                    "filter", "[?(@.active==true)]"
            ));
        }

        return Map.of(
                "RequestInfo", requestInfo,
                "MdmsCriteria", Map.of(
                        "tenantId", tenantId,
                        "moduleDetails", List.of(
                                Map.of(
                                        "moduleName", MODULE_NAME,
                                        "masterDetails", masterDetails
                                )
                        )
                )
        );
    }

    /**
     * Parse MDMS response.
     */
    private Map<String, List<Map<String, Object>>> parseMdmsResponse(String response) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode mdmsRes = root.path("MdmsRes").path(MODULE_NAME);

            Iterator<String> fieldNames = mdmsRes.fieldNames();
            while (fieldNames.hasNext()) {
                String masterName = fieldNames.next();
                JsonNode masterData = mdmsRes.path(masterName);

                List<Map<String, Object>> dataList = new ArrayList<>();
                if (masterData.isArray()) {
                    for (JsonNode item : masterData) {
                        Map<String, Object> itemMap = objectMapper.convertValue(item, Map.class);
                        dataList.add(itemMap);
                    }
                }
                result.put(masterName, dataList);
            }
        } catch (Exception e) {
            log.error("Error parsing MDMS response", e);
        }

        return result;
    }

    /**
     * Validate service type code exists in MDMS.
     */
    public boolean isValidServiceType(RequestInfo requestInfo, String tenantId, String serviceType) {
        Map<String, List<Map<String, Object>>> mdmsData = getMdmsData(
                requestInfo, tenantId, List.of(MASTER_SERVICE_TYPE));

        List<Map<String, Object>> serviceTypes = mdmsData.get(MASTER_SERVICE_TYPE);
        if (serviceTypes == null) return false;

        return serviceTypes.stream()
                .anyMatch(st -> serviceType.equals(st.get("code")));
    }

    /**
     * Validate rejection reason code exists in MDMS.
     */
    public boolean isValidRejectionReason(RequestInfo requestInfo, String tenantId, String reason) {
        Map<String, List<Map<String, Object>>> mdmsData = getMdmsData(
                requestInfo, tenantId, List.of(MASTER_REJECTION_REASON));

        List<Map<String, Object>> reasons = mdmsData.get(MASTER_REJECTION_REASON);
        if (reasons == null) return false;

        return reasons.stream()
                .anyMatch(r -> reason.equals(r.get("code")));
    }

    /**
     * Validate resolution type code exists in MDMS.
     */
    public boolean isValidResolutionType(RequestInfo requestInfo, String tenantId, String resolutionType) {
        Map<String, List<Map<String, Object>>> mdmsData = getMdmsData(
                requestInfo, tenantId, List.of(MASTER_RESOLUTION_TYPE));

        List<Map<String, Object>> types = mdmsData.get(MASTER_RESOLUTION_TYPE);
        if (types == null) return false;

        return types.stream()
                .anyMatch(t -> resolutionType.equals(t.get("code")));
    }

    /**
     * Get payout configuration.
     */
    public PayoutService.PayoutConfig getPayoutConfig(RequestInfo requestInfo, String tenantId) {
        Map<String, List<Map<String, Object>>> mdmsData = getMdmsData(
                requestInfo, tenantId, List.of(MASTER_PAYOUT_CONFIG));

        List<Map<String, Object>> configs = mdmsData.get(MASTER_PAYOUT_CONFIG);
        if (configs == null || configs.isEmpty()) {
            throw new CustomException("PAYOUT_CONFIG_NOT_FOUND", "Payout configuration not found");
        }

        Map<String, Object> configMap = configs.get(0);

        return PayoutService.PayoutConfig.builder()
                .amount(new java.math.BigDecimal(configMap.get("amount").toString()))
                .monthlyCap(new java.math.BigDecimal(configMap.get("monthlyCap").toString()))
                .dailyLimit((Integer) configMap.get("dailyLimit"))
                .taxHeadCode((String) configMap.get("taxHeadCode"))
                .businessService((String) configMap.get("businessService"))
                .build();
    }

    // ========== FRAUD-DETECTION Module Methods (stubs) ==========

    /**
     * Get fraud rules from MDMS FRAUD-DETECTION module.
     *
     * @param requestInfo Request context
     * @param tenantId Tenant ID
     * @return List of FraudRule objects
     */
    @Cacheable(value = "fraudRules", key = "#tenantId")
    public List<FraudRule> getFraudRules(RequestInfo requestInfo, String tenantId) {
        log.debug("Fetching fraud rules for tenant: {}", tenantId);

        // TODO: Implement
        // 1. Call getMdmsDataForModule(requestInfo, tenantId, FRAUD_MODULE_NAME, MASTER_FRAUD_RULES)
        // 2. Parse response into List<FraudRule>
        // 3. Filter by enabled=true

        return List.of();
    }

    /**
     * Get external validators from MDMS FRAUD-DETECTION module.
     *
     * @param requestInfo Request context
     * @param tenantId Tenant ID
     * @return List of ExternalValidator objects
     */
    @Cacheable(value = "externalValidators", key = "#tenantId")
    public List<ExternalValidator> getExternalValidators(RequestInfo requestInfo, String tenantId) {
        log.debug("Fetching external validators for tenant: {}", tenantId);

        // TODO: Implement
        // 1. Call getMdmsDataForModule(requestInfo, tenantId, FRAUD_MODULE_NAME, MASTER_EXTERNAL_VALIDATORS)
        // 2. Parse response into List<ExternalValidator>
        // 3. Filter by enabled=true

        return List.of();
    }

    /**
     * Get a specific external validator by ID.
     *
     * @param requestInfo Request context
     * @param tenantId Tenant ID
     * @param validatorId Validator ID (e.g., "DOG_BREED_CLASSIFIER")
     * @return ExternalValidator or null if not found
     */
    public ExternalValidator getExternalValidatorById(RequestInfo requestInfo, String tenantId, String validatorId) {
        List<ExternalValidator> validators = getExternalValidators(requestInfo, tenantId);
        return validators.stream()
                .filter(v -> validatorId.equals(v.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get fraud rules filtered by rule type.
     *
     * @param requestInfo Request context
     * @param tenantId Tenant ID
     * @param ruleType INTERNAL or EXTERNAL
     * @return Filtered list of FraudRule objects
     */
    public List<FraudRule> getFraudRulesByType(RequestInfo requestInfo, String tenantId, FraudRule.RuleType ruleType) {
        List<FraudRule> allRules = getFraudRules(requestInfo, tenantId);
        return allRules.stream()
                .filter(r -> ruleType.equals(r.getRuleType()))
                .toList();
    }

    /**
     * Get MDMS data for a specific module (not just SDCRS).
     */
    private Map<String, List<Map<String, Object>>> getMdmsDataForModule(
            RequestInfo requestInfo, String tenantId, String moduleName, List<String> masterNames) {

        log.debug("Fetching MDMS data for module: {}, tenant: {}, masters: {}",
                moduleName, tenantId, masterNames);

        // TODO: Implement similar to getMdmsData but with configurable module name
        return Map.of();
    }
}
