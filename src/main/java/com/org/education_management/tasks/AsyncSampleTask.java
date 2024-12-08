package com.org.education_management.tasks;

import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AsyncSampleTask {
    private static Logger logger = Logger.getLogger(AsyncSampleTask.class.getName());
    public void execute() {
        logger.log(Level.INFO, "SampleTask executed at " + System.currentTimeMillis());
    }
}
