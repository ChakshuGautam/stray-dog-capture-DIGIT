package org.digit.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * FraudDetectionApplication - Main entry point for the Fraud Detection Service.
 *
 * This is a generic, reusable fraud detection microservice for the DIGIT platform.
 * It can be used by any DIGIT module (SDCRS, TL, PGR, etc.) to validate
 * submissions against configurable fraud rules.
 *
 * Key Features:
 * - MDMS-driven fraud rules configuration
 * - INTERNAL rules: Threshold-based validation (GPS, timestamp, duplicates, velocity)
 * - EXTERNAL rules: AI/ML API integration via ExternalValidators
 * - Configurable scoring and risk assessment
 * - Caching for performance optimization
 *
 * API Endpoints:
 * - POST /fraud/v1/_evaluate       - Full evaluation
 * - POST /fraud/v1/_evaluateSync   - INTERNAL rules only (fast)
 * - POST /fraud/v1/_evaluateAsync  - EXTERNAL rules only (AI/ML)
 * - POST /fraud/v1/rules/_search   - Get fraud rules from MDMS
 */
@SpringBootApplication
@EnableCaching
public class FraudDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }
}
