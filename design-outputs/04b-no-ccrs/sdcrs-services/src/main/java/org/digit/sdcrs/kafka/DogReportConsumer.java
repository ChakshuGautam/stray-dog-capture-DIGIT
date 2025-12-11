package org.digit.sdcrs.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReport;
import org.digit.sdcrs.model.DogReportRequest;
import org.digit.sdcrs.service.PayoutService;
import org.digit.sdcrs.service.ValidationService;
import org.digit.sdcrs.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DogReportConsumer - Kafka consumer for dog report events.
 * Handles async processing: validation, payout triggers, notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DogReportConsumer {

    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final PayoutService payoutService;
    private final NotificationService notificationService;

    /**
     * Consume auto-validation messages.
     * Triggered after report creation to run automated validation.
     */
    @KafkaListener(topics = "${sdcrs.kafka.validation.topic}")
    public void consumeValidation(Map<String, Object> record) {
        log.info("Consuming validation message");

        try {
            DogReportRequest request = objectMapper.convertValue(record, DogReportRequest.class);

            for (DogReport report : request.getDogReports()) {
                validationService.runAutoValidation(request.getRequestInfo(), report);
            }
        } catch (Exception e) {
            log.error("Error processing validation message", e);
        }
    }

    /**
     * Consume payout trigger messages.
     * Triggered when report status changes to CAPTURED.
     */
    @KafkaListener(topics = "${sdcrs.kafka.payout-trigger.topic}")
    public void consumePayoutTrigger(Map<String, Object> record) {
        log.info("Consuming payout trigger message");

        try {
            DogReportRequest request = objectMapper.convertValue(record, DogReportRequest.class);
            payoutService.processPayout(request);
        } catch (Exception e) {
            log.error("Error processing payout trigger", e);
        }
    }

    /**
     * Consume notification messages.
     * Triggered on status changes to send SMS/Email notifications.
     */
    @KafkaListener(topics = "${sdcrs.kafka.notification.topic}")
    public void consumeNotification(Map<String, Object> record) {
        log.info("Consuming notification message");

        try {
            DogReportRequest request = objectMapper.convertValue(record, DogReportRequest.class);

            for (DogReport report : request.getDogReports()) {
                notificationService.sendStatusNotification(request.getRequestInfo(), report);
            }
        } catch (Exception e) {
            log.error("Error processing notification", e);
        }
    }

    /**
     * Consume payment completion events from Billing Service.
     * Updates payout status when payment is processed.
     */
    @KafkaListener(topics = "${sdcrs.kafka.payment-completed.topic}")
    public void consumePaymentCompleted(Map<String, Object> record) {
        log.info("Consuming payment completed message");

        try {
            String demandId = (String) record.get("demandId");
            String status = (String) record.get("status");
            payoutService.handlePaymentCompletion(demandId, status);
        } catch (Exception e) {
            log.error("Error processing payment completion", e);
        }
    }
}
