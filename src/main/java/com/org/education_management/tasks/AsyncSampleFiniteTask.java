package com.org.education_management.tasks;

import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AsyncSampleFiniteTask {
    private static Logger logger = Logger.getLogger(AsyncSampleFiniteTask.class.getName());
    public void execute() {
        logger.log(Level.INFO,"SampleFiniteTask executed at " + System.currentTimeMillis());
    }
}
