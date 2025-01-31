package com.org.education_management.controller;

import com.org.education_management.registry.RegistryManager;
import com.org.education_management.service.OrgService;
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
        if(isFreshStart()) {
            populateStaticTableData();
            createDeveloperAccount();
            //RegistryManager.setRegistryString("Initiated", "true");
        } else {
            logger.log(Level.INFO, "Server startup_type is warm, so skipping static data population");
            startUpService.startSchedulers();
        }
    }

    private void populateStaticTableData() throws Exception {
        String jsonConfFilesPath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "static-table-public.json";
        if(FileHandler.fileExists(jsonConfFilesPath)) {
            startUpService.populateStaticTableDataFiles(jsonConfFilesPath);
        } else {
            logger.log(Level.WARNING, "static-table-public.json file doesn't exist ! , unable to populate static data");
            throw new FileNotFoundException("static-table-public.json file doesn't exist");
        }
    }

    public boolean isFreshStart() throws IOException {
        return !RegistryManager.hasRegistryKey("Initiated");
    }

    public void createDeveloperAccount(){
        OrgService service = new OrgService();
        try {
            service.createOrg("ERP", "admin@erp.com","admin","admin");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDeveloperMode() throws IOException {
        return Boolean.parseBoolean(AppProperty.getInstance().getProperty("feature.developerMode"));
    }
}
