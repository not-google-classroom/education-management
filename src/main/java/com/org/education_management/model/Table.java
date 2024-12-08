package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Table {
    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("columns")
    private List<Column> columns;

    @JsonProperty("description")
    private String descritpion;

    public Table() {}

    public Table(String tableName, String tableDesc, List<Column> columns) {
        this.tableName = tableName.toLowerCase();
        this.columns = columns;
        this.descritpion = tableDesc;
    }

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

    public String getDescritpion() {
        return descritpion;
    }

    public void setDescritpion(String descritpion) {
        this.descritpion = descritpion;
    }
}
