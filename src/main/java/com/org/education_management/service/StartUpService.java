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
import com.org.education_management.util.*;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertValuesStepN;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.*;

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
            throw new Exception("Exception occurred! unable to proceed for startup!");
        }
    }

    private void updateTableDetailsDataToDB(TableMetaData tableMetaData) throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        for(Table table : tableMetaData.getTables()) {
            String tableName = table.getTableName();
            String tableDesc = table.getDescription();
            List<PrimaryKey> primaryKeyList = table.getPrimaryKeys();
            List<UniqueKey> uniqueKeyList = table.getUniqueKeys();
            List<ForeignKey> foreignKeyList = table.getForeignKeys();
            List<IndexKey> indexKeyList = table.getIndexKeys();
            HashMap<String, PrimaryKey> primaryKeyHashMap = new HashMap<>();
            if(primaryKeyList != null) {
                for (PrimaryKey primaryKey : primaryKeyList) {
                    for (String colName : primaryKey.getPkColumns()) {
                        primaryKeyHashMap.put(colName, primaryKey);
                    }
                }
            }
            HashMap<String, ForeignKey> foreignKeyHashMap = new HashMap<>();
            if(foreignKeyList != null) {
                for (ForeignKey foreignKey : foreignKeyList) {
                    for (String colName : foreignKey.getFkColumns()) {
                        foreignKeyHashMap.put(colName, foreignKey);
                    }
                }
            }
            HashMap<String, UniqueKey> uniqueKeyHashMap = new HashMap<>();
            if(uniqueKeyList != null) {
                for (UniqueKey uniqueKey : uniqueKeyList) {
                    for (String colName : uniqueKey.getUkColumns()) {
                        uniqueKeyHashMap.put(colName, uniqueKey);
                    }
                }
            }
            HashMap<String, IndexKey> indexKeyHashMap = new HashMap<>();
            if(indexKeyList != null) {
                for (IndexKey indexKey : indexKeyList) {
                    for (String colName : indexKey.getIkColumns()) {
                        indexKeyHashMap.put(colName, indexKey);
                    }
                }
            }
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
                            DSL.field("IS_INDEX_KEY"),
                            DSL.field("IS_NULLABLE"),
                            DSL.field("DEFAULT_VALUE"));

            for(Column column : table.getColumns()) {
                String columnName = column.getName();
                String columnType = column.getType();
                Object defValue = column.getDefaultValue();
                boolean isPrimary = !primaryKeyHashMap.isEmpty() && primaryKeyHashMap.containsKey(columnName);
                boolean isUnique = !uniqueKeyHashMap.isEmpty() && uniqueKeyHashMap.containsKey(columnName);
                boolean notNull = column.getNotNull() != null && column.getNotNull();
                boolean isForeignKey = !foreignKeyHashMap.isEmpty() && foreignKeyHashMap.containsKey(columnName);
                boolean isIndexKey = !indexKeyHashMap.isEmpty() && indexKeyHashMap.containsKey(columnName);
                insertStep.values(columnName, columnType, tableID, isPrimary, isUnique, isForeignKey, isIndexKey, notNull, defValue);
            }
            insertStep.execute();
            updateConstraintDetails(primaryKeyHashMap, foreignKeyHashMap, uniqueKeyHashMap, indexKeyHashMap, tableID, table);
        }
    }

    private void updateConstraintDetails(HashMap<String, PrimaryKey> primaryKeyHashMap, HashMap<String, ForeignKey> foreignKeyHashMap, HashMap<String, UniqueKey> uniqueKeyHashMap, HashMap<String, IndexKey> indexKeyHashMap, Long tableID, Table table) throws Exception {
        if(table != null) {
            boolean isPKCol = !primaryKeyHashMap.isEmpty();
            boolean isUKCol = !uniqueKeyHashMap.isEmpty();
            boolean isIKCol = !indexKeyHashMap.isEmpty();
            boolean isFkCol = !foreignKeyHashMap.isEmpty();
            if(isPKCol) {
                updatePKColDetails(tableID, table, primaryKeyHashMap);
            }
            if(isUKCol) {
                updateUKColDetails(tableID, table, uniqueKeyHashMap);
            }
            if(isIKCol) {
                updateIKColDetails(tableID, table, indexKeyHashMap);
            }
            if(isFkCol) {
                updateFKColDetails(tableID, table, foreignKeyHashMap);
            }
        }
    }

    private void updateFKColDetails(Long tableID, Table table, HashMap<String, ForeignKey> foreignKeyHashMap) throws Exception {
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("FKDetails"),
                            DSL.field("FK_COL_ID"),
                            DSL.field("FK_REF_COL_ID"),
                            DSL.field("FK_REF_TABLE_ID"),
                            DSL.field("FK_TYPE"),
                            DSL.field("FK_GEN_NAME"));
            for(Map.Entry<String, ForeignKey> fKeySet : foreignKeyHashMap.entrySet()) {
                ForeignKey foreignKey = fKeySet.getValue();
                String genName = foreignKey.getFkName();
                List<String> fkCols = foreignKey.getFkColumns();
                List<String> referencedCols = foreignKey.getReferencedColumns();
                String refTabName = foreignKey.getReferencedTable();
                Long refTabID = TableUtil.getInstance().findTableId(refTabName);
                String fkType = foreignKey.getOnDelete();

                for (int index = 0; index < referencedCols.size(); index++) {
                    Long colID = TableUtil.getInstance().findColumnId(fkCols.get(index), tableID);
                    Long refColID = TableUtil.getInstance().findColumnId(referencedCols.get(index), refTabID);
                    if (colID != null && refColID != null) {
                        insertStep.values(colID, refColID, refTabID, fkType, genName);
                    } else {
                        logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for colName : {0}, refCol Name : {1}", new Object[]{fkCols.get(index), referencedCols.get(index)});
                        throw new NullPointerException("ColumnDetails Empty! unable to add foreignKeys details");
                    }
                }
            }
            insertStep.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding FK Details to tableName : {0}, Exception : {1}", new Object[]{table.getTableName(), e});
            throw new Exception("Exception when adding FK Column Details : {0}", e);
        }
    }

    private void updateIKColDetails(Long tableID, Table table, HashMap<String, IndexKey> indexKeyHashMap) throws Exception {
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("IKDetails"),
                            DSL.field("IK_COL_ID"),
                            DSL.field("IK_GEN_NAME"));
            for(Map.Entry<String, IndexKey> ikSet : indexKeyHashMap.entrySet()) {
                IndexKey indexKey = ikSet.getValue();
                String genName = indexKey.getIkName();
                List<String> ikCols = indexKey.getIkColumns();

                for (String ikColName : ikCols) {
                    Long colID = TableUtil.getInstance().findColumnId(ikColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                        logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {{0}", colID);
                        throw new NullPointerException("ColumnDetails Empty! unable to add indexkey details");
                    }
                }
            }
            insertStep.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding IK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding IK Column Details : {0}", e);
        }
    }

    private void updateUKColDetails(Long tableID, Table table, HashMap<String, UniqueKey> uniqueKeyHashMap) throws Exception {
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("UKDetails"),
                            DSL.field("UK_COL_ID"),
                            DSL.field("UK_GEN_NAME"));
            for(Map.Entry<String, UniqueKey> ukSet : uniqueKeyHashMap.entrySet()) {
                UniqueKey uniqueKey = ukSet.getValue();
                String genName = uniqueKey.getUkName();
                List<String> ukCols = uniqueKey.getUkColumns();
                for (String ukColName : ukCols) {
                    Long colID = TableUtil.getInstance().findColumnId(ukColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                        logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {0}", colID);
                        throw new NullPointerException("ColumnDetails Empty! unable to add uniqueKey details");
                    }
                }
            }
            insertStep.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding UK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding UK Column Details : {0}", e);
        }
    }

    private void updatePKColDetails(Long tableID, Table table, HashMap<String, PrimaryKey> primaryKeyHashMap) throws Exception {
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext
                    .insertInto(DSL.table("PKDetails"),
                            DSL.field("PK_COL_ID"),
                            DSL.field("PK_GEN_NAME"));
            for(Map.Entry<String, PrimaryKey> pkSet : primaryKeyHashMap.entrySet()) {
                PrimaryKey primaryKey = pkSet.getValue();
                String genName = primaryKey.getPkName();
                List<String> pkCols = primaryKey.getPkColumns();
                for (String pkColName : pkCols) {
                    Long colID = TableUtil.getInstance().findColumnId(pkColName, tableID);
                    if (colID != null) {
                        insertStep.values(colID, genName);
                    } else {
                    logger.log(Level.SEVERE, "ColumnDetails not found!, throw error for ID : {0}", colID);
                    throw new NullPointerException("ColumnDetails Empty! unable to add primaryKey details");
                    }
                }
            }
            insertStep.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding PK Details to tableID : {0}", tableID);
            throw new Exception("Exception when adding PK Column Details : {0}", e);
        }
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
            HashMap<String, Column> columnDetails = new HashMap<>();
            for (Column column : columns) {
                columnDetails.put(column.getName(), column);
            }
            DSLContext dslContext = DataBaseUtil.getDSLContext();
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

                    if (columnDetails.containsKey(colName)) {
                        // Special handling for primary key
                        Column column = columnDetails.get(colName);
                        boolean isPrimaryKey = column.getPrimary();
                        boolean isForeignKey = column.getForeign();

                        if (isPrimaryKey) {
                            if (insertColValue == null || insertColValue.toString().isEmpty()) {
                                throw new IllegalArgumentException("Mandatory mapping details missing!");
                            }
                            insertColValue = getDataForColType(column, tableName, insertColValue);
                        }

                        if (isForeignKey) {
                            if (insertColValue == null || insertColValue.toString().isEmpty()) {
                                throw new IllegalArgumentException("Mandatory mapping details missing!");
                            }
                            insertColValue = getDataForColType(column, tableName, insertColValue);
                        }

                        insertStep = insertStep.set(DSL.field(DSL.name(field.getKey().toLowerCase())), insertColValue);
                    }
                    else {
                        throw new NoSuchFieldException("The columnName : " + colName + ", doesn't exists in the table : " + tableName);
                    }
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
            if (primaryKeyCol.getType().equalsIgnoreCase("bigint") || primaryKeyCol.getType().equalsIgnoreCase("long") ||
            primaryKeyCol.getType().equalsIgnoreCase("int") || primaryKeyCol.getType().equalsIgnoreCase("integer")) {
                String columnValue = value.toString();
                if(columnValue.contains("uvg")) {
                    Long uniqueGenValue = UniqueValueGenerator.getInstance().getUVGFor(columnValue);
                    if(uniqueGenValue == null) {
                        uniqueGenValue = UniqueValueGenerator.getInstance().getNextId(tableName, primaryKeyCol.getName());
                        UniqueValueGenerator.getInstance().updateUniqueValueToMap(columnValue, uniqueGenValue);
                    }
                    return uniqueGenValue;
                } else {
                    return value;
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

    public void startSchedulers() {
        SchemaUtil.getInstance().startUserSpecificSchedulers();
    }
}
