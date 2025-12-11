package org.digit.sdcrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * UserService - Integration with DIGIT User Service.
 * Fetches user details for enrichment and validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SDCRSConfiguration config;

    /**
     * Get user by UUID.
     */
    public User getUserById(RequestInfo requestInfo, String uuid) {
        log.debug("Fetching user by ID: {}", uuid);

        String url = config.getUserHost() + config.getUserSearchPath();

        Map<String, Object> userRequest = Map.of(
                "RequestInfo", requestInfo,
                "uuid", List.of(uuid)
        );

        try {
            String response = restTemplate.postForObject(url, userRequest, String.class);
            return parseUserResponse(response);
        } catch (Exception e) {
            log.error("Error fetching user", e);
            throw new CustomException("USER_FETCH_ERROR", "Error fetching user: " + e.getMessage());
        }
    }

    /**
     * Get user by mobile number.
     */
    public User getUserByMobile(RequestInfo requestInfo, String tenantId, String mobileNumber) {
        log.debug("Fetching user by mobile: {}", mobileNumber);

        String url = config.getUserHost() + config.getUserSearchPath();

        Map<String, Object> userRequest = Map.of(
                "RequestInfo", requestInfo,
                "tenantId", tenantId,
                "mobileNumber", mobileNumber
        );

        try {
            String response = restTemplate.postForObject(url, userRequest, String.class);
            return parseUserResponse(response);
        } catch (Exception e) {
            log.error("Error fetching user", e);
            throw new CustomException("USER_FETCH_ERROR", "Error fetching user: " + e.getMessage());
        }
    }

    /**
     * Check if user has a specific role.
     */
    public boolean userHasRole(User user, String roleCode) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }

    /**
     * Parse user response from User service.
     */
    private User parseUserResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode userNode = root.path("user");

            if (userNode.isArray() && !userNode.isEmpty()) {
                return objectMapper.treeToValue(userNode.get(0), User.class);
            }
        } catch (Exception e) {
            log.error("Error parsing user response", e);
        }
        return null;
    }

    /**
     * Get users by role for assignment.
     */
    public List<User> getUsersByRole(RequestInfo requestInfo, String tenantId, String roleCode) {
        log.debug("Fetching users by role: {} for tenant: {}", roleCode, tenantId);

        String url = config.getUserHost() + config.getUserSearchPath();

        Map<String, Object> userRequest = Map.of(
                "RequestInfo", requestInfo,
                "tenantId", tenantId,
                "roleCodes", List.of(roleCode)
        );

        try {
            String response = restTemplate.postForObject(url, userRequest, String.class);
            return parseUsersResponse(response);
        } catch (Exception e) {
            log.error("Error fetching users", e);
            throw new CustomException("USER_FETCH_ERROR", "Error fetching users: " + e.getMessage());
        }
    }

    /**
     * Parse multiple users from response.
     */
    private List<User> parseUsersResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode userNode = root.path("user");

            if (userNode.isArray()) {
                return objectMapper.readerForListOf(User.class).readValue(userNode);
            }
        } catch (Exception e) {
            log.error("Error parsing users response", e);
        }
        return List.of();
    }
}
