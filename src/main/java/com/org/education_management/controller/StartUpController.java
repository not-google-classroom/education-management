package com.org.education_management.controller;

import com.org.education_management.service.StartUpService;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.FileHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartUpController {

    private static final Logger logger = Logger.getLogger(String.valueOf(StartUpController.class));
    public StartUpService startUpService = new StartUpService();

    public void populateIfNeeded() throws Exception {
        boolean isFreshStart = isFreshStart();
        if(isFreshStart) {
            populateStaticMetaData();
            AppProperty.getInstance().setProperty("server", "startup_type", "warm");
        } else {
            logger.log(Level.INFO, "Server startup_type is warm, so skipping static data population");
        }
    }

    private void populateStaticMetaData() throws Exception {
        String jsonConfFilesPath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "static-meta.json";
        if(FileHandler.fileExists(jsonConfFilesPath)) {
            startUpService.populateStaticMetaDataFiles(jsonConfFilesPath);
        } else {
            logger.log(Level.WARNING, "static-meta.json file doesn't exist ! , unable to populate static data");
            throw new FileNotFoundException("static-meta.json file doesn't exist");
        }

    }

    public boolean isFreshStart() throws IOException {
        boolean isFreshStart = false;
        String startUpType = AppProperty.getInstance().getProperty("server", "startup_type");
        if(startUpType != null && startUpType.equalsIgnoreCase("cold")) {
            isFreshStart = true;
        }
        return isFreshStart;
    }
}
