package org.digit.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.fraud.model.FraudEvaluationRequest;
import org.digit.fraud.model.FraudEvaluationResponse.RuleResult;
import org.digit.fraud.model.FraudRule;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalRuleEvaluator {

    private final MDMSService mdmsService;
    private final ExpressionEvaluatorService expressionEvaluator;

    // In-memory stores for velocity/duplicate checks (would be Redis/DB in production)
    private final Map<String, List<Long>> submissionTimestamps = new ConcurrentHashMap<>();
    private final Map<String, String> contentHashes = new ConcurrentHashMap<>();
    private final Map<String, String> imageHashes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> deviceUsers = new ConcurrentHashMap<>();

    public RuleResult evaluate(FraudRule rule, FraudEvaluationRequest request) {
        Map<String, Object> condition = rule.getCondition();
        String conditionType = (String) condition.get("type");

        log.debug("Evaluating rule {} ({}) with condition type: {}",
                rule.getId(), rule.getCode(), conditionType);

        return switch (conditionType) {
            case "NULL_CHECK" -> evaluateNullCheck(rule, request, condition);
            case "GEO_BOUNDARY" -> evaluateGeoBoundary(rule, request, condition);
            case "GEO_DISTANCE" -> evaluateGeoDistance(rule, request, condition);
            case "VELOCITY" -> evaluateVelocity(rule, request, condition);
            case "TIMESTAMP_AGE" -> evaluateTimestampAge(rule, request, condition);
            case "TIMESTAMP_DIFF" -> evaluateTimestampDiff(rule, request, condition);
            case "HASH_MATCH" -> evaluateHashMatch(rule, request, condition);
            case "IMAGE_SIMILARITY" -> evaluateImageSimilarity(rule, request, condition);
            case "INTERVAL" -> evaluateInterval(rule, request, condition);
            case "DEVICE_SHARING" -> evaluateDeviceSharing(rule, request, condition);
            case "GEO_CLUSTER" -> evaluateGeoCluster(rule, request, condition);
            case "METADATA_CHECK" -> evaluateMetadataCheck(rule, request, condition);
            case "TIME_WINDOW" -> evaluateTimeWindow(rule, request, condition);
            case "AGGREGATE_COUNT" -> evaluateAggregateCount(rule, request, condition);
            case "GPS_VELOCITY" -> evaluateGpsVelocity(rule, request, condition);
            case "CUSTOM" -> evaluateCustomExpression(rule, request, condition);
            default -> createNotTriggeredResult(rule, "Unknown condition type: " + conditionType);
        };
    }

    private RuleResult evaluateNullCheck(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        String field = (String) condition.get("field");

        boolean isNull = false;
        String fieldValue = null;

        if (field.contains("gpsLatitude")) {
            if (request.getEvidences() != null) {
                isNull = request.getEvidences().stream()
                        .anyMatch(e -> e.getMetadata() == null || e.getMetadata().getGpsLatitude() == null);
            } else {
                isNull = true;
            }
        }

        if (isNull) {
            return createTriggeredResult(rule, "Required field is missing: " + field,
                    Map.of("field", field, "missing", true));
        }
        return createNotTriggeredResult(rule, "Field present: " + field);
    }

    private RuleResult evaluateGeoBoundary(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        FraudEvaluationRequest.LocationData location = request.getLocationData();
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            return createNotTriggeredResult(rule, "No location data to validate");
        }

        // Simulated boundary check - NCR region approximate bounds
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // NCR approximate bounds: 28.4-28.9 N, 76.8-77.5 E
        boolean outsideBoundary = lat < 28.4 || lat > 28.9 || lon < 76.8 || lon > 77.5;

        if (outsideBoundary) {
            return createTriggeredResult(rule, "Location outside tenant boundary",
                    Map.of("latitude", lat, "longitude", lon, "boundary", "NCR"));
        }
        return createNotTriggeredResult(rule, "Location within boundary");
    }

    private RuleResult evaluateGeoDistance(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number maxDistanceMeters = (Number) condition.get("maxDistanceMeters");
        if (maxDistanceMeters == null) maxDistanceMeters = 500;

        if (request.getEvidences() == null || request.getEvidences().size() < 2) {
            return createNotTriggeredResult(rule, "Insufficient evidence for GPS comparison");
        }

        FraudEvaluationRequest.EvidenceData dogPhoto = request.getEvidences().stream()
                .filter(e -> "DOG_PHOTO".equals(e.getPurpose()))
                .findFirst().orElse(null);
        FraudEvaluationRequest.EvidenceData selfie = request.getEvidences().stream()
                .filter(e -> "SELFIE".equals(e.getPurpose()))
                .findFirst().orElse(null);

        if (dogPhoto == null || selfie == null ||
                dogPhoto.getMetadata() == null || selfie.getMetadata() == null ||
                dogPhoto.getMetadata().getGpsLatitude() == null || selfie.getMetadata().getGpsLatitude() == null) {
            return createNotTriggeredResult(rule, "Missing GPS data in evidence");
        }

        double distance = calculateDistance(
                dogPhoto.getMetadata().getGpsLatitude(), dogPhoto.getMetadata().getGpsLongitude(),
                selfie.getMetadata().getGpsLatitude(), selfie.getMetadata().getGpsLongitude()
        );

        if (distance > maxDistanceMeters.doubleValue()) {
            return createTriggeredResult(rule, String.format("GPS mismatch: %.0fm apart", distance),
                    Map.of("distance_meters", distance, "max_allowed", maxDistanceMeters));
        }
        return createNotTriggeredResult(rule, String.format("GPS within range: %.0fm", distance));
    }

    private RuleResult evaluateVelocity(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number threshold = (Number) condition.get("threshold");
        Number windowHours = (Number) condition.get("windowHours");
        if (threshold == null) threshold = 5;
        if (windowHours == null) windowHours = 1;

        String applicantId = request.getApplicantInfo() != null ?
                request.getApplicantInfo().getApplicantId() : null;
        if (applicantId == null) {
            return createNotTriggeredResult(rule, "No applicant ID for velocity check");
        }

        long now = System.currentTimeMillis();
        long windowMs = windowHours.longValue() * 60 * 60 * 1000;
        long windowStart = now - windowMs;

        List<Long> timestamps = submissionTimestamps.getOrDefault(applicantId, List.of());
        long recentCount = timestamps.stream().filter(t -> t > windowStart).count() + 1;

        // Record this submission
        submissionTimestamps.compute(applicantId, (k, v) -> {
            List<Long> newList = new java.util.ArrayList<>(v != null ? v : List.of());
            newList.add(now);
            return newList;
        });

        if (recentCount > threshold.longValue()) {
            return createTriggeredResult(rule,
                    String.format("High velocity: %d submissions in %d hours", recentCount, windowHours),
                    Map.of("count", recentCount, "threshold", threshold, "window_hours", windowHours));
        }
        return createNotTriggeredResult(rule, String.format("Velocity OK: %d submissions", recentCount));
    }

    private RuleResult evaluateTimestampAge(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number maxAgeHours = (Number) condition.get("maxAgeHours");
        if (maxAgeHours == null) maxAgeHours = 48;

        if (request.getEvidences() == null || request.getEvidences().isEmpty()) {
            return createNotTriggeredResult(rule, "No evidence to check timestamp");
        }

        long now = System.currentTimeMillis();
        long maxAgeMs = maxAgeHours.longValue() * 60 * 60 * 1000;

        for (FraudEvaluationRequest.EvidenceData evidence : request.getEvidences()) {
            if (evidence.getMetadata() != null && evidence.getMetadata().getTimestamp() != null) {
                long age = now - evidence.getMetadata().getTimestamp();
                if (age > maxAgeMs) {
                    long ageHours = age / (60 * 60 * 1000);
                    return createTriggeredResult(rule,
                            String.format("Evidence is %d hours old (max: %d)", ageHours, maxAgeHours),
                            Map.of("age_hours", ageHours, "max_hours", maxAgeHours));
                }
            }
        }
        return createNotTriggeredResult(rule, "Evidence timestamps within allowed age");
    }

    private RuleResult evaluateTimestampDiff(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number maxDiffMinutes = (Number) condition.get("maxDiffMinutes");
        if (maxDiffMinutes == null) maxDiffMinutes = 10;

        if (request.getEvidences() == null || request.getEvidences().size() < 2) {
            return createNotTriggeredResult(rule, "Insufficient evidence for timestamp comparison");
        }

        Long dogPhotoTs = request.getEvidences().stream()
                .filter(e -> "DOG_PHOTO".equals(e.getPurpose()))
                .findFirst()
                .map(e -> e.getMetadata() != null ? e.getMetadata().getTimestamp() : null)
                .orElse(null);

        Long selfieTs = request.getEvidences().stream()
                .filter(e -> "SELFIE".equals(e.getPurpose()))
                .findFirst()
                .map(e -> e.getMetadata() != null ? e.getMetadata().getTimestamp() : null)
                .orElse(null);

        if (dogPhotoTs == null || selfieTs == null) {
            return createNotTriggeredResult(rule, "Missing timestamps for comparison");
        }

        long diffMinutes = Math.abs(dogPhotoTs - selfieTs) / (60 * 1000);
        if (diffMinutes > maxDiffMinutes.longValue()) {
            return createTriggeredResult(rule,
                    String.format("Photo-Selfie time gap: %d minutes (max: %d)", diffMinutes, maxDiffMinutes),
                    Map.of("diff_minutes", diffMinutes, "max_minutes", maxDiffMinutes));
        }
        return createNotTriggeredResult(rule, String.format("Time gap OK: %d minutes", diffMinutes));
    }

    private RuleResult evaluateHashMatch(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        String field = (String) condition.get("field");

        if (request.getEvidences() == null || request.getEvidences().isEmpty()) {
            return createNotTriggeredResult(rule, "No evidence to check for duplicates");
        }

        for (FraudEvaluationRequest.EvidenceData evidence : request.getEvidences()) {
            String hash = evidence.getContentHash();
            if (hash != null) {
                String existingAppId = contentHashes.get(hash);
                if (existingAppId != null && !existingAppId.equals(request.getApplicationId())) {
                    return createTriggeredResult(rule, "Exact duplicate content detected",
                            Map.of("matching_application", existingAppId, "hash", hash));
                }
                contentHashes.put(hash, request.getApplicationId());
            }
        }
        return createNotTriggeredResult(rule, "No duplicate content found");
    }

    private RuleResult evaluateImageSimilarity(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number threshold = (Number) condition.get("threshold");
        if (threshold == null) threshold = 0.90;

        // Simulated perceptual hash comparison
        // In production, this would use a pHash library and image comparison service
        return createNotTriggeredResult(rule, "Image similarity check passed (simulated)");
    }

    private RuleResult evaluateInterval(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number minIntervalMinutes = (Number) condition.get("minIntervalMinutes");
        if (minIntervalMinutes == null) minIntervalMinutes = 5;

        String applicantId = request.getApplicantInfo() != null ?
                request.getApplicantInfo().getApplicantId() : null;
        if (applicantId == null) {
            return createNotTriggeredResult(rule, "No applicant ID for interval check");
        }

        List<Long> timestamps = submissionTimestamps.getOrDefault(applicantId, List.of());
        if (!timestamps.isEmpty()) {
            long lastSubmission = timestamps.get(timestamps.size() - 1);
            long intervalMinutes = (System.currentTimeMillis() - lastSubmission) / (60 * 1000);
            if (intervalMinutes < minIntervalMinutes.longValue()) {
                return createTriggeredResult(rule,
                        String.format("Rapid submission: %d minutes since last (min: %d)", intervalMinutes, minIntervalMinutes),
                        Map.of("interval_minutes", intervalMinutes, "min_interval", minIntervalMinutes));
            }
        }
        return createNotTriggeredResult(rule, "Submission interval OK");
    }

    private RuleResult evaluateDeviceSharing(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number minUniqueUsers = (Number) condition.get("minUniqueUsers");
        if (minUniqueUsers == null) minUniqueUsers = 2;

        String deviceId = null;
        if (request.getEvidences() != null) {
            deviceId = request.getEvidences().stream()
                    .filter(e -> e.getMetadata() != null && e.getMetadata().getDeviceId() != null)
                    .findFirst()
                    .map(e -> e.getMetadata().getDeviceId())
                    .orElse(null);
        }
        if (deviceId == null && request.getApplicantInfo() != null) {
            deviceId = request.getApplicantInfo().getDeviceId();
        }
        if (deviceId == null) {
            return createNotTriggeredResult(rule, "No device ID to check sharing");
        }

        String applicantId = request.getApplicantInfo() != null ?
                request.getApplicantInfo().getApplicantId() : "unknown";

        String finalDeviceId = deviceId;
        deviceUsers.compute(deviceId, (k, v) -> {
            List<String> users = new java.util.ArrayList<>(v != null ? v : List.of());
            if (!users.contains(applicantId)) {
                users.add(applicantId);
            }
            return users;
        });

        int uniqueUsers = deviceUsers.get(deviceId).size();
        if (uniqueUsers >= minUniqueUsers.intValue()) {
            return createTriggeredResult(rule,
                    String.format("Device shared by %d users", uniqueUsers),
                    Map.of("device_id", deviceId, "unique_users", uniqueUsers, "users", deviceUsers.get(deviceId)));
        }
        return createNotTriggeredResult(rule, "Device not shared excessively");
    }

    private RuleResult evaluateGeoCluster(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulated geo-clustering check
        // In production, this would use spatial queries against historical submissions
        return createNotTriggeredResult(rule, "Geo-cluster check passed (simulated)");
    }

    private RuleResult evaluateMetadataCheck(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Boolean expectedValue = (Boolean) condition.get("expectedValue");
        String field = (String) condition.get("field");

        if (request.getEvidences() == null || request.getEvidences().isEmpty()) {
            return createNotTriggeredResult(rule, "No evidence to check metadata");
        }

        if (field.contains("exifPresent")) {
            boolean exifMissing = request.getEvidences().stream()
                    .anyMatch(e -> e.getMetadata() == null ||
                            e.getMetadata().getExifPresent() == null ||
                            !e.getMetadata().getExifPresent());
            if (exifMissing && Boolean.TRUE.equals(expectedValue)) {
                return createTriggeredResult(rule, "EXIF metadata stripped from photo",
                        Map.of("field", field, "expected", expectedValue));
            }
        }
        return createNotTriggeredResult(rule, "Metadata check passed");
    }

    private RuleResult evaluateTimeWindow(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        String timezone = (String) condition.getOrDefault("timezone", "Asia/Kolkata");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allowedWindows = (List<Map<String, Object>>) condition.get("allowedWindows");

        if (allowedWindows == null || allowedWindows.isEmpty()) {
            return createNotTriggeredResult(rule, "No time windows configured");
        }

        LocalTime now = LocalTime.now(ZoneId.of(timezone));
        String dayOfWeek = java.time.LocalDate.now(ZoneId.of(timezone)).getDayOfWeek().name().substring(0, 3);

        for (Map<String, Object> window : allowedWindows) {
            @SuppressWarnings("unchecked")
            List<String> days = (List<String>) window.get("days");
            if (days != null && days.contains(dayOfWeek)) {
                LocalTime start = LocalTime.parse((String) window.get("start"));
                LocalTime end = LocalTime.parse((String) window.get("end"));
                if (!now.isBefore(start) && !now.isAfter(end)) {
                    return createNotTriggeredResult(rule, "Within allowed time window");
                }
            }
        }

        return createTriggeredResult(rule,
                String.format("Submission outside school hours: %s %s", dayOfWeek, now),
                Map.of("time", now.toString(), "day", dayOfWeek));
    }

    private RuleResult evaluateAggregateCount(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        Number threshold = (Number) condition.get("threshold");
        Number periodDays = (Number) condition.get("periodDays");
        if (threshold == null) threshold = 5;
        if (periodDays == null) periodDays = 1;

        String applicantId = request.getApplicantInfo() != null ?
                request.getApplicantInfo().getApplicantId() : null;
        if (applicantId == null) {
            return createNotTriggeredResult(rule, "No applicant ID for aggregate check");
        }

        long now = System.currentTimeMillis();
        long periodMs = periodDays.longValue() * 24 * 60 * 60 * 1000;
        long periodStart = now - periodMs;

        List<Long> timestamps = submissionTimestamps.getOrDefault(applicantId, List.of());
        long periodCount = timestamps.stream().filter(t -> t > periodStart).count() + 1;

        if (periodCount > threshold.longValue()) {
            return createTriggeredResult(rule,
                    String.format("Aggregate limit exceeded: %d in %d days (max: %d)",
                            periodCount, periodDays, threshold),
                    Map.of("count", periodCount, "threshold", threshold, "period_days", periodDays));
        }
        return createNotTriggeredResult(rule, String.format("Aggregate count OK: %d", periodCount));
    }

    private RuleResult evaluateGpsVelocity(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        // Simulated GPS velocity check
        // In production, this would compare current location with last known location
        return createNotTriggeredResult(rule, "GPS velocity check passed (simulated)");
    }

    private RuleResult evaluateCustomExpression(FraudRule rule, FraudEvaluationRequest request, Map<String, Object> condition) {
        String expression = (String) condition.get("expression");

        if (expression == null || expression.isBlank()) {
            log.warn("Rule {} has empty expression", rule.getCode());
            return createNotTriggeredResult(rule, "No expression configured");
        }

        // Validate expression syntax first
        if (!expressionEvaluator.isValidExpression(expression)) {
            log.error("Rule {} has invalid expression: {}", rule.getCode(), expression);
            return createNotTriggeredResult(rule, "Invalid expression syntax: " + expression);
        }

        log.debug("Evaluating SpEL expression for rule {}: {}", rule.getCode(), expression);

        try {
            boolean triggered = expressionEvaluator.evaluate(expression, request);

            if (triggered) {
                return createTriggeredResult(rule,
                        String.format("Custom expression triggered: %s", expression),
                        Map.of("expression", expression, "result", true));
            }
            return createNotTriggeredResult(rule, "Custom expression evaluated to false");

        } catch (Exception e) {
            log.error("Error evaluating expression '{}' for rule {}: {}",
                    expression, rule.getCode(), e.getMessage());
            return createNotTriggeredResult(rule, "Expression evaluation error: " + e.getMessage());
        }
    }

    private RuleResult createTriggeredResult(FraudRule rule, String message, Map<String, Object> details) {
        Integer weight = mdmsService.getCategoryWeight(rule.getCategory());
        Integer score = calculateRuleScore(rule, weight);

        return RuleResult.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .triggered(true)
                .score(score)
                .message(message)
                .details(details)
                .build();
    }

    private RuleResult createNotTriggeredResult(FraudRule rule, String message) {
        return RuleResult.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getCode())
                .ruleName(rule.getName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .triggered(false)
                .score(0)
                .message(message)
                .details(Map.of())
                .build();
    }

    private Integer calculateRuleScore(FraudRule rule, Integer categoryWeight) {
        int severityMultiplier = switch (rule.getSeverity()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 1;
        };
        return (categoryWeight * severityMultiplier) / 4;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
