package com.org.education_management.util.api;

import com.org.education_management.config.SchedulerConfig;
import com.org.education_management.util.DynamicSchedulerUtil;
import com.org.education_management.util.FileHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppInitializer {
    private static final Logger logger = Logger.getLogger(String.valueOf(AppInitializer.class));
    public static void initialize(){
        startRateLimitScheduler();
    }

    public static void startQueue() {
        startKafkaQueue();
    }

    public static void startKafkaQueue() {
        try {
            String batFile = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" +
                    FileHandler.getFileSeparator() + "Commands" + FileHandler.getFileSeparator() + "startKafkaZoo.bat";
            //Zookeeper port - 2181, kafka port - 9092
            if (!isPortOpen("localhost", 2181) && !isPortOpen("localhost", 9092)) {
                startProcess(batFile);
            } else {
                logger.log(Level.INFO,"Kafka queue not started as port 2181 or 9092 is occupied, kill the ports and restart the server");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while starting kafka queue", e);
        }
    }

    public static boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void startProcess(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    private static void startRateLimitScheduler(){
        HashMap<String, SchedulerConfig> map = new HashMap<>();
        map.put("ApiRateLimitScheduler", new SchedulerConfig("*/60 * * * * *",
                -1, "ApiRateLimitTask"));
        DynamicSchedulerUtil.getInstance().addOrUpdateSchedulers(map);
    }
}
