package org.egov.upi.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.upi.service.PayoutService;
import org.egov.upi.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/payout/v1")
@Slf4j
public class PayoutController {

    private final PayoutService payoutService;

    @Autowired
    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * Create new payout(s)
     * POST /payout/v1/_create
     */
    @PostMapping("/_create")
    public ResponseEntity<PayoutResponse> createPayout(@Valid @RequestBody PayoutRequest request) {
        log.info("Received payout create request with {} payout(s)",
            request.getPayouts().size());

        List<Payout> payouts = payoutService.createPayouts(request);

        PayoutResponse response = PayoutResponse.builder()
            .responseInfo(createResponseInfo(request.getRequestInfo()))
            .payouts(payouts)
            .totalCount(payouts.size())
            .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Search payouts
     * POST /payout/v1/_search
     */
    @PostMapping("/_search")
    public ResponseEntity<PayoutResponse> searchPayouts(@Valid @RequestBody PayoutSearchRequest request) {
        log.info("Received payout search request");

        List<Payout> payouts = payoutService.searchPayouts(request);

        PayoutResponse response = PayoutResponse.builder()
            .responseInfo(createResponseInfo(request.getRequestInfo()))
            .payouts(payouts)
            .totalCount(payouts.size())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retry a failed payout
     * POST /payout/v1/_retry
     */
    @PostMapping("/_retry")
    public ResponseEntity<PayoutResponse> retryPayout(
            @RequestParam String payoutId,
            @Valid @RequestBody PayoutSearchRequest request) {
        log.info("Received retry request for payout: {}", payoutId);

        Payout payout = payoutService.retryPayout(payoutId, request.getRequestInfo());

        PayoutResponse response = PayoutResponse.builder()
            .responseInfo(createResponseInfo(request.getRequestInfo()))
            .payouts(List.of(payout))
            .totalCount(1)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get payout by ID
     * GET /payout/v1/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse> getPayoutById(@PathVariable String id) {
        log.info("Received get request for payout: {}", id);

        PayoutSearchCriteria criteria = PayoutSearchCriteria.builder()
            .ids(List.of(id))
            .build();

        PayoutSearchRequest request = PayoutSearchRequest.builder()
            .requestInfo(null)
            .criteria(criteria)
            .build();

        List<Payout> payouts = payoutService.searchPayouts(request);

        PayoutResponse response = PayoutResponse.builder()
            .payouts(payouts)
            .totalCount(payouts.size())
            .build();

        return ResponseEntity.ok(response);
    }

    private ResponseInfo createResponseInfo(org.egov.common.contract.request.RequestInfo requestInfo) {
        return ResponseInfo.builder()
            .apiId(requestInfo != null ? requestInfo.getApiId() : "upi-payout-adapter")
            .ver(requestInfo != null ? requestInfo.getVer() : "1.0")
            .ts(System.currentTimeMillis())
            .resMsgId("upi-" + System.currentTimeMillis())
            .msgId(requestInfo != null ? requestInfo.getMsgId() : null)
            .status("successful")
            .build();
    }
}
