package com.org.education_management.util;

import com.org.education_management.model.QueueConfig;
import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.kafka.KafkaQueue;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueueUtil {
    private static Logger logger = Logger.getLogger(QueueUtil.class.getName());
    private static QueueUtil queueUtil = null;

    public static QueueUtil getInstance(){
        if(queueUtil == null){
            queueUtil = new QueueUtil();
        }
        return queueUtil;
    }
    public static void addQueues() {
        try{
            String queueStr = AppProperty.getInstance().getProperty("kafka.topics");
            List<String> queues = Arrays.asList(queueStr.split(","));
            String fullFilePath = FileHandler.getHomeDir() + FileHandler.getFileSeparator()
                    + "resources" + FileHandler.getFileSeparator() + "Queues" + FileHandler.getFileSeparator() + "queue-data.json";
            List<QueueObject> queueList = FileHandler.readJsonFile(fullFilePath, QueueConfig.class).getQueues();
            for(QueueObject queueObject : queueList){
                if(queues.contains(queueObject.getQueueName())) {
                    KafkaQueue queue = new KafkaQueue();
                    queue.createQueue(queueObject);
                }
                else {
                    logger.log(Level.WARNING, "{0} not present in application properties",
                            new Object[]{queueObject.getQueueName()});
                }
            }
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Exception while adding queues", e);
        }
    }
}
