package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PrimaryKey {
    @JsonProperty("pkName")
    private String pkName;

    @JsonProperty("pkColumns")
    private List<String> pkColumns;

    public PrimaryKey() {}

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName.toLowerCase();
    }

    public List<String> getPkColumns() {
        return pkColumns;
    }

    public void setPkColumns(List<String> pkColumns) {
        if (pkColumns != null) {
            this.pkColumns = pkColumns.stream()
                    .map(String::toLowerCase)
                    .toList(); // Collect the transformed stream to a new list (Java 16+)
        } else {
            this.pkColumns = null;
        }
    }
}