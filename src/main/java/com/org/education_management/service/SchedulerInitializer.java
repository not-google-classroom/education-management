package com.org.education_management.service;

import com.org.education_management.config.SchedulerConfig;
import com.org.education_management.util.DynamicSchedulerUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SchedulerInitializer implements CommandLineRunner {
    private final DynamicSchedulerUtil schedulerManager;

    public SchedulerInitializer(DynamicSchedulerUtil schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @Override
    public void run(String... args) {
        // Initialize schedulers at startup
        Map<String, SchedulerConfig> schedulerList = new HashMap<>();
        //Take date from the table populated
        schedulerManager.addOrUpdateSchedulers(schedulerList);
    }
}
