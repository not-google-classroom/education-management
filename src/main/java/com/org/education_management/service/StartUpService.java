package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.Table;
import com.org.education_management.model.TableMetaData;
import com.org.education_management.util.FileHandler;
import com.org.education_management.util.SQLGenerator;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.Iterator;
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

            String fullFilePath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + filePath;

            populateTablesToDB(fullFilePath);

        }
    }

    private void populateTablesToDB(String fullFilePath) {
        try {
            TableMetaData tableMetaData = FileHandler.readSchemaFromFile(fullFilePath);
            for(Table table : tableMetaData.getTables()) {
                SQLGenerator sqlGenerator = new SQLGenerator();
                String sql = sqlGenerator.generateCreateTableSQL(table);
                DSLContext dslContext = DataBaseUtil.getDSLContext();
                dslContext.execute(sql);
                logger.log(Level.INFO, "Table : {0}, columns :{1} populated successfully", new Object[]{table.getTableName(), table.getColumns()});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when populating tables to DB", e);
        }
    }
}
