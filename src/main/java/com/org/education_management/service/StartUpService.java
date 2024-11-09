package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.Column;
import com.org.education_management.model.ForeignKey;
import com.org.education_management.model.Table;
import com.org.education_management.model.TableMetaData;
import com.org.education_management.util.FileHandler;
import com.org.education_management.util.SQLGenerator;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

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
            StringBuilder bulkQueryString = new StringBuilder();
            for(Table table : tableMetaData.getTables()) {
                SQLGenerator sqlGenerator = new SQLGenerator();
                String sql = sqlGenerator.generateCreateTableSQL(table);
                bulkQueryString.append(sql).append(";");
                logger.log(Level.INFO, "SQL Constructed for Table : {0}, SQL : {1}", new Object[]{table.getTableName(), sql});
            }
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            dslContext.execute(bulkQueryString.toString());
            updateTableMetaDataToDB(tableMetaData);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when populating tables to DB", e);
        }
    }

    private void updateTableMetaDataToDB(TableMetaData tableMetaData) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        for(Table table : tableMetaData.getTables()) {
            String tableName = table.getTableName();
            String tableDesc = table.getDescritpion();
            org.jooq.Record record = dslContext.insertInto(table("TableDetails"),
                            field("TABLE_NAME"), field("DESCRIPTION"))
                    .values(tableName, tableDesc)
                    .returningResult(field("TABLE_ID")) // Specify the auto-increment column
                    .fetchOne();

            Long tableID =  record != null ? record.get(field("TABLE_ID", Long.class)) : null;
            InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("ColumnDetails"),
                            DSL.field("COLUMN_NAME"),
                            DSL.field("COLUMN_TYPE"),
                            DSL.field("TABLE_ID"),
                            DSL.field("IS_PRIMARY_KEY"),
                            DSL.field("IS_UNIQUE"),
                            DSL.field("IS_FOREIGN_KEY"),
                            DSL.field("IS_NULLABLE"),
                            DSL.field("FKEY_TABLE_ID"),
                            DSL.field("FKEY_COLUMN_ID"));

            for(Column column : table.getColumns()) {
                String columnName = column.getName();
                String columnType = column.getType();
                String defValue = column.getDefaultValue();
                boolean isPrimary = column.getPrimaryKey() != null && column.getPrimaryKey();
                boolean isUnique = column.getUnique() != null && column.getUnique();
                boolean notNull = column.getNotNull() != null && column.getNotNull();
                boolean fkey = false;
                Long fkeyTableID = null;
                Long fkeyColID = null;
                ForeignKey foreignKey = column.getForeignKey();
                if(foreignKey != null) {
                    fkey = true;
                    fkeyTableID = findTableId(foreignKey.getReferencedTable());
                    fkeyColID = findColumnId(foreignKey.getReferencedColumn(), fkeyTableID);
                }
                insertStep.values(columnName, columnType, tableID, isPrimary, isUnique, fkey, notNull, fkeyTableID, fkeyColID);
            }
            insertStep.execute();
        }
    }

    private Long findTableId(String tableName) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("TABLE_ID", Long.class))
                .from(DSL.table("TableDetails"))
                .where(DSL.field("TABLE_NAME").eq(tableName))
                .fetchOne();
        return record != null ? (Long) record.get(field("TABLE_ID")) : null;
    }

    private Long findColumnId(String columnName, Long tableId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("COLUMN_ID", Long.class))
                .from(DSL.table("ColumnDetails"))
                .where(DSL.field("COLUMN_NAME").eq(columnName))
                .and(DSL.field("TABLE_ID").eq(tableId))
                .fetchOne();
        return record != null ? (Long) record.get(field("COLUMN_ID")) : null;
    }

}
