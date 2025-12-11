package org.egov.upi.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.service.PayoutService;
import org.egov.upi.util.PayoutConstants;
import org.egov.upi.web.models.WebhookPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/webhook/v1")
@Slf4j
public class WebhookController {

    private final PayoutService payoutService;
    private final UpiAdapterConfiguration config;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebhookController(PayoutService payoutService,
                              UpiAdapterConfiguration config,
                              ObjectMapper objectMapper) {
        this.payoutService = payoutService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Razorpay webhook endpoint
     * POST /webhook/v1/razorpay
     */
    @PostMapping("/razorpay")
    public ResponseEntity<Map<String, String>> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        log.info("Received Razorpay webhook");

        // Verify webhook signature
        if (config.getWebhookEnabled() && !verifyWebhookSignature(payload, signature)) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", "error", "message", "Invalid signature"));
        }

        try {
            WebhookPayload webhookPayload = objectMapper.readValue(payload, WebhookPayload.class);
            String event = webhookPayload.getEvent();

            log.info("Processing webhook event: {}", event);

            switch (event) {
                case PayoutConstants.EVENT_PAYOUT_PROCESSED:
                    handlePayoutProcessed(webhookPayload);
                    break;
                case PayoutConstants.EVENT_PAYOUT_FAILED:
                    handlePayoutFailed(webhookPayload);
                    break;
                case PayoutConstants.EVENT_PAYOUT_REVERSED:
                    handlePayoutReversed(webhookPayload);
                    break;
                default:
                    log.info("Ignoring unhandled webhook event: {}", event);
            }

            return ResponseEntity.ok(Map.of("status", "success"));

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Handle payout.processed event
     */
    private void handlePayoutProcessed(WebhookPayload payload) {
        WebhookPayload.PayoutDetails payout = payload.getPayload().getPayout().getEntity();

        log.info("Processing payout.processed for ID: {}", payout.getId());

        payoutService.updatePayoutStatus(
            payout.getId(),
            payout.getStatus(),
            payout.getUtr(),
            null,
            createSystemRequestInfo()
        );
    }

    /**
     * Handle payout.failed event
     */
    private void handlePayoutFailed(WebhookPayload payload) {
        WebhookPayload.PayoutDetails payout = payload.getPayload().getPayout().getEntity();

        log.info("Processing payout.failed for ID: {}", payout.getId());

        String failureReason = payout.getFailureReason();
        if (payout.getError() != null) {
            failureReason = payout.getError().getDescription();
        }

        payoutService.updatePayoutStatus(
            payout.getId(),
            payout.getStatus(),
            null,
            failureReason,
            createSystemRequestInfo()
        );
    }

    /**
     * Handle payout.reversed event
     */
    private void handlePayoutReversed(WebhookPayload payload) {
        WebhookPayload.PayoutDetails payout = payload.getPayload().getPayout().getEntity();

        log.info("Processing payout.reversed for ID: {}", payout.getId());

        payoutService.updatePayoutStatus(
            payout.getId(),
            "reversed",
            payout.getUtr(),
            "Payment reversed by bank",
            createSystemRequestInfo()
        );
    }

    /**
     * Verify Razorpay webhook signature
     */
    private boolean verifyWebhookSignature(String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }

        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                config.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(hash);

            return expectedSignature.equals(signature);

        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "upi-payout-adapter-webhook"
        ));
    }

    private RequestInfo createSystemRequestInfo() {
        User systemUser = User.builder()
            .uuid("SYSTEM")
            .type("SYSTEM")
            .build();

        return RequestInfo.builder()
            .apiId("upi-webhook")
            .ver("1.0")
            .ts(System.currentTimeMillis())
            .msgId("webhook-" + System.currentTimeMillis())
            .userInfo(systemUser)
            .build();
    }
}
