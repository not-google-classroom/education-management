package com.org.education_management.model;

public class Column {
    private String name;
    private String type;
    private Boolean primaryKey = false;
    private Boolean unique = false;
    private Boolean notNull = false;
    private Boolean autoIncrement;
    private ForeignKey foreignKey;
    private String defaultValue;

    public Column() {}

    public Column(String colName, String colType, Boolean isPrimary, Boolean isUnique, Boolean isNull, ForeignKey foreignKey) {
        this.name = colName.toLowerCase();
        this.type = colType;
        this.primaryKey = isPrimary;
        this.unique = isUnique;
        this.notNull = isNull;
        this.foreignKey = foreignKey;
    }

    public String getName() {
        return name;
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

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
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
