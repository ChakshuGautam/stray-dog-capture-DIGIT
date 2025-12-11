package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * UrlShortenerService - Integration with DIGIT URL Shortening Service.
 * Creates short tracking URLs for SMS delivery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final RestTemplate restTemplate;
    private final SDCRSConfiguration config;

    /**
     * Shorten a URL using DIGIT URL Shortener service.
     *
     * @param longUrl The full tracking URL
     * @return Shortened URL or original if shortening fails
     */
    public String shortenUrl(String longUrl) {
        log.debug("Shortening URL: {}", longUrl);

        try {
            String url = config.getUrlShortenerHost() + config.getUrlShortenerPath();

            Map<String, String> request = Map.of("url", longUrl);
            String shortUrl = restTemplate.postForObject(url, request, String.class);

            if (shortUrl != null && !shortUrl.isEmpty()) {
                log.debug("Shortened URL: {}", shortUrl);
                return shortUrl;
            }
        } catch (Exception e) {
            log.warn("Error shortening URL, using original: {}", e.getMessage());
        }

        // Return original URL if shortening fails
        return longUrl;
    }

    /**
     * Create tracking URL with report number and tracking ID.
     *
     * @param reportNumber Report number for reference
     * @param trackingId Short tracking ID
     * @return Full tracking URL
     */
    public String createTrackingUrl(String reportNumber, String trackingId) {
        return config.getTrackingBaseUrl() + "?trackingId=" + trackingId;
    }
}
