package com.org.education_management.util;

import com.org.education_management.model.QueueObject;
import com.org.education_management.queue.kafka.KafkaQueue;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueueUtil {
    private static Logger logger = Logger.getLogger(QueueUtil.class.getName());
    public static void addQueues() {
        try{
            String fullFilePath = FileHandler.getHomeDir() + FileHandler.getFileSeparator()
                    + "resources" + FileHandler.getFileSeparator() + "queue-data.json";
            //todo this will not work need to revamp
            List<QueueObject> queueList = Collections.singletonList(FileHandler.readJsonFile(fullFilePath, QueueObject.class));
            for(QueueObject queueObject : queueList){
                KafkaQueue queue = new KafkaQueue();
                queue.createQueue(queueObject);
            }
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Exception while adding queues", e);
        }
    }
}
