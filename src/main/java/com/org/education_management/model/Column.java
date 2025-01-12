package com.org.education_management.model;

public class Column {
    private String name;
    private String type;
    private PrimaryKey primaryKey;
    private UniqueKey unique;
    private Boolean notNull = false;
    private Boolean autoIncrement = false;
    private ForeignKey foreignKey;
    private String defaultValue;

    public Column() {}

    public Column(String colName, String colType, Boolean isPrimary, Boolean isUnique, Boolean isNull, ForeignKey foreignKey) {

    }

    public String getName() {
        return name.toLowerCase();
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getNotNull() {
        return notNull;
    }

    public void setNotNull(Boolean notNull) {
        this.notNull = notNull;
    }

    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}