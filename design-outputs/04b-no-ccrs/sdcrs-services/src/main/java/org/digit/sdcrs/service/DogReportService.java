package org.digit.sdcrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.enrichment.DogReportEnrichment;
import org.digit.sdcrs.kafka.DogReportProducer;
import org.digit.sdcrs.model.*;
import org.digit.sdcrs.repository.DogReportRepository;
import org.digit.sdcrs.validator.DogReportValidator;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * DogReportService - Core business logic for dog report operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DogReportService {

    private final DogReportValidator validator;
    private final DogReportEnrichment enrichment;
    private final DogReportRepository repository;
    private final DogReportProducer producer;
    private final WorkflowService workflowService;
    private final TrackingService trackingService;
    private final LocalizationService localizationService;

    @Value("${sdcrs.kafka.save.topic}")
    private String saveTopic;

    @Value("${sdcrs.kafka.update.topic}")
    private String updateTopic;

    /**
     * Create new dog report(s).
     *
     * Flow (as per SDCRS_Create sequence diagram):
     * 1. Validate MDMS master data (ServiceType, PayoutConfig)
     * 2. Validate request payload (business rules)
     * 3. Validate and extract EXIF from photos
     * 4. Check GPS coordinates match submitted location
     * 5. Calculate image hash and check for duplicates
     * 6. Generate report number and tracking ID via IDGen
     * 7. Enrich with user details and audit info
     * 8. Create workflow instance (status: PENDING_VALIDATION)
     * 9. Push to Kafka for async persistence
     * 10. Return created reports
     */
    public List<DogReport> create(DogReportRequest request) {
        log.info("Processing create request for {} reports", request.getDogReports().size());

        // Step 1-2: Validate MDMS data and business rules
        validator.validateCreate(request);

        // Step 3-5: Validate evidence files, extract EXIF, check duplicates
        validator.validateEvidence(request);

        // Step 6-7: Enrich with IDs, user details, audit info
        enrichment.enrichCreate(request);

        // Step 8: Initialize workflow (SUBMIT action -> PENDING_VALIDATION)
        workflowService.updateWorkflowStatus(request);

        // Step 9: Push to Kafka for async persistence
        producer.push(saveTopic, request);

        log.info("Created {} dog reports", request.getDogReports().size());
        return request.getDogReports();
    }

    /**
     * Update existing dog report(s).
     *
     * Flow (as per SDCRS_Update sequence diagram):
     * 1. Validate MDMS master data
     * 2. Validate business rules
     * 3. Fetch existing reports from DB
     * 4. Validate user has permission for action
     * 5. Validate capture photo if MARK_CAPTURED action
     * 6. Enrich with audit details
     * 7. Transition workflow based on action
     * 8. Set payout eligible if MARK_CAPTURED
     * 9. Push to Kafka for async persistence
     * 10. Trigger payout if status changed to CAPTURED
     */
    public List<DogReport> update(DogReportRequest request) {
        log.info("Processing update request for {} reports", request.getDogReports().size());

        // Step 1-2: Validate MDMS data and business rules
        validator.validateUpdate(request);

        // Step 3: Fetch existing reports
        List<DogReport> existingReports = getExistingReports(request);

        // Step 4: Validate permissions for requested action
        validator.validatePermissions(request, existingReports);

        // Step 5: Validate capture evidence if applicable
        validator.validateCaptureEvidence(request);

        // Step 6: Enrich with audit details
        enrichment.enrichUpdate(request, existingReports);

        // Step 7: Transition workflow
        workflowService.updateWorkflowStatus(request);

        // Step 8-9: Push to Kafka for async persistence
        producer.push(updateTopic, request);

        log.info("Updated {} dog reports", request.getDogReports().size());
        return request.getDogReports();
    }

    /**
     * Search dog reports with role-based filtering.
     *
     * Filtering rules:
     * - TEACHER: Can only see own reports
     * - VERIFIER: Can see reports in PENDING_VERIFICATION
     * - MC_OFFICER: Can see assigned reports
     * - MC_SUPERVISOR: Can see reports in ward/district
     */
    public List<DogReport> search(RequestInfo requestInfo, DogReportSearchCriteria criteria) {
        log.info("Searching reports with criteria: {}", criteria);

        // Apply role-based filtering
        enrichment.enrichSearchCriteria(requestInfo, criteria);

        // Query database
        return repository.search(criteria);
    }

    /**
     * Count total reports matching criteria.
     */
    public Integer count(RequestInfo requestInfo, DogReportSearchCriteria criteria) {
        // Apply role-based filtering
        enrichment.enrichSearchCriteria(requestInfo, criteria);

        return repository.count(criteria);
    }

    /**
     * Public tracking - no authentication required.
     * Returns sanitized response without PII.
     *
     * Flow (as per SDCRS_Track sequence diagram):
     * 1. Validate request has reportNumber OR trackingId
     * 2. Query database by identifier
     * 3. Sanitize response (remove PII)
     * 4. Get localized status descriptions
     * 5. Build timeline from workflow history
     * 6. Calculate estimated resolution
     */
    public TrackResponse track(TrackRequest request) {
        log.info("Processing track request: reportNumber={}, trackingId={}",
                request.getReportNumber(), request.getTrackingId());

        // Step 1: Validate request
        if (!request.isValid()) {
            throw new CustomException("INVALID_TRACK_REQUEST",
                    "Either reportNumber or trackingId is required");
        }

        // Step 2: Fetch report
        DogReport report = repository.findByTrackingIdentifier(
                request.getReportNumber(), request.getTrackingId());

        if (report == null) {
            throw new CustomException("REPORT_NOT_FOUND", "Report not found");
        }

        // Step 3-6: Build sanitized response
        return trackingService.buildTrackResponse(report, request.getLocale());
    }

    /**
     * Fetch existing reports for update validation.
     */
    private List<DogReport> getExistingReports(DogReportRequest request) {
        List<String> reportNumbers = request.getDogReports().stream()
                .map(DogReport::getReportNumber)
                .toList();

        DogReportSearchCriteria criteria = DogReportSearchCriteria.builder()
                .tenantId(request.getDogReports().get(0).getTenantId())
                .build();

        // Set IDs for direct lookup
        criteria.setIds(request.getDogReports().stream()
                .map(DogReport::getId)
                .toList());

        List<DogReport> existing = repository.search(criteria);

        if (existing.size() != request.getDogReports().size()) {
            throw new CustomException("REPORT_NOT_FOUND",
                    "One or more reports not found for update");
        }

        return existing;
    }
}
