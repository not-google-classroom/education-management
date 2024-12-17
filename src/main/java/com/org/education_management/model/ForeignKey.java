package com.org.education_management.model;

public class ForeignKey {
    private String referencedTable;
    private String referencedColumn;
    private String onDelete;

    public ForeignKey() {}

    public ForeignKey(String refTableName, String refColName) {
        this.referencedTable = refTableName;
        this.referencedColumn = refColName;
    }

    public String getReferencedTable() {
        return referencedTable.toLowerCase();
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable.toLowerCase();
    }

    public String getReferencedColumn() {
        return referencedColumn.toLowerCase();
    }

    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn.toLowerCase();
    }

    public String getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(String onDelete) {
        this.onDelete = onDelete;
    }
}