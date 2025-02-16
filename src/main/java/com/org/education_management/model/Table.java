package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Table {

    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("columns")
    private List<Column> columns;

    @JsonProperty("description")
    private String description;

    @JsonProperty("primaryKeys")
    private List<PrimaryKey> primaryKeys; // Updated to List<PrimaryKey>

    @JsonProperty("uniqueKeys")
    private List<UniqueKey> uniqueKeys; // For Unique Keys

    @JsonProperty("indexKeys")
    private List<IndexKey> indexKeys; // For Index Keys

    @JsonProperty("foreignKeys")
    private List<ForeignKey> foreignKeys;

    private PrimaryKey primaryKey;
    private UniqueKey uniqueKey;
    private IndexKey indexKey;
    private ForeignKey foreignKey;

    public Table() {}

    public Table(String tableName, String tableDesc, List<Column> columns, PrimaryKey primaryKey, ForeignKey foreignKey, UniqueKey uniqueKey, IndexKey indexKey) {
        this.tableName = tableName;
        this.description = tableDesc;
        this.columns = columns;
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
        this.uniqueKey = uniqueKey;
        this.indexKey = indexKey;
    }

    public UniqueKey getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(UniqueKey uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public IndexKey getIndexKey() {
        return indexKey;
    }

    public void setIndexKey(IndexKey indexKey) {
        this.indexKey = indexKey;
    }

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName.toLowerCase();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<PrimaryKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
        if(primaryKeys != null && !primaryKeys.isEmpty()) {
            this.primaryKey = primaryKeys.get(0);
        }
    }

    public List<UniqueKey> getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(List<UniqueKey> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
        if(uniqueKeys != null && !uniqueKeys.isEmpty()) {
            this.uniqueKey = uniqueKeys.get(0);
        }
    }

    public List<IndexKey> getIndexKeys() {
        return indexKeys;
    }

    public void setIndexKeys(List<IndexKey> indexKeys) {
        this.indexKeys = indexKeys;
        if(indexKeys != null && !indexKeys.isEmpty()) {
            this.indexKey = indexKeys.get(0);
        }
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
        if(foreignKeys != null && !foreignKeys.isEmpty()) {
            this.foreignKey = foreignKeys.get(0);
        }
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }
}