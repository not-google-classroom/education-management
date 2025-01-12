package com.org.education_management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.Column;
import com.org.education_management.model.ForeignKey;
import com.org.education_management.model.IndexKey;
import com.org.education_management.model.PrimaryKey;
import com.org.education_management.model.Table;
import com.org.education_management.model.TableMetaData;
import com.org.education_management.model.UniqueKey;
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

    public void populateStaticTableDataFiles(String jsonConfFilesPath) throws Exception {
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

    private void populateTablesToDB(String fullFilePath) throws Exception {
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
            logger.log(Level.SEVERE, "Exception when populating file : {0} tables to DB : {1}", new Object[]{fullFilePath, e});
            throw new Exception("Exception ccurred! unable to proceed for startup!");
        }
    }

    private void updateTableDetailsDataToDB(TableMetaData tableMetaData) throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        for(Table table : tableMetaData.getTables()) {
            String tableName = table.getTableName();
            String tableDesc = table.getDescription();
            PrimaryKey primaryKey = table.getPrimaryKey();
            UniqueKey uniqueKey = table.getUniqueKey();
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
                            DSL.field("FKEY_GEN_NAME"),
                            DSL.field("IS_NULLABLE"),
                            DSL.field("DEFAULT_VALUE"));
            InsertValuesStepN<?> insertFKStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("FKDetails"),
                        DSL.field("FK_GEN_NAME"), DSL.field("FKEY_TABLE_ID"), DSL.field("FKEY_COLUMN_ID"));


            for(Column column : table.getColumns()) {
                String columnName = column.getName();
                String columnType = column.getType();
                String defValue = column.getDefaultValue();
                boolean isPrimary = primaryKey != null && primaryKey.getPkColumns().contains(columnName);
                boolean isUnique = uniqueKey != null && uniqueKey.getUkColumns().contains(columnName);
                boolean notNull = column.getNotNull() != null && column.getNotNull();
                ForeignKey foreignKey = column.getForeignKey();
                String fkey = "--";
                if(foreignKey != null) {
                    fkey = foreignKey.getFkName();
                    Long fkeyTableID = TableUtil.getInstance().findTableId(foreignKey.getReferencedTable());
                    Long fkeyColID = TableUtil.getInstance().findColumnId(foreignKey.getReferencedColumn(), fkeyTableID);
                    insertFKStep.values(foreignKey.getFkName(), fkeyTableID, fkeyColID);
                }
                insertStep.values(columnName, columnType, tableID, isPrimary, isUnique, fkey, notNull, defValue);
            }
            insertStep.execute();
            insertFKStep.execute();
            updateConstraintDetails(table, tableID);
            storeTableDataToCache(table);
        }
    }

    private void updateConstraintDetails(Table table, Long tableID) throws Exception {
        if(table != null) {
            boolean isPKCol = table.getPrimaryKeys() != null;
            boolean isUKCol = table.getUniqueKeys() != null ;
            boolean isIKCol = table.getIndexKeys() != null;
            if(isPKCol) {
                updatePKColDetails(tableID, table);
            }
            if(isUKCol) {
                updateUKColDetails(tableID, table);
            }
            if(isIKCol) {
                updateIKColDetails(tableID, table);
            }
        }

    }

    private void updateIKColDetails(Long tableID, Table table) throws Exception {
        try {
            IndexKey indexKey = table.getIndexKey();
            if (indexKey != null) {
                DSLContext dslContext = DataBaseUtil.getDSLContext();
                String genName = indexKey.getIkName();
                List<String> ikCols = indexKey.getIkColumns();
                InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                        .insertInto(DSL.table("IKDetails"),
                                DSL.field("IK_COL_ID"),
                                DSL.field("IK_GEN_NAME"));
                for (String ikColName : ikCols) {
                    Long colID = TableUtil.getInstance().findColumnId(ikColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                        logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {{0}", colID);
                        throw new NullPointerException("ColumnDetails Empty! unable to add indexkey details");
                    }
                }
                insertStep.execute();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding IK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding IK Column Details : {0}", e);
        }
    }

    private void updateUKColDetails(Long tableID, Table table) throws Exception {
        try {
            UniqueKey uniqueKey = table.getUniqueKey();
            if (uniqueKey != null) {
                DSLContext dslContext = DataBaseUtil.getDSLContext();
                String genName = uniqueKey.getUkName();
                List<String> ukCols = uniqueKey.getUkColumns();
                InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                        .insertInto(DSL.table("UKDetails"),
                                DSL.field("UK_COL_ID"),
                                DSL.field("UK_GEN_NAME"));
                for (String ukColName : ukCols) {
                    Long colID = TableUtil.getInstance().findColumnId(ukColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                        logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {{0}", colID);
                        throw new NullPointerException("ColumnDetails Empty! unable to add uniqueKey details");
                    }
                }
                insertStep.execute();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding UK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding UK Column Details : {0}", e);
        }
    }

    private void updatePKColDetails(Long tableID, Table table) throws Exception {
        try {
            PrimaryKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null) {
                DSLContext dslContext = DataBaseUtil.getDSLContext();
                String genName = primaryKey.getPkName();
                List<String> pkCols = primaryKey.getPkColumns();
                InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                        .insertInto(DSL.table("PKDetails"),
                                DSL.field("PK_COL_ID"),
                                DSL.field("PK_GEN_NAME"));
                for (String pkColName : pkCols) {
                    Long colID = TableUtil.getInstance().findColumnId(pkColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                    logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {0}", colID);
                    throw new NullPointerException("ColumnDetails Empty! unable to add primaryKey details");
                }
                }
                insertStep.execute();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding PK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding PK Column Details : {0}", e);
        }
    }

    private void storeTableDataToCache(Table table) {
        String cacheKey = "TableCache_" + table.getTableName();
        CacheService.getInstance().putInCache("TableCache", cacheKey, table);
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

            processMetaData(tableName.toLowerCase(), tableData);
        }
    }

    private void processMetaData(String tableName, JsonNode tableData) {

        try {
            Table table = getTableData(tableName);
            List<Column> columns = table.getColumns();
            PrimaryKey primaryKey = table.getPrimaryKey();
            UniqueKey uniqueKey = table.getUniqueKey();
            HashMap<String,Column> primaryKeyColMap = new LinkedHashMap<>();
            HashMap<String, Column> foreignKeyColMap = new LinkedHashMap<>();
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            for (Column column : columns) {
                if(primaryKey != null && primaryKey.getPkColumns().contains(column.getName())) {
                    primaryKeyColMap.put(column.getName(), column);
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
                    if (!foreignKeyColMap.isEmpty() && foreignKeyColMap.containsKey(colName)) {
                        if (insertColValue == null || insertColValue.toString().isEmpty()) {
                            throw new IllegalArgumentException("Mandatory mapping details missing!");
                        }
                        insertColValue = getDataForColType(primaryKeyColMap.get(colName), tableName, insertColValue);
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
            String cacheKey = "TableCache_" + tableName;
            Table table = (Table) CacheService.getInstance().getFromCache("TableCache", cacheKey);
            if(table == null) {
                table = TableUtil.getInstance().getTableDetailsByName(tableName);
                if(table != null) {
                    CacheService.getInstance().putInCache("TableCache", cacheKey, table);
                } else {
                    logger.log(Level.INFO, "unable to fetch table data for meta population! tableName : {0}", tableName);
                }
            }
            return table;
        }
        return null;
    }
}
