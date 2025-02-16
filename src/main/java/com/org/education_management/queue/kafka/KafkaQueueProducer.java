package com.org.education_management.queue.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaQueueProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String queueName, String message) {
        kafkaTemplate.send(queueName, message);
    }
}
