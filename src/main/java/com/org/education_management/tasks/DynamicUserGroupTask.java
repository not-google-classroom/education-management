package com.org.education_management.tasks;

import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DynamicUserGroupTask {

    private static Logger logger = Logger.getLogger(DynamicUserGroupTask.class.getName());
    public void execute() {
        logger.log(Level.INFO, "DynamicUserGroupScheduler going to start...");

        logger.log(Level.INFO, "DynamicUserGroupScheduler ended");
    }
}
