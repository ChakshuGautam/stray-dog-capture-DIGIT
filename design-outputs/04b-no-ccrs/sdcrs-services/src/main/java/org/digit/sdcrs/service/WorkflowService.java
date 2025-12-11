package org.digit.sdcrs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.config.SDCRSConfiguration;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportRequest;
import org.egov.common.contract.models.Workflow;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkflowService - Integration with DIGIT Workflow Service.
 *
 * Manages workflow state transitions for dog reports:
 * - SUBMIT -> PENDING_VALIDATION
 * - AUTO_VALIDATE_PASS -> PENDING_VERIFICATION
 * - VERIFY -> VERIFIED
 * - ASSIGN_MC -> ASSIGNED
 * - MARK_CAPTURED -> CAPTURED
 * - PROCESS_PAYOUT -> RESOLVED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SDCRSConfiguration config;

    private static final String BUSINESS_SERVICE = "SDCRS";

    // Workflow actions
    public static final String ACTION_SUBMIT = "SUBMIT";
    public static final String ACTION_AUTO_VALIDATE_PASS = "AUTO_VALIDATE_PASS";
    public static final String ACTION_AUTO_VALIDATE_FAIL = "AUTO_VALIDATE_FAIL";
    public static final String ACTION_VERIFY = "VERIFY";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_MARK_DUPLICATE = "MARK_DUPLICATE";
    public static final String ACTION_ASSIGN_MC = "ASSIGN_MC";
    public static final String ACTION_START_FIELD_VISIT = "START_FIELD_VISIT";
    public static final String ACTION_MARK_CAPTURED = "MARK_CAPTURED";
    public static final String ACTION_MARK_UNABLE_TO_LOCATE = "MARK_UNABLE_TO_LOCATE";
    public static final String ACTION_PROCESS_PAYOUT = "PROCESS_PAYOUT";

    // Workflow statuses
    public static final String STATUS_PENDING_VALIDATION = "PENDING_VALIDATION";
    public static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_CAPTURED = "CAPTURED";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_AUTO_REJECTED = "AUTO_REJECTED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";
    public static final String STATUS_UNABLE_TO_LOCATE = "UNABLE_TO_LOCATE";

    /**
     * Update workflow status for dog reports.
     * Calls DIGIT Workflow Service transition API.
     */
    public void updateWorkflowStatus(DogReportRequest request) {
        log.info("Updating workflow status for {} reports", request.getDogReports().size());

        for (DogReport report : request.getDogReports()) {
            Workflow workflow = report.getWorkflow();
            if (workflow == null || workflow.getAction() == null) {
                // For new reports, default to SUBMIT action
                workflow = Workflow.builder()
                        .action(ACTION_SUBMIT)
                        .build();
                report.setWorkflow(workflow);
            }

            // Call workflow service
            ProcessInstanceResponse wfResponse = callWorkflowTransition(
                    request.getRequestInfo(), report);

            // Update report status from workflow response
            if (wfResponse != null && wfResponse.getProcessInstances() != null
                    && !wfResponse.getProcessInstances().isEmpty()) {

                ProcessInstance pi = wfResponse.getProcessInstances().get(0);
                report.setStatus(pi.getState().getState());
            }
        }
    }

    /**
     * Get workflow business service configuration.
     */
    public BusinessService getBusinessService(RequestInfo requestInfo, String tenantId) {
        String url = config.getWfHost() + config.getWfBusinessServiceSearchPath();

        Map<String, Object> request = new HashMap<>();
        request.put("RequestInfo", requestInfo);
        request.put("tenantId", tenantId);
        request.put("businessServices", List.of(BUSINESS_SERVICE));

        try {
            BusinessServiceResponse response = restTemplate.postForObject(
                    url, request, BusinessServiceResponse.class);

            if (response != null && response.getBusinessServices() != null
                    && !response.getBusinessServices().isEmpty()) {
                return response.getBusinessServices().get(0);
            }
        } catch (Exception e) {
            log.error("Error fetching business service", e);
            throw new CustomException("WORKFLOW_ERROR", "Error fetching workflow configuration");
        }

        throw new CustomException("BUSINESS_SERVICE_NOT_FOUND",
                "Business service not found: " + BUSINESS_SERVICE);
    }

    /**
     * Call workflow transition API.
     */
    private ProcessInstanceResponse callWorkflowTransition(
            RequestInfo requestInfo, DogReport report) {

        String url = config.getWfHost() + config.getWfTransitionPath();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(
                        ProcessInstance.builder()
                                .businessId(report.getReportNumber())
                                .businessService(BUSINESS_SERVICE)
                                .tenantId(report.getTenantId())
                                .action(report.getWorkflow().getAction())
                                .comment(report.getWorkflow().getComments())
                                .assignees(report.getWorkflow().getAssignes())
                                .documents(report.getWorkflow().getDocuments())
                                .build()
                ))
                .build();

        try {
            return restTemplate.postForObject(url, wfRequest, ProcessInstanceResponse.class);
        } catch (Exception e) {
            log.error("Error calling workflow transition", e);
            throw new CustomException("WORKFLOW_ERROR",
                    "Error transitioning workflow: " + e.getMessage());
        }
    }

    /**
     * Check if action is valid for current state.
     */
    public boolean isValidAction(String currentStatus, String action) {
        return switch (currentStatus) {
            case STATUS_PENDING_VALIDATION ->
                    ACTION_AUTO_VALIDATE_PASS.equals(action) || ACTION_AUTO_VALIDATE_FAIL.equals(action);
            case STATUS_PENDING_VERIFICATION ->
                    ACTION_VERIFY.equals(action) || ACTION_REJECT.equals(action) || ACTION_MARK_DUPLICATE.equals(action);
            case STATUS_VERIFIED -> ACTION_ASSIGN_MC.equals(action);
            case STATUS_ASSIGNED -> ACTION_START_FIELD_VISIT.equals(action);
            case STATUS_IN_PROGRESS ->
                    ACTION_MARK_CAPTURED.equals(action) || ACTION_MARK_UNABLE_TO_LOCATE.equals(action);
            case STATUS_CAPTURED -> ACTION_PROCESS_PAYOUT.equals(action);
            default -> false;
        };
    }

    // Inner classes for workflow API
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessInstanceRequest {
        private RequestInfo requestInfo;
        private List<ProcessInstance> processInstances;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessInstanceResponse {
        private List<ProcessInstance> processInstances;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessInstance {
        private String id;
        private String businessId;
        private String businessService;
        private String tenantId;
        private String action;
        private String comment;
        private State state;
        private List<String> assignees;
        private List<Object> documents;
    }

    @lombok.Data
    public static class State {
        private String state;
        private String applicationStatus;
        private Boolean isTerminateState;
    }

    @lombok.Data
    public static class BusinessServiceResponse {
        private List<BusinessService> businessServices;
    }

    @lombok.Data
    public static class BusinessService {
        private String businessService;
        private String business;
        private List<State> states;
    }
}
