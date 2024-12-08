package com.org.education_management;

import com.org.education_management.config.SchedulerConfig;
import com.org.education_management.controller.StartUpController;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.DatabaseInitializer;
import com.org.education_management.util.DynamicSchedulerUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@EnableScheduling
public class EducationManagementApplication {

    private static final Logger logger = Logger.getLogger(String.valueOf(EducationManagementApplication.class));

    public static void main(String[] args) {
        try {
            String dbUrl = AppProperty.getInstance().getProperty("spring.datasource.url.without");
            String user = AppProperty.getInstance().getProperty("spring.datasource.username");
            String pwd = AppProperty.getInstance().getProperty("spring.datasource.password");
            String dbName = AppProperty.getInstance().getProperty("spring.datasource.database");

            logger.log(Level.INFO, "Startup initiated...connecting to database");
            StartUpController controller = new StartUpController();
            boolean isFreshStart = controller.isFreshStart();
            if (isFreshStart) {
                DatabaseInitializer.dropDatabaseIfExists(dbUrl, user, pwd, dbName);
                DatabaseInitializer.createDatabaseIfNotExists(dbUrl, user, pwd, dbName);
            }
            try {
                controller.populateIfNeeded();
            } catch (Exception e1) {
                logger.log(Level.SEVERE, "Exception when starting server ! , {0}", e1);
            }

            SpringApplication.run(EducationManagementApplication.class, args);
            testScheduler();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when starting server !, {0}", e);
        }
    }

    public static void testScheduler(){
        DynamicSchedulerUtil schedulerUtil = new DynamicSchedulerUtil();
        HashMap<String, SchedulerConfig> map = new HashMap<>();
        map.put("Infinite", new SchedulerConfig("0/10 * * * * *", -1L, "AsyncSampleTask"));
        map.put("finite", new SchedulerConfig("0/10 * * * * *", 3L, "AsyncSampleFiniteTask"));
        schedulerUtil.addOrUpdateSchedulers(map);
    }

}