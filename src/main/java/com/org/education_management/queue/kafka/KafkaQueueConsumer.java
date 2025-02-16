package com.org.education_management.queue.kafka;

import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.QueueProcessor;
import com.org.education_management.util.CommonUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueConsumer {

    private final KafkaQueue queueRegistry;

    public KafkaQueueConsumer(KafkaQueue queueRegistry, QueueProcessor processorFactory) {
        this.queueRegistry = queueRegistry;
    }

    @KafkaListener(topics = "#{'${kafka.topics}'.split(',')}", groupId = "dynamic-group")
    public void consume(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        QueueObject queue = queueRegistry.getQueue(topic);
        if (queue != null) {
            Object instance = CommonUtil.getInstance().getObjForClassName(queue.getProcessorClassName());

            if (instance != null && !(instance instanceof QueueProcessor)) {
                throw new IllegalArgumentException(queue.getProcessorClassName()
                        + " does not implement QueueProcessor");
            }

            QueueProcessor processor = (QueueProcessor) instance;
            processor.process(message);
        } else {
            System.out.println("No processor found for topic: " + topic);
        }
    }
}

