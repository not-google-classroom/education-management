package com.org.education_management.util;

import com.org.education_management.config.SchedulerConfig;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
public class DynamicSchedulerUtil {

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> runningSchedulers = new HashMap<>();
    private final Map<String, Long> executionCounts = new HashMap<>();

    public DynamicSchedulerUtil() {
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("DynamicScheduler-");
        taskScheduler.initialize();
    }

    /**
     * Create infinite schedulers based on cron expressions.
     */
    public void addOrUpdateSchedulers(Map<String, SchedulerConfig> infiniteSchedulers) {
        infiniteSchedulers.forEach((name, config) -> {
            try {
                if(runningSchedulers.containsKey(name)){
                    stopScheduler(name);
                }
                if (config.getMaxRuns() < 0) {
                    createInfiniteTimeScheduler(name, config);
                }
                else {
                    createLimitedTimeScheduler(name, config);
                }
            } catch (Exception e) {
                System.err.println("Failed to add or update scheduler: " + name + ". Reason: " + e.getMessage());
            }
        });
    }

    private void createInfiniteTimeScheduler(String name, SchedulerConfig config) {
        System.out.println("Creating infinite scheduler: " + name);

        executionCounts.put(name, 0L);

        // Schedule the task
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                System.out.println("Executing infinite task for " + name + " at: " + System.currentTimeMillis());
                long currentCount = executionCounts.get(name);
                executionCounts.put(name, currentCount + 1L);
            }
            catch (Exception e) {
                System.err.println("Error while executing task for " + name + ": " + e.getMessage());
            }
        }, new CronTrigger(config.getCronString()));

        // Store the ScheduledFuture for later reference
        runningSchedulers.put(name, future);
    }

    private void createLimitedTimeScheduler(String name, SchedulerConfig config) throws IllegalArgumentException {
        System.out.println("Creating limited scheduler: " + name);

        // Reset execution count for this scheduler
        executionCounts.put(name, 0L);

        // Schedule the task
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                long currentCount = executionCounts.get(name);
                System.out.println("Executing limited task for " + name + ": Run " + (currentCount + 1));

                // Increment execution count
                executionCounts.put(name, currentCount + 1L);

                // Stop scheduler if maxRuns is reached
                if (currentCount + 1 >= config.getMaxRuns()) {
                    System.out.println("Stopping scheduler: " + name);
                    stopScheduler(name);
                }
            } catch (Exception e) {
                System.err.println("Error while executing limited task for " + name + ": " + e.getMessage());
            }
        }, new CronTrigger(config.getCronString()));

        // Store the ScheduledFuture for later reference
        runningSchedulers.put(name, future);
    }

    /**
     * Stop a specific scheduler by name.
     */
    public void stopScheduler(String name) {
        try {
            ScheduledFuture<?> future = runningSchedulers.get(name);
            if (future != null) {
                future.cancel(false); // Cancel the task
                runningSchedulers.remove(name);
                executionCounts.remove(name);
                System.out.println("Scheduler " + name + " stopped.");
            } else {
                throw new IllegalArgumentException("Scheduler with name '" + name + "' does not exist.");
            }
        } catch (Exception e) {
            System.err.println("Failed to stop scheduler: " + name + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Stop all running schedulers.
     */
    public void stopAllSchedulers() {
        try {
            runningSchedulers.keySet().forEach(this::stopScheduler);
            System.out.println("All schedulers have been stopped.");
        } catch (Exception e) {
            System.err.println("Error occurred while stopping all schedulers: " + e.getMessage());
        }
    }

//    /**
//     * Save scheduler configurations to a persistent store.
//     */
//    public void saveSchedulersToDatabase() {
//        try {
//            System.out.println("Saving scheduler configurations to database...");
//            runningSchedulers.keySet().forEach(name -> {
//                // Simulate saving to a database
//                String cron = "Sample Cron Expression"; // Retrieve cron dynamically if stored
//                System.out.println("Saved scheduler: " + name + " with cron: " + cron);
//            });
//        } catch (Exception e) {
//            System.err.println("Error occurred while saving schedulers to database: " + e.getMessage());
//        }
//    }

//    /**
//     * Load scheduler configurations from a persistent store.
//     */
//    public void loadSchedulersFromDatabase(Map<String, SchedulerConfig> configs) {
//        try {
//            System.out.println("Loading scheduler configurations from database...");
//            configs.forEach((name, config) -> {
//                try {
//                    createInfiniteScheduler(name, config);
//                } catch (Exception e) {
//                    System.err.println("Failed to load scheduler: " + config.getCronString() + ". Reason: " + e.getMessage());
//                }
//            });
//        } catch (Exception e) {
//            System.err.println("Error occurred while loading schedulers from database: " + e.getMessage());
//        }
//    }
}
