package com.fleetix.fleetservice.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentReadinessEventPublisher {

    private final KafkaTemplate<String, PaymentReadinessEvent> kafkaTemplate;
    private final String topic;

    public PaymentReadinessEventPublisher(
            KafkaTemplate<String, PaymentReadinessEvent> kafkaTemplate,
            @Value("${app.kafka.topic.payment-readiness}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PaymentReadinessEvent event) {
        kafkaTemplate.send(topic, event.requestId().toString(), event);
    }
}
