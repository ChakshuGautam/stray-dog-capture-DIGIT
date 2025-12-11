package org.digit.sdcrs.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digit.sdcrs.model.DogReportRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * DogReportProducer - Kafka producer for dog report events.
 * Publishes to topics consumed by Persister, Indexer, and other services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DogReportProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Push message to Kafka topic.
     *
     * @param topic Topic name
     * @param request Request to publish
     */
    public void push(String topic, DogReportRequest request) {
        log.info("Pushing {} reports to topic: {}",
                request.getDogReports().size(), topic);
        kafkaTemplate.send(topic, request);
    }

    /**
     * Push message with key for ordering.
     *
     * @param topic Topic name
     * @param key Partition key (e.g., reportNumber)
     * @param request Request to publish
     */
    public void push(String topic, String key, DogReportRequest request) {
        log.info("Pushing to topic: {} with key: {}", topic, key);
        kafkaTemplate.send(topic, key, request);
    }

    /**
     * Push generic object to topic.
     *
     * @param topic Topic name
     * @param message Message to publish
     */
    public void pushGeneric(String topic, Object message) {
        log.info("Pushing message to topic: {}", topic);
        kafkaTemplate.send(topic, message);
    }
}
