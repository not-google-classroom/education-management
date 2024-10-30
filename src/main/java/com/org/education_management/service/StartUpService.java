package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.FileHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StartUpService {
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
            String className = AppProperty.getInstance().getProperty("spring.datasource.driverClassName");
            String password = AppProperty.getInstance().getProperty("spring.datasource.password");

            // Establish connection to H2 database
            try (Connection connection = DriverManager.getConnection(url, userName, password)) {
                JsonNode schemaNode = FileHandler.readJsonFile(fullFilePath);
                List<String> createTableStatements = new ArrayList<>();
                List<String> addForeignKeyStatements = new ArrayList<>();

                // Iterate through the tables defined in the JSON
                for (JsonNode tableNode : schemaNode.get("tables")) {
                    String tableName = tableNode.get("tableName").asText();
                    StringBuilder createTableSql = new StringBuilder("CREATE TABLE " + tableName + " (");

                    // Iterate through the columns
                    for (JsonNode columnNode : tableNode.get("columns")) {
                        String columnName = columnNode.get("name").asText();
                        String columnType = columnNode.get("type").asText();
                        boolean isPrimaryKey = columnNode.path("primaryKey").asBoolean(false);
                        boolean isUniqueKey = columnNode.path("uniqueKey").asBoolean(false);
                        String defaultValue = columnNode.path("defaultValue").asText(null);

                        createTableSql.append(columnName).append(" ").append(columnType);

                        if (isPrimaryKey) {
                            createTableSql.append(" PRIMARY KEY");
                        }
                        if (isUniqueKey) {
                            createTableSql.append(" UNIQUE");
                        }
                        if (defaultValue != null && !defaultValue.equals("null")) {
                            createTableSql.append(" DEFAULT ").append(defaultValue);
                        }

                        createTableSql.append(", ");

                        // Check for foreign key constraints
                        JsonNode foreignKeyNode = columnNode.path("foreignKey");
                        if (!foreignKeyNode.isMissingNode()) {
                            String referencedTable = foreignKeyNode.get("referencedTable").asText();
                            String referencedColumn = foreignKeyNode.get("referencedColumn").asText();
                            String onDeleteAction = foreignKeyNode.path("onDelete").asText(null);
                            StringBuilder foreignKeySql = new StringBuilder();
                            foreignKeySql.append(String.format(
                                    "ALTER TABLE %s ADD CONSTRAINT fk_%s FOREIGN KEY (%s) REFERENCES %s(%s)",
                                    tableName, columnName, columnName, referencedTable, referencedColumn
                            ));

                            // Add ON DELETE action if specified
                            if (onDeleteAction != null && !onDeleteAction.isEmpty()) {
                                foreignKeySql.append(" ON DELETE ").append(onDeleteAction);
                            }
                            foreignKeySql.append(";");
                        }
                    }

                    // Remove last comma and space, and close the CREATE TABLE statement
                    createTableSql.setLength(createTableSql.length() - 2);
                    createTableSql.append(");");

                    // Store the CREATE TABLE statement
                    createTableStatements.add(createTableSql.toString());
                }

                // Execute the CREATE TABLE statements
                try (Statement statement = connection.createStatement()) {
                    for (String createTableSql : createTableStatements) {
                        statement.execute(createTableSql);
                        System.out.println("Executed: " + createTableSql);
                    }

                    // Execute the foreign key constraints
                    for (String foreignKeySql : addForeignKeyStatements) {
                        statement.execute(foreignKeySql);
                        System.out.println("Executed: " + foreignKeySql);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
