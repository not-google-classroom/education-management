package com.org.education_management.queue.kafka;

import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.QueueProcessor;
import com.org.education_management.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class KafkaQueueConsumer {

    private static final Logger LOGGER = Logger.getLogger(KafkaQueueConsumer.class.getName());
    private static KafkaQueue queueRegistry;

    @Autowired
    public KafkaQueueConsumer(KafkaQueue queueRegistry) {
        this.queueRegistry = queueRegistry;
    }

    @KafkaListener(topics = "#{'${kafka.topics:default-topic}'.split(',')}", groupId = "dynamic-group")
    public void consume(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        LOGGER.log(Level.INFO, "Message received from topic: {0} | Message: {1}", new Object[]{topic, message});
        processMessage(topic, message);
    }

    public static void processMessage(String topic, String message) {
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
            LOGGER.log(Level.WARNING, "No processor found for topic: {0}", topic);
        }
    }
}
