package com.org.education_management.queue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleQueueProcessor implements QueueProcessor {
    private static Logger logger = Logger.getLogger(SampleQueueProcessor.class.getName());
    @Override
    public void process(String message) {
        logger.log(Level.INFO, "Queue processed successfully - " + message);
    }
}
