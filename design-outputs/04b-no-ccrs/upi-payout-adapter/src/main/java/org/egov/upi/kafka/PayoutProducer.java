package org.egov.upi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.upi.config.UpiAdapterConfiguration;
import org.egov.upi.web.models.Payout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PayoutProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UpiAdapterConfiguration config;
    private final ObjectMapper objectMapper;

    @Autowired
    public PayoutProducer(KafkaTemplate<String, Object> kafkaTemplate,
                          UpiAdapterConfiguration config,
                          ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Push payouts to persister topic
     */
    public void pushToPersist(List<Payout> payouts) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("payouts", payouts);

            kafkaTemplate.send(config.getPersistTopic(), message);
            log.info("Pushed {} payout(s) to persist topic: {}",
                payouts.size(), config.getPersistTopic());
        } catch (Exception e) {
            log.error("Error pushing to persist topic: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to push to Kafka", e);
        }
    }

    /**
     * Push status update to status topic
     */
    public void pushStatusUpdate(Payout payout) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("payout", payout);
            message.put("event", "STATUS_UPDATE");
            message.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send(config.getStatusUpdateTopic(), message);
            log.info("Pushed status update for payout {} to topic: {}",
                payout.getPayoutNumber(), config.getStatusUpdateTopic());
        } catch (Exception e) {
            log.error("Error pushing status update: {}", e.getMessage(), e);
        }
    }

    /**
     * Push error event to error topic
     */
    public void pushError(Payout payout, String errorMessage) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("payout", payout);
            message.put("event", "ERROR");
            message.put("errorMessage", errorMessage);
            message.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send(config.getErrorTopic(), message);
            log.info("Pushed error event for payout {} to topic: {}",
                payout.getPayoutNumber(), config.getErrorTopic());
        } catch (Exception e) {
            log.error("Error pushing to error topic: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify SDCRS service of payout completion/failure
     */
    public void pushToSdcrsCallback(Payout payout) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("referenceId", payout.getReferenceId());
            message.put("beneficiaryId", payout.getBeneficiaryId());
            message.put("payoutNumber", payout.getPayoutNumber());
            message.put("status", payout.getStatus().getValue());
            message.put("utr", payout.getUtr());
            message.put("amount", payout.getAmount());
            message.put("failureReason", payout.getFailureReason());
            message.put("processedAt", payout.getProcessedAt());
            message.put("event", "PAYOUT_STATUS_CHANGE");
            message.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send(config.getSdcrsCallbackTopic(), message);
            log.info("Pushed SDCRS callback for payout {} with status: {}",
                payout.getPayoutNumber(), payout.getStatus());
        } catch (Exception e) {
            log.error("Error pushing to SDCRS callback topic: {}", e.getMessage(), e);
        }
    }
}
