package com.org.education_management.Listener;

import com.org.education_management.queue.QueueProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DynamicQueueListener {

    @Value("${dynamic.topics:}")
    private String[] topics;

    @KafkaListener(topics = "#{T(java.util.Arrays).asList('${dynamic.topics:}').toArray()}", groupId = "dynamic-queue-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }

    public void invokeProcessor(String processorClassName, String message) {
        try {
            Class<?> clazz = Class.forName(processorClassName);

            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof QueueProcessor)) {
                throw new IllegalArgumentException(processorClassName + " does not implement QueueProcessor");
            }

            QueueProcessor processor = (QueueProcessor) instance;
            processor.process(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to invoke processor: " + processorClassName);
        }
    }
}

