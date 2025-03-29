package com.org.education_management.util;

import com.org.education_management.config.SchedulerConfig;
import com.org.education_management.database.DataBaseUtil;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DynamicSchedulerUtil {

    private static Logger logger = Logger.getLogger(DynamicSchedulerUtil.class.getName());
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> runningSchedulers = new HashMap<>();
    private final Map<String, Long> executionCounts = new HashMap<>();
    private static DynamicSchedulerUtil dynamicSchedulerUtil = null;

    public DynamicSchedulerUtil() {
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("DynamicScheduler-");
        taskScheduler.initialize();
    }

    public static DynamicSchedulerUtil getInstance(){
        if(dynamicSchedulerUtil == null){
            dynamicSchedulerUtil = new DynamicSchedulerUtil();
        }
        return dynamicSchedulerUtil;
    }

    /**
     * Create infinite schedulers based on cron expressions.
     */
    public void addOrUpdateSchedulers(Map<String, SchedulerConfig> infiniteSchedulers, Long orgID, String schemaName) {
        infiniteSchedulers.forEach((name, config) -> {
            try {
                if(runningSchedulers.containsKey(name)){
                    stopScheduler(name);
                }
                if (config.getMaxRuns() < 0) {
                    createInfiniteTimeScheduler(name, config, orgID, schemaName);
                }
                else {
                    createLimitedTimeScheduler(name, config, orgID, schemaName);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Failed to add or update scheduler: " + name + ".", e);
            }
        });
    }

    private void createInfiniteTimeScheduler(String name, SchedulerConfig config, Long orgID, String schemaName) {
        logger.log(Level.INFO,"Creating infinite scheduler: " + name + ", orgID : "+ orgID);

        executionCounts.put(name, 0L);

        // Schedule the task
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                logger.log(Level.INFO,"Executing infinite task for " + name + " for orgID : " + orgID +", at " + System.currentTimeMillis());
                SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
                executeTask(config.getTaskClass());
                long currentCount = executionCounts.get(name);
                executionCounts.put(name, currentCount + 1L);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error while executing limited task for " + name + ", orgID : " + orgID + ", Exception : ", e);
                throw new RuntimeException(e);
            }  finally {
                SchemaUtil.getInstance().setSearchPathToPublic();
            }
        }, new CronTrigger(config.getCronString()));

        // Store the ScheduledFuture for later reference
        runningSchedulers.put(name, future);
    }

    private void createLimitedTimeScheduler(String name, SchedulerConfig config, Long orgID, String schemaName) throws IllegalArgumentException {
        logger.log(Level.INFO,"Creating limited scheduler: " + name + ", orgID : " + orgID);

        // Reset execution count for this scheduler
        executionCounts.put(name, 0L);

        // Schedule the task
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                long currentCount = executionCounts.get(name);
                SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
                logger.log(Level.INFO,"Executing limited task for " + name + " for orgID : " + orgID +", Run " + (currentCount + 1));
                executeTask(config.getTaskClass());
                // Increment execution count
                executionCounts.put(name, currentCount + 1L);

                // Stop scheduler if maxRuns is reached
                if (currentCount + 1 >= config.getMaxRuns()) {
                    logger.log(Level.INFO,"Stopping scheduler: " + name + " for orgID : " + orgID +" , max run reached");
                    stopScheduler(name);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"Error while executing limited task for " + name + ", orgID : " + orgID + ", Exception : ", e);
                throw new RuntimeException(e);
            } finally {
                SchemaUtil.getInstance().setSearchPathToPublic();
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
                logger.log(Level.INFO,"Scheduler " + name + " stopped.");
            } else {
                throw new IllegalArgumentException("Scheduler with name '" + name + "' does not exist.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Failed to stop scheduler: " + name + ".",e);
        }
    }

    /**
     * Stop all running schedulers.
     */
    public void stopAllSchedulers() {
        try {
            runningSchedulers.keySet().forEach(this::stopScheduler);
            logger.log(Level.INFO,"All schedulers have been stopped.");
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error occurred while stopping all schedulers: ", e);
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

    /**
     * Load scheduler configurations from a persistent store.
     */
    public void loadDefaultSchedulersFromDatabase(Long orgID, String schemaName) {
        try {
            Map<String, SchedulerConfig> schedulerList = new HashMap<>();
            Result<Record> result = DataBaseUtil.getDSLContext().select().from("scheduler").fetch();
            for (Record record : result){
                SchedulerConfig config = new SchedulerConfig((String) record.get("scheduler_cron"),
                        (long) record.get("scheduler_run_time"), (String) record.get("task_name"));
                schedulerList.put((String) record.get("scheduler_name"), config);
            }
            addOrUpdateSchedulers(schedulerList, orgID, schemaName);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error occurred while loading schedulers from database: ", e);
        }
    }

    private void executeTask(String className) throws Exception {
        try {
            Object object = CommonUtil.getInstance().getObjForClassName(className);
            Method executeMethod = object.getClass().getMethod("execute");
            executeMethod.invoke(object);
        }
        catch (Exception e){
            logger.log(Level.SEVERE,"Exception while executing task name "+className, e);
            throw e;
        }
    }
}
