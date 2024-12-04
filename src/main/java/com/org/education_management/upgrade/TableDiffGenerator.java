package com.org.education_management.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.org.education_management.upgrade.bean.Column;
import com.org.education_management.upgrade.bean.Table;
import com.org.education_management.upgrade.impl.PostgresQueryConstructor;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TableDiffGenerator {
    LinkedHashMap<String,Table> tableHashMap = new LinkedHashMap<>();
    LinkedHashMap<String,Table> oldtableHashMap = new LinkedHashMap<>();
    private static final Logger logger = Logger.getLogger(TableDiffGenerator.class.getName());
    PostgresQueryConstructor postgresQueryConstructor = new PostgresQueryConstructor();
    public static void main(String args[]) throws Exception{
        if(args.length > 2){
            Boolean toGenerateIsu = Boolean.parseBoolean(args[0]);
            if(toGenerateIsu){
                new TableDiffGenerator().GenerateTableDiff(args[1],args[2]);
            }
            else{
                logger.log(Level.SEVERE,"Skipping Isu Generation");
            }
        }
        else{
            throw new Exception("Invalid argument list...");
        }
    }
    public void GenerateTableDiff(String newLocation, String oldLocation) throws Exception{
        loadStaticTablePublic(newLocation,tableHashMap);
        loadStaticTablePublic(oldLocation,oldtableHashMap);
        loadStaticTableSpecific(newLocation,tableHashMap);
        loadStaticTableSpecific(oldLocation,oldtableHashMap);
        System.out.println("Successfully loaded!!!");

        generateDiff();
    }
    public void loadNewPublicTables(String fileName,HashMap<String,Table> tableDetailsHashMap,String baseLocation) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new File(fileName));
        Iterator<String> iterator = node.fieldNames();
        while (iterator.hasNext()){
            String key = iterator.next();
            JsonNode fileDetails = node.get(key);
            String file = fileDetails.get("file_path").asText();
            String fileLocation = baseLocation + File.separator + "src" + File.separator + "main" +File.separator +file;
            JsonNode innerJson = mapper.readTree(new File(fileLocation));
            JsonNode tableDetails = innerJson.get("tables");
            ArrayNode arrayNode = (ArrayNode) tableDetails;
            for (int index=0;index<arrayNode.size();index++){
                JsonNode tableDetailsJson = arrayNode.get(index);
                String tableName = tableDetailsJson.get("tableName").asText();
                String tableDesciption = tableDetailsJson.has("tableDetailsJson")?tableDetailsJson.get("description").asText():null;
                Boolean hasPrimaryKey = tableDetailsJson.has("primaryKeys");
                Boolean hasIndexKey = tableDetailsJson.has("indexKeys");
                Table table = new Table(tableName,tableDesciption,key,hasPrimaryKey);
                if(tableDetailsHashMap.containsKey(tableName) && !(tableDetailsHashMap.get(tableName).getModuleName().equalsIgnoreCase(key))){
                    throw new Exception("Already table exist");
                }
                tableDetailsHashMap.put(tableName,table);

                List<String> primaryKeyList = new ArrayList<>();
                LinkedHashMap<String,List<String>> tablePrimaryKeyList = new LinkedHashMap<>();
                if(hasPrimaryKey){
                    JsonNode primaryKeys = tableDetailsJson.get("primaryKeys");
                    ArrayNode primaryKeyArrNode = (ArrayNode) primaryKeys;
                    for (int primaryKeyIndex=0;primaryKeyIndex<primaryKeyArrNode.size();primaryKeyIndex++){
                        JsonNode primaryKey = primaryKeyArrNode.get(primaryKeyIndex);
                        String primaryKeyName = primaryKey.get("pkName").asText();
                        List<String> columnToPkRef = new ArrayList<>();
                        ArrayNode primaryKeyNode = (ArrayNode) primaryKey.get("pkColumns");
                        for (int listIndex=0;listIndex<primaryKeyNode.size();listIndex++){
                            JsonNode primaryKeyDetailNode = primaryKeyNode.get(listIndex);
                            String pkColumnName = primaryKeyDetailNode.asText();
                            primaryKeyList.add(pkColumnName);
                            columnToPkRef.add(pkColumnName);
                        }
                        tablePrimaryKeyList.put(primaryKeyName,columnToPkRef);
                    }
                }
                table.setTablesPkDetails(tablePrimaryKeyList,primaryKeyList);

                List<String> indexKeyList = new ArrayList<>();
                LinkedHashMap<String,List<String>> tableIndexList = new LinkedHashMap<>();
                if(hasIndexKey){
                    JsonNode indexKeys = tableDetailsJson.get("indexKeys");
                    ArrayNode indexKeyArrNode = (ArrayNode) indexKeys;
                    for (int indexKeyIndex=0;indexKeyIndex<indexKeyArrNode.size();indexKeyIndex++){
                        JsonNode indexKey = indexKeyArrNode.get(indexKeyIndex);
                        String indexKeyName = indexKey.get("indexName").asText();
                        List<String> columnToIndexRef = new ArrayList<>();
                        ArrayNode indexKeyNode = (ArrayNode) indexKey.get("indexColumns");
                        for (int listIndex=0;listIndex<indexKeyNode.size();listIndex++){
                            JsonNode primaryKeyDetailNode = indexKeyNode.get(listIndex);
                            String indexColumnName = primaryKeyDetailNode.asText();
                            indexKeyList.add(indexColumnName);
                            columnToIndexRef.add(indexColumnName);
                        }
                        tableIndexList.put(indexKeyName,columnToIndexRef);
                    }
                }
                table.setTablesIndexDetails(tableIndexList);
                table.setIndexColumnList(indexKeyList);

                JsonNode columnDetails = tableDetailsJson.get("columns");
                ArrayNode columnNodes = (ArrayNode) columnDetails;
                LinkedHashMap<String,Column> columnDetailsMap = new LinkedHashMap<>();
                for (int columnIndex=0;columnIndex<columnNodes.size();columnIndex++){
                    JsonNode column = columnDetails.get(columnIndex);
                    String columnName = column.get("name").asText();
                    String dataType = column.get("type").asText();
                    Boolean isAutoIncrement = column.has("autoIncrement")?column.get("autoIncrement").asBoolean():Boolean.FALSE;
                    Boolean isNotNull = column.has("notNull")?column.get("notNull").asBoolean():Boolean.FALSE;
                    Boolean isForeignKey = column.has("foreignKey")?Boolean.TRUE:Boolean.FALSE;
                    Boolean isUniqueColumn = column.has("unique")?column.get("unique").asBoolean():Boolean.FALSE;
                    Boolean hasDefaultValue = column.has("defaultValue")?Boolean.TRUE:Boolean.FALSE;
                    Boolean isPrimaryKey = primaryKeyList.contains(columnName);
                    Boolean isIndexColumn = indexKeyList.contains(columnName);
                    Object defaultValue = null;
                    Column columnBean = null;
                    if(hasDefaultValue){
                        defaultValue = column.get("defaultValue");
                        columnBean = new Column(columnName,dataType,isPrimaryKey,isForeignKey,isAutoIncrement,isNotNull,isUniqueColumn,hasDefaultValue,defaultValue,isIndexColumn);
                    }
                    else{
                        columnBean = new Column(columnName,dataType,isPrimaryKey,isForeignKey,isAutoIncrement,isNotNull,isUniqueColumn,isIndexColumn);
                    }
                    columnDetailsMap.put(columnName,columnBean);
                    if(isPrimaryKey){
                        String pkName = null;
                        Iterator<String> pkIterator = tablePrimaryKeyList.keySet().iterator();
                        while (pkIterator.hasNext()){
                            String pkKey = pkIterator.next();
                            List<String> pkList = tablePrimaryKeyList.get(pkKey);
                            if(pkList.contains(columnName)){
                                pkName = pkKey;
                                break;
                            }
                        }
                        columnBean.setPkName(pkName);
                    }
                    if(isIndexColumn){
                        String indexName = null;
                        Iterator<String> indexIterator = tableIndexList.keySet().iterator();
                        while (indexIterator.hasNext()){
                            String inName = indexIterator.next();
                            List<String> inNameList = tableIndexList.get(inName);
                            if(inNameList.contains(columnName)){
                                indexName = inName;
                                break;
                            }
                        }
                        columnBean.setIndexName(indexName);
                    }
                    if(isForeignKey){
                        JsonNode foreignKeyDetail = column.get("foreignKey");
                        columnBean.setForeignKey(foreignKeyDetail);
                        String referenceTableName = foreignKeyDetail.get("referencedTable").asText();
                        String referencedColumn = foreignKeyDetail.get("referencedColumn").asText();
                        if(foreignKeyDetail.has("onDelete")){
                            String onDelete = foreignKeyDetail.get("onDelete").asText();
                            Table tableObj = tableDetailsHashMap.get(referenceTableName);
                            HashMap<String,Column> referenceTableColumnDetails = tableObj.getColumnList();
                            Column referenceTableColumnName = referenceTableColumnDetails.get(referencedColumn);
                            referenceTableColumnName.setPrimaryKeyDetails(tableName,columnName,onDelete,table,columnBean);
                        }
                    }
                }
                table.setColumnDetails(columnDetailsMap);
            }
        }
    }
    public void loadStaticTablePublic(String location,HashMap<String,Table> tableDetailsHashMap) throws Exception{
        String fileLocation = location + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static-table-public.json";
        loadNewPublicTables(fileLocation,tableDetailsHashMap,location);
    }
    public void loadStaticTableSpecific(String location,HashMap<String,Table> tableDetailsHashMap) throws Exception{
        String fileLocation = location + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static-table-specific.json";
        loadNewPublicTables(fileLocation,tableDetailsHashMap,location);
    }
    public void generateDiff() throws Exception{
        Iterator<String> iterator = tableHashMap.keySet().iterator();
        while (iterator.hasNext()){
            String tableName = iterator.next();
            Table table = tableHashMap.get(tableName);
            if(!oldtableHashMap.containsKey(tableName)){
                postgresQueryConstructor.constructTableQuery(table);
            }
            else{
                Table oldTableObj = oldtableHashMap.get(tableName);
                LinkedHashMap<String, Column> newColumnMap = table.getColumnList();
                LinkedHashMap<String, Column> oldColumnMap = oldTableObj.getColumnList();
                Set<String> newTableColumn = newColumnMap.keySet();
                Set<String> oldTableColumn = oldColumnMap.keySet();
                Set<String> addedColumns = new LinkedHashSet<>(newTableColumn);
                addedColumns.removeAll(oldTableColumn);
                Set<String> removedColumns = new LinkedHashSet<>(oldTableColumn);
                removedColumns.removeAll(newTableColumn);
                if(!addedColumns.isEmpty()){
                    Iterator<String> addedColumn = addedColumns.iterator();
                    while (addedColumn.hasNext()){
                        Column addedColumnObject = newColumnMap.get(addedColumn.next());
                        postgresQueryConstructor.constructNewColumnQuery(table,addedColumnObject);
                    }
                }
                if(!removedColumns.isEmpty()){
                    Iterator<String> removedColumn = removedColumns.iterator();
                    while (removedColumn.hasNext()){
                        Column removedColumnsObject = oldColumnMap.get(removedColumn.next());
                        postgresQueryConstructor.constructDeleteColumnQuery(table,removedColumnsObject);
                    }
                }
                Set<String> existingColumns = new LinkedHashSet<>(newTableColumn);
                existingColumns.removeAll(oldTableColumn);
                Iterator<String> existingColumn = existingColumns.iterator();
                while (existingColumn.hasNext()){
                    String existingColumnName = existingColumn.next();
                    Column existingColumnInNewDiff = newColumnMap.get(existingColumnName);
                    Column existingColumnInOldDiff = oldColumnMap.get(existingColumnName);
                    checkIfColumnPropChanged(table,existingColumnInNewDiff,existingColumnInOldDiff);
                }
            }
        }
    }
    public void checkIfColumnPropChanged(Table table,Column newDiff,Column oldDiff){
        String newDataType = newDiff.getDataType();
        String oldDataType = oldDiff.getDataType();
        if(!newDataType.equalsIgnoreCase(oldDataType)){
            postgresQueryConstructor.alterExistingColumnsDataType(table,newDiff);
        }

        Boolean newIsPrimaryKey = newDiff.getIsPrimaryKey();
        Boolean oldIsPrimaryKey = oldDiff.getIsPrimaryKey();
        if(newIsPrimaryKey != oldIsPrimaryKey){
            if(newIsPrimaryKey && !oldIsPrimaryKey){
                postgresQueryConstructor.alterExistingColumnAddPrimaryKey(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropPrimaryKey(table,newDiff);
            }
        }

        Boolean newIsUniqueKey = newDiff.getUnique();
        Boolean oldIsUniqueKey = oldDiff.getUnique();
        if(newIsUniqueKey != oldIsUniqueKey){
            if(newIsUniqueKey && !oldIsUniqueKey){
                postgresQueryConstructor.alterExistingColumnAddUniqueConstraint(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropUniqueConstraint(table,newDiff);
            }
        }

        Boolean newIsAutoIncrement = newDiff.getAutoIncrementColumn();
        Boolean oldIsAutoIncrement = oldDiff.getAutoIncrementColumn();
        if(newIsAutoIncrement != oldIsAutoIncrement){
            if(newIsAutoIncrement && !oldIsAutoIncrement){
                postgresQueryConstructor.alterExistingColumnAddIsAutoIncrementContraint(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropIsAutoIncrementContraint(table,newDiff);
            }
        }

        Boolean newIsNotNull = newDiff.getNotNullColumn();
        Boolean oldIsNotNull = oldDiff.getNotNullColumn();
        if(newIsNotNull != oldIsNotNull){
            if(newIsNotNull && !oldIsNotNull){
                postgresQueryConstructor.alterExistingColumnAddNotNullContraint(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropNotNullContraint(table,newDiff);
            }
        }

        Boolean newIsForeignKey = newDiff.getForeignKey();
        Boolean oldIsForeignKey = oldDiff.getForeignKey();
        if(newIsForeignKey != oldIsForeignKey){
            if(newIsForeignKey && !oldIsForeignKey){
                postgresQueryConstructor.alterExistingColumnAddForeignKey(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropForeignKey(table,newDiff);
            }
        }

        Boolean newHasDefaultValue = newDiff.getHasDefaultValue();
        Boolean oldHasDefaultValue = oldDiff.getHasDefaultValue();
        if(newHasDefaultValue != oldHasDefaultValue){
            if(newHasDefaultValue && !oldHasDefaultValue){
                postgresQueryConstructor.alterExistingColumnAddForeignKey(table,newDiff);
            }
            else{
                postgresQueryConstructor.alterExistingColumnDropForeignKey(table,newDiff);
            }
        }
    }

}
