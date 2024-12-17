package com.org.education_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class IndexKey {
    @JsonProperty("ikName")
    private String ikName;

    @JsonProperty("ikColumns")
    private List<String> ikColumns;

    public IndexKey() {}

    public String getIkName() {
        return ikName;
    }

    public void setIkName(String ikName) {
        this.ikName = ikName;
    }

    public List<String> getIkColumns() {
        return ikColumns;
    }

    public void setIkColumns(List<String> ikColumns) {
        if (ikColumns != null) {
            this.ikColumns = ikColumns.stream()
                    .map(String::toLowerCase)
                    .toList(); // Collect the transformed stream to a new list (Java 16+)
        } else {
            this.ikColumns = null;
        }
    }
}