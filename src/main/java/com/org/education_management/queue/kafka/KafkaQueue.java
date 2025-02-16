package com.org.education_management.queue.kafka;

import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.Queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class KafkaQueue implements Queue {
    private static Logger logger = Logger.getLogger(KafkaQueue.class.getName());
    private final Map<String, QueueObject> activeQueues = new ConcurrentHashMap<>();

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
}
