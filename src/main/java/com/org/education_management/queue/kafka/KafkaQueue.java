package com.org.education_management.queue.kafka;

import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KafkaQueue implements Queue {
    private static Map<String, QueueObject> activeQueues = new ConcurrentHashMap<>();
    private static KafkaQueueProducer kafkaProducerService;
    @Autowired
    public void setKafkaProducerService(KafkaQueueProducer kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }
    @Override
    public void createQueue(QueueObject queueObject) {
        activeQueues.put(queueObject.getQueueName(), queueObject);
    }

    @Override
    public QueueObject getQueue(String queue) {
        return activeQueues.get(queue);
    }

    @Override
    public void removeQueue(String queueName) {
        activeQueues.remove(queueName);
    }

    @Override
    public void send(String queueName, String message) {
        kafkaProducerService.sendMessage(queueName, message);
    }
}
