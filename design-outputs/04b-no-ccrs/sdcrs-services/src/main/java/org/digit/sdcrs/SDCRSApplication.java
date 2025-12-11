package org.digit.sdcrs;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * SDCRSApplication - Main Spring Boot application for SDCRS service.
 *
 * Stray Dog Capture & Reporting System (SDCRS)
 * - Manages dog sighting reports from teachers
 * - Handles verification workflow
 * - Coordinates with MC officers for capture
 * - Processes teacher payouts upon successful capture
 */
@SpringBootApplication
@Import(TracerConfiguration.class)
public class SDCRSApplication {

    public static void main(String[] args) {
        SpringApplication.run(SDCRSApplication.class, args);
    }

    /**
     * Set default timezone for the application.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    /**
     * RestTemplate bean for external service calls.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
