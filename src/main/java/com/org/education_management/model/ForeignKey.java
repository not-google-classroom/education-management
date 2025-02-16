package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class ForeignKey {

    @JsonProperty("referencedTable")
    private String referencedTable;

    @JsonProperty("referencedColumns")
    private List<String> referencedColumns;

    @JsonProperty("onDelete")
    private String onDelete;

    @JsonProperty("fkName")
    private String fkName;

    @JsonProperty("fkColumns")
    private List<String> fkColumns;


    public String getFkName() {
        return fkName;
    }

    public void setFkName(String fkName) {
        this.fkName = fkName;
    }

    public ForeignKey() {}

    public ForeignKey(HashMap<String, List<String>> fkMap) {
        if(fkMap != null && !fkMap.isEmpty()) {
            String fkGenName = fkMap.keySet().iterator().next();
            this.fkName = fkGenName;
        }
    }

    public String getReferencedTable() {
        return referencedTable.toLowerCase();
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable.toLowerCase();
    }

    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    public List<String> getReferencedColumns() {
        return referencedColumns;
    }

    public void setReferencedColumns(List<String> referencedColumn) {
        if (referencedColumn != null) {
            this.referencedColumns = referencedColumn.stream()
                    .map(String::toLowerCase)
                    .toList(); // Collect the transformed stream to a new list (Java 16+)
        } else {
            this.referencedColumns = null;
        }
    }

    public List<String> getFkColumns() {
        return fkColumns;
    }

    public void setFkColumns(List<String> fkColumns) {
        if (fkColumns != null) {
            this.fkColumns = fkColumns.stream()
                    .map(String::toLowerCase)
                    .toList(); // Collect the transformed stream to a new list (Java 16+)
        } else {
            this.fkColumns = null;
        }
    }
}