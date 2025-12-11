package org.digit.sdcrs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.*;
import org.digit.sdcrs.service.DogReportService;
import org.digit.sdcrs.util.ResponseInfoFactory;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * DogReportController - REST API controller for Dog Report operations.
 *
 * Endpoints:
 * - POST /v1/report/_create  - Create new dog report(s)
 * - POST /v1/report/_update  - Update existing dog report(s)
 * - POST /v1/report/_search  - Search dog reports
 * - POST /v1/report/_track   - Public tracking (no auth required)
 */
@Slf4j
@RestController
@RequestMapping("/v1/report")
@RequiredArgsConstructor
@Validated
public class DogReportController {

    private final DogReportService dogReportService;
    private final ResponseInfoFactory responseInfoFactory;

    /**
     * Create new dog report(s).
     * Used by Teachers to submit new stray dog sightings.
     *
     * @param request DogReportRequest containing report details
     * @return Created dog reports with generated IDs
     */
    @PostMapping("/_create")
    public ResponseEntity<DogReportResponse> create(
            @Valid @RequestBody DogReportRequest request) {

        log.info("Creating {} dog report(s) for tenant: {}",
                request.getDogReports().size(),
                request.getDogReports().get(0).getTenantId());

        List<DogReport> createdReports = dogReportService.create(request);

        DogReportResponse response = DogReportResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfo(
                        request.getRequestInfo(), true))
                .dogReports(createdReports)
                .totalCount(createdReports.size())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update existing dog report(s).
     * Used for workflow transitions: verify, assign, resolve, etc.
     *
     * @param request DogReportRequest with updated report details
     * @return Updated dog reports
     */
    @PostMapping("/_update")
    public ResponseEntity<DogReportResponse> update(
            @Valid @RequestBody DogReportRequest request) {

        log.info("Updating {} dog report(s)", request.getDogReports().size());

        List<DogReport> updatedReports = dogReportService.update(request);

        DogReportResponse response = DogReportResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfo(
                        request.getRequestInfo(), true))
                .dogReports(updatedReports)
                .totalCount(updatedReports.size())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search dog reports with filters.
     * Applies role-based filtering:
     * - TEACHER: own reports only
     * - VERIFIER: reports in PENDING_VERIFICATION
     * - MC_OFFICER: assigned reports
     * - MC_SUPERVISOR: reports in ward/district
     *
     * @param requestInfo Standard DIGIT request info
     * @param criteria Search criteria
     * @return Matching dog reports
     */
    @PostMapping("/_search")
    public ResponseEntity<DogReportResponse> search(
            @Valid @RequestBody RequestInfo requestInfo,
            @Valid @ModelAttribute DogReportSearchCriteria criteria) {

        log.info("Searching dog reports with criteria: {}", criteria);

        List<DogReport> reports = dogReportService.search(requestInfo, criteria);
        Integer totalCount = dogReportService.count(requestInfo, criteria);

        DogReportResponse response = DogReportResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfo(requestInfo, true))
                .dogReports(reports)
                .totalCount(totalCount)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Public tracking endpoint (NO AUTHENTICATION REQUIRED).
     * Whitelisted in gateway via egov-open-endpoints-whitelist.
     *
     * Returns sanitized response without PII:
     * - No reporter name/phone
     * - No officer name/phone
     * - No evidence URLs
     *
     * @param request TrackRequest with reportNumber OR trackingId
     * @return Sanitized tracking response
     */
    @PostMapping("/_track")
    public ResponseEntity<TrackResponse> track(
            @Valid @RequestBody TrackRequest request) {

        log.info("Public tracking request: reportNumber={}, trackingId={}",
                request.getReportNumber(), request.getTrackingId());

        TrackResponse response = dogReportService.track(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
