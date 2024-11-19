package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.Column;
import com.org.education_management.model.ForeignKey;
import com.org.education_management.model.Table;
import com.org.education_management.model.TableMetaData;
import com.org.education_management.util.FileHandler;
import com.org.education_management.util.SQLGenerator;
import com.org.education_management.util.TableUtil;
import com.org.education_management.util.UniqueValueGenerator;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertValuesStepN;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class StartUpService {

    private static Logger logger = Logger.getLogger(StartUpService.class.getName());
    public static StartUpService startUpService = null;

    public static StartUpService getInstance() {
        if(startUpService == null) {
            startUpService = new StartUpService();
        }
        return startUpService;
    }

    public void populateStaticTableDataFiles(String jsonConfFilesPath) throws IOException {
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
            DataBaseUtil.batchUpdateQueries(bulkQueryString.toString());
            updateTableDetailsDataToDB(tableMetaData);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when populating tables to DB", e);
        }
    }

    private void updateTableDetailsDataToDB(TableMetaData tableMetaData) {
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
                    fkeyTableID = TableUtil.getInstance().findTableId(foreignKey.getReferencedTable());
                    fkeyColID = TableUtil.getInstance().findColumnId(foreignKey.getReferencedColumn(), fkeyTableID);
                }
                insertStep.values(columnName, columnType, tableID, isPrimary, isUnique, fkey, notNull, fkeyTableID, fkeyColID);
            }
            insertStep.execute();
            storeTableDataToCache(table);
        }
    }

    private void storeTableDataToCache(Table table) {
        String cacheKey = "TableCache_" + table.getTableName();
        CacheService.getInstance().putInCache("SimpleCache", cacheKey, table.getColumns());
        CacheService.getInstance().getFromCache("SimpleCache", cacheKey);
        logger.log(Level.INFO, "Table data added to cache");
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

            populateMetaToDB(fullFilePath);

        }
    }

    private void populateMetaToDB(String fullFilePath) throws IOException {
        JsonNode metaDataNode = FileHandler.readJsonFile(fullFilePath);
        Iterator<Map.Entry<String, JsonNode>> tablesIterator = metaDataNode.fields();
        while (tablesIterator.hasNext()) {
            Map.Entry<String, JsonNode> tableEntry = tablesIterator.next();
            String tableName = tableEntry.getKey();
            JsonNode tableData = tableEntry.getValue();

            processMetaData(tableName, tableData);
        }
    }

    private void processMetaData(String tableName, JsonNode tableData) {

        try {
            Table table = getTableData(tableName);
            List<Column> columns = table.getColumns();
            Column primaryKeyCol = null;
            HashMap<String, Column> foreignKeyColMap = new LinkedHashMap<>();
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            for (Column column : columns) {
                if(column.getPrimaryKey()) {
                    primaryKeyCol = column;
                    break;
                }
                if(column.getForeignKey() != null) {
                    foreignKeyColMap.put(column.getName(), column);
                }
            }
            StringBuilder sqlList = new StringBuilder();
            // Iterate over each record in the table
            for (JsonNode record : tableData) {
                // Start constructing the insert query
                InsertSetMoreStep<?> insertStep = (InsertSetMoreStep<?>) dslContext.insertInto(table(tableName));

                // Add all fields dynamically
                Iterator<Map.Entry<String, JsonNode>> fields = record.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    Object insertColValue = field.getValue().asText();
                    String colName = field.getKey().toLowerCase();

                    // Special handling for primary key
                    if (primaryKeyCol != null && primaryKeyCol.getName().equalsIgnoreCase(colName)) {
                        if (insertColValue == null || insertColValue.toString().isEmpty()) {
                            throw new IllegalArgumentException("Mandatory mapping details missing!");
                        }
                        insertColValue = getDataForColType(primaryKeyCol, tableName, insertColValue);
                    }

                    if(!foreignKeyColMap.isEmpty() && foreignKeyColMap.containsKey(colName)) {
                        if (insertColValue == null || insertColValue.toString().isEmpty()) {
                            throw new IllegalArgumentException("Mandatory mapping details missing!");
                        }
                        insertColValue = getDataForColType(foreignKeyColMap.get(colName), tableName, insertColValue);
                    }

                    insertStep = insertStep.set(DSL.field(DSL.name(field.getKey())), insertColValue);
                }
                sqlList.append(insertStep.getSQL(ParamType.INLINED)).append(";");
            }
            DataBaseUtil.batchUpdateQueries(sqlList.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when populating meta files : {0}", e);
        }
    }

    private Object getDataForColType(Column primaryKeyCol, String tableName, Object value) {
        if(value != null) {
            if (primaryKeyCol.getType().equalsIgnoreCase("bigint")) {
                String columnValue = value.toString();
                if(columnValue.contains("uvg")) {
                    Long uniqueGenValue = UniqueValueGenerator.getInstance().getUVGFor(columnValue);
                    if(uniqueGenValue == null) {
                        uniqueGenValue = UniqueValueGenerator.getInstance().getNextId(tableName, primaryKeyCol.getName());
                        UniqueValueGenerator.getInstance().updateUniqueValueToMap(columnValue, uniqueGenValue);
                    }
                    return uniqueGenValue;
                }
            } else {
                return value;
            }
        } else {
            logger.log(Level.SEVERE, "insert column value is null!, unable to process insert");
        }
        return null;
    }

    private Table getTableData(String tableName) {
        if(tableName != null && !tableName.isEmpty()) {
            Table table = (Table) CacheService.getInstance().getFromCache("TableCache", tableName);
            if(table == null) {
                table = TableUtil.getInstance().getTableDetailsByName(tableName);
                if(table != null) {
                    CacheService.getInstance().putInCache("TableCache", tableName, table);
                } else {
                    logger.log(Level.INFO, "unable to fetch table data for meta population! tableName : {0}", tableName);
                }
            }
            return table;
        }
        return null;
    }
}
