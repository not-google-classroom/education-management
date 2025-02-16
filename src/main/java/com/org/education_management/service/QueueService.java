package com.org.education_management.service;

import com.org.education_management.model.DynamicQueue;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueueService {
    private final ConcurrentHashMap<String, DynamicQueue> queues = new ConcurrentHashMap<>();

    public void addQueue(DynamicQueue queue) {
        queues.put(queue.getQueueName(), queue);
        System.out.println("Queue added: " + queue.getQueueName());
    }

    public void removeQueue(String queueName) {
        queues.remove(queueName);
        System.out.println("Queue removed: " + queueName);
    }

    public DynamicQueue getQueue(String queueName) {
        return queues.get(queueName);
    }
}
