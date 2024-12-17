package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UniqueKey {
    @JsonProperty("ukName")
    private String ukName;

    @JsonProperty("ukColumns")
    private List<String> ukColumns;

    public UniqueKey() {}

    public String getUkName() {
        return ukName;
    }

    public void setUkName(String ukName) {
        this.ukName = ukName;
    }

    public List<String> getUkColumns() {
        return ukColumns;
    }

    public void setUkColumns(List<String> ukColumns) {
        if (ukColumns != null) {
            this.ukColumns = ukColumns.stream()
                    .map(String::toLowerCase)
                    .toList(); // Collect the transformed stream to a new list (Java 16+)
        } else {
            this.ukColumns = null;
        }
    }
}