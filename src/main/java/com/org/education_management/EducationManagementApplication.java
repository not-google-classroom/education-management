package com.org.education_management;

import com.org.education_management.controller.StartUpController;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.DatabaseInitializer;
import com.org.education_management.util.QueueUtil;
import com.org.education_management.util.api.AppInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.logging.*;

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
            setUPLogger();
            logger.log(Level.INFO, "Startup initiated...connecting to database");
            StartUpController controller = new StartUpController();
            boolean isDeveloperMode = controller.isDeveloperMode();
            if (isDeveloperMode) {
                logger.log(Level.INFO, "Developer mode enabled");
                boolean isFreshStart = controller.isFreshStart();
                if (isFreshStart) {
                    DatabaseInitializer.dropDatabaseIfExists(dbUrl, user, pwd, dbName);
                    DatabaseInitializer.createDatabaseIfNotExists(dbUrl, user, pwd, dbName);
                }
            }
            try {
                controller.populateIfNeeded();
                AppInitializer.startRateLimitScheduler();
                QueueUtil.addQueues();
            } catch (Exception e1) {
                logger.log(Level.SEVERE, "Exception when starting server ! , {0}", e1);
            }

            SpringApplication.run(EducationManagementApplication.class, args);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when starting server !, {0}", e);
        }
    }

    private static void setUPLogger() {
        Logger rootLogger = Logger.getLogger("");

        // Remove default console handler
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        try {
            // File Handler with Rotation
            String filePath = com.org.education_management.util.FileHandler.getHomeDir() + com.org.education_management.util.FileHandler.getFileSeparator() + "logs";
            com.org.education_management.util.FileHandler.createDirectoryIfNotExists(filePath);
            filePath += com.org.education_management.util.FileHandler.getFileSeparator() + "application-%g.log";
            System.out.println("Logger File Location : " + filePath);
            Handler fileHandler = new FileHandler(filePath, 5 * 1024 * 1024, 10, true); // 10 MB file size, 5 backups
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);

            // Console Handler
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.INFO);

            // Add handlers to the root logger
            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);

            // Set logging level for the root logger
            rootLogger.setLevel(Level.INFO);

        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

}