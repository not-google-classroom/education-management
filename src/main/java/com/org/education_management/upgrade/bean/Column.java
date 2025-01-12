package com.org.education_management.upgrade.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Column {
    private String columnName;
    private String dataType;
    private Boolean isPrimaryKey;
    private String pkName;
    private Long autoIncrementStartValue;
    private Boolean isForeignKey;
    private Boolean isAutoIncrementColumn;
    private Boolean isNotNullColumn;
    private Boolean isUnique;
    private JsonNode foreignKey;
    private Boolean hasDefaultValue;
    private Boolean isIndexedColumn;
    private String indexName;
    private Object defaultValue;
    private LinkedHashMap<String,LinkedHashMap<String,Object>> primaryKeyDetails;

    public Column(String columnName, String dataType, Boolean isPrimaryKey, Boolean isForignKey, Boolean isAutoIncrementColumn, Boolean isNotNullColumn, Boolean isUnique,Boolean isIndexedColumn){
        this.columnName = columnName;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.isForeignKey = isForignKey;
        this.isAutoIncrementColumn= isAutoIncrementColumn;
        this.isNotNullColumn = isNotNullColumn;
        this.isUnique = isUnique;
        ObjectMapper objectMapper = new ObjectMapper();
        this.foreignKey = objectMapper.createObjectNode();
        this.hasDefaultValue = Boolean.FALSE;
        this.defaultValue = null;
        this.primaryKeyDetails = new LinkedHashMap<>();
        this.isIndexedColumn = isIndexedColumn;
        this.autoIncrementStartValue =1L ;
    }

    public Column(String columnName, String dataType, Boolean isPrimaryKey, Boolean isForignKey, Boolean isAutoIncrementColumn, Boolean isNotNullColumn, Boolean isUnique, Boolean hasDefaultValue,Object defaultValue,Boolean isIndexedColumn){
        this.columnName = columnName;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.isForeignKey = isForignKey;
        this.isAutoIncrementColumn= isAutoIncrementColumn;
        this.isNotNullColumn = isNotNullColumn;
        this.isUnique = isUnique;
        ObjectMapper objectMapper = new ObjectMapper();
        this.foreignKey = objectMapper.createObjectNode();
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValue = defaultValue;
        this.isIndexedColumn = isIndexedColumn;
        this.primaryKeyDetails = new LinkedHashMap<>();
    }

    public Boolean getAutoIncrementColumn() {
        return isAutoIncrementColumn;
    }

    public Boolean getForeignKey() {
        return isForeignKey;
    }

    public Boolean getNotNullColumn() {
        return isNotNullColumn;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public String getColumnName() {
        return columnName;
    }

    public Boolean getUnique() {
        return isUnique;
    }

    public String getDataType() {
        return dataType;
    }
    public JsonNode foreignKey(){
        return foreignKey;
    }

    public Boolean getHasDefaultValue() {
        return hasDefaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setForeignKey(JsonNode foreignKey) {
        this.foreignKey = foreignKey;
    }

    public void setPrimaryKeyDetails(String tableName,String columnName,String onDelete,Table table,Column column) throws Exception {
        if(primaryKeyDetails.containsKey(tableName)){
            HashMap<String,Object> details = primaryKeyDetails.get(tableName);
            HashMap<String,Object> columns = (HashMap<String, Object>) details.get("columns");
            HashMap<String,Object> columnMap = new HashMap<>();
            columnMap.put("columnObject",column);
            columnMap.put("onDelete",onDelete);
            columns.put(columnName,columnMap);
        }
        else{
            HashMap<String,Object> details = new HashMap<>();
            details.put("tableObject",table);
            HashMap<String,Object> columnDetails = new HashMap<>();
            columnDetails.put("columnObject",column);
            columnDetails.put("onDelete",onDelete);
            LinkedHashMap<String,Object> columnDetailsMap = new LinkedHashMap<>();
            columnDetailsMap.put(columnName,details);
            primaryKeyDetails.put("columns",columnDetailsMap);
        }
    }
     public LinkedHashMap<String, LinkedHashMap<String, Object>> getPrimaryKeyDetails(){
        return primaryKeyDetails;
     }

    public Boolean getIndexedColumn() {
        return isIndexedColumn;
    }

    public void setIndexedColumn(Boolean indexedColumn) {
        isIndexedColumn = indexedColumn;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public void setAutoIncrementStartValue(Long autoIncrementStartValue) {
        this.autoIncrementStartValue = autoIncrementStartValue;
    }

    public Long getAutoIncrementStartValue() {
        return autoIncrementStartValue;
    }
}
