package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.model.Table;
import com.org.education_management.model.TableMetaData;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.FileHandler;
import com.org.education_management.util.SQLGenerator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartUpService {

    private static Logger logger = Logger.getLogger(StartUpService.class.getName());
    public static StartUpService startUpService = null;


    public StartUpService getInstance() {
        if(startUpService == null) {
            startUpService = new StartUpService();
        }
        return startUpService;
    }

    public void populateStaticMetaDataFiles(String jsonConfFilesPath) throws IOException {
        JsonNode metaDataNode = FileHandler.readJsonFile(jsonConfFilesPath);

        // Iterate through the fields in the JSON
        Iterator<String> fieldNames = metaDataNode.fieldNames();
        while (fieldNames.hasNext()) {
            String moduleName = fieldNames.next();
            JsonNode moduleData = metaDataNode.get(moduleName);

            String fileName = moduleData.get("file_name").asText();
            String filePath = moduleData.get("file_path").asText();

            String fullFilePath = System.getProperty("user.dir") + FileHandler.getFileSeparator() + filePath;

            populateTablesToDB(fullFilePath);

        }
    }

    private void populateTablesToDB(String fullFilePath) {
        try {
            String url = AppProperty.getInstance().getProperty("spring.datasource.url");
            String userName = AppProperty.getInstance().getProperty("spring.datasource.username");
            String password = AppProperty.getInstance().getProperty("spring.datasource.password");

            try (Connection connection = DriverManager.getConnection(url, userName, password)) {
                TableMetaData tableMetaData = FileHandler.readSchemaFromFile(fullFilePath);
                for(Table table : tableMetaData.getTables()) {
                    SQLGenerator sqlGenerator = new SQLGenerator();
                    String sql = sqlGenerator.generateCreateTableSQL(table);
                    Statement stmt = connection.createStatement();
                    if (stmt.execute(sql)) {
                        logger.log(Level.INFO, "Table : {0}, columns :{1} populated successfully", new Object[]{table.getTableName(), table.getColumns()});
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when populating tables to DB", e);
        }
    }
}
