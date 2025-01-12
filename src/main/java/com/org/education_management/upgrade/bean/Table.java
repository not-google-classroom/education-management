package com.org.education_management.upgrade.bean;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Table {
    private String tableName;
    private String moduleName;
    private LinkedHashMap<String,Column> columnMap;
    private String tableDesc;
    private Boolean hasPrimaryKey;
    private LinkedHashMap<String, List<String>> tablesPkDetails;
    private List<String> pkColumnList;
    private LinkedHashMap<String, List<String>> tablesIndexDetails;
    private List<String> indexColumnList;
    public Table(String tableName,String tableDesc,String moduleName,Boolean hasPrimaryKey){
        this.tableName = tableName;
        this.columnMap = new LinkedHashMap<>();
        this.tableDesc = tableDesc;
        this.moduleName = moduleName;
        this.tablesPkDetails = new LinkedHashMap<>();
        this.hasPrimaryKey = hasPrimaryKey;
        this.pkColumnList = new ArrayList<>();
        this.tablesIndexDetails = new LinkedHashMap<>();
        this.indexColumnList = new ArrayList<>();
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setColumnDetails(LinkedHashMap<String,Column> columnHashMap) {
        this.columnMap = columnHashMap;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public LinkedHashMap<String, Column> getColumnList() {
        return columnMap;
    }

    public String getTableName() {
        return tableName;
    }

    public LinkedHashMap<String, List<String>> getTablesPkDetails() {
        return tablesPkDetails;
    }

    public void setTablesPkDetails(LinkedHashMap<String, List<String>> tablesPkDetails,List<String> columnDetails) {
        this.tablesPkDetails = tablesPkDetails;
        this.pkColumnList = columnDetails;
    }

    public Boolean getHasPrimaryKey() {
        return hasPrimaryKey;
    }

    public void setHasPrimaryKey(Boolean hasPrimaryKey) {
        this.hasPrimaryKey = hasPrimaryKey;
    }

    public LinkedHashMap<String, List<String>> getTablesIndexDetails() {
        return tablesIndexDetails;
    }

    public void setTablesIndexDetails(LinkedHashMap<String, List<String>> tablesIndexDetails) {
        this.tablesIndexDetails = tablesIndexDetails;
    }

    public void setIndexColumnList(List<String> indexColumnList) {
        this.indexColumnList = indexColumnList;
    }

    public List<String> getIndexColumnList() {
        return indexColumnList;
    }

    public List<String> getPkColumnList() {
        return pkColumnList;
    }
}
