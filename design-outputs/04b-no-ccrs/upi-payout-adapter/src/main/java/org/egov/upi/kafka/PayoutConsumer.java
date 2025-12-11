package org.egov.upi.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.upi.service.PayoutService;
import org.egov.upi.web.models.Payout;
import org.egov.upi.web.models.PayoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PayoutConsumer {

    private final PayoutService payoutService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PayoutConsumer(PayoutService payoutService, ObjectMapper objectMapper) {
        this.payoutService = payoutService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen for payout creation requests from SDCRS
     */
    @KafkaListener(topics = "${upi.kafka.topic.create}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenForPayoutCreate(Map<String, Object> message,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received payout create request from topic: {}", topic);

        try {
            // Extract payout data from message
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> payoutMaps = (List<Map<String, Object>>) message.get("payouts");
            Map<String, Object> requestInfoMap = (Map<String, Object>) message.get("RequestInfo");

            // Convert to objects
            List<Payout> payouts = payoutMaps.stream()
                .map(m -> objectMapper.convertValue(m, Payout.class))
                .toList();

            RequestInfo requestInfo = objectMapper.convertValue(requestInfoMap, RequestInfo.class);
            if (requestInfo == null) {
                requestInfo = createSystemRequestInfo();
            }

            // Create request and process
            PayoutRequest request = PayoutRequest.builder()
                .requestInfo(requestInfo)
                .payouts(payouts)
                .build();

            payoutService.createPayouts(request);

            log.info("Successfully processed {} payout(s) from Kafka", payouts.size());

        } catch (Exception e) {
            log.error("Error processing payout create message: {}", e.getMessage(), e);
            // TODO: Push to DLQ for retry
        }
    }

    /**
     * Listen for status update requests (from webhook or polling)
     */
    @KafkaListener(topics = "${upi.kafka.topic.status}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenForStatusUpdate(Map<String, Object> message,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received status update from topic: {}", topic);

        try {
            String providerPayoutId = (String) message.get("providerPayoutId");
            String status = (String) message.get("status");
            String utr = (String) message.get("utr");
            String failureReason = (String) message.get("failureReason");

            payoutService.updatePayoutStatus(
                providerPayoutId,
                status,
                utr,
                failureReason,
                createSystemRequestInfo()
            );

            log.info("Successfully updated payout status for provider ID: {}", providerPayoutId);

        } catch (Exception e) {
            log.error("Error processing status update message: {}", e.getMessage(), e);
        }
    }

    /**
     * Create system RequestInfo for async processing
     */
    private RequestInfo createSystemRequestInfo() {
        User systemUser = User.builder()
            .uuid("SYSTEM")
            .type("SYSTEM")
            .build();

        return RequestInfo.builder()
            .apiId("upi-payout-adapter")
            .ver("1.0")
            .ts(System.currentTimeMillis())
            .msgId("async-" + System.currentTimeMillis())
            .userInfo(systemUser)
            .build();
    }
}
