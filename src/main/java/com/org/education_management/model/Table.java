package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Table {
    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("columns")
    private List<Column> columns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
