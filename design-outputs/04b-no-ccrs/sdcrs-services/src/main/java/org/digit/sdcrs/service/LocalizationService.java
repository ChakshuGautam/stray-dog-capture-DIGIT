package org.digit.sdcrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * LocalizationService - Integration with DIGIT Localization Service.
 * Fetches localized messages for status descriptions, labels, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SDCRSConfiguration config;

    private static final String DEFAULT_LOCALE = "en_IN";

    /**
     * Get localized messages for a module.
     * Results are cached for performance.
     *
     * @param tenantId Tenant ID
     * @param locale Locale code (e.g., "en_IN", "hi_IN")
     * @param module Module name (e.g., "sdcrs")
     * @return Map of message codes to localized text
     */
    @Cacheable(value = "localization", key = "#tenantId + '_' + #locale + '_' + #module")
    public Map<String, String> getMessages(String tenantId, String locale, String module) {
        log.debug("Fetching localization for tenant: {}, locale: {}, module: {}",
                tenantId, locale, module);

        if (locale == null || locale.isEmpty()) {
            locale = DEFAULT_LOCALE;
        }

        String url = buildUrl(tenantId, locale, module);

        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseMessages(response);
        } catch (Exception e) {
            log.error("Error fetching localization", e);
            return new HashMap<>();
        }
    }

    /**
     * Build localization service URL.
     */
    private String buildUrl(String tenantId, String locale, String module) {
        return config.getLocalizationHost() + config.getLocalizationSearchPath()
                + "?tenantId=" + tenantId
                + "&locale=" + locale
                + "&module=" + module;
    }

    /**
     * Parse localization response to map.
     */
    private Map<String, String> parseMessages(String response) {
        Map<String, String> messages = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode messagesNode = root.path("messages");

            if (messagesNode.isArray()) {
                for (JsonNode message : messagesNode) {
                    String code = message.path("code").asText();
                    String text = message.path("message").asText();
                    messages.put(code, text);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing localization response", e);
        }

        return messages;
    }

    /**
     * Get single localized message.
     */
    public String getMessage(String tenantId, String locale, String module, String code) {
        Map<String, String> messages = getMessages(tenantId, locale, module);
        return messages.getOrDefault(code, code);
    }

    /**
     * Get message with parameter substitution.
     * Parameters are replaced in order: {0}, {1}, etc.
     */
    public String getMessage(String tenantId, String locale, String module,
                            String code, Object... params) {
        String message = getMessage(tenantId, locale, module, code);

        for (int i = 0; i < params.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(params[i]));
        }

        return message;
    }
}
