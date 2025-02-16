package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryKey {
    @JsonProperty("pkName")
    private String pkName;

    @JsonProperty("pkColumns")
    private List<String> pkColumns;

    public PrimaryKey() {}

    public PrimaryKey(HashMap<String, List<String>> pkMap) {
        if(pkMap != null && !pkMap.isEmpty()) {
            String pkGenName = pkMap.keySet().iterator().next();
            this.pkName = pkGenName;
            this.pkColumns = pkMap.get(pkGenName);
        }
    }

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