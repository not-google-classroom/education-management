package com.org.education_management.model;

public class Column {
    private String name;
    private String type;
    private Boolean notNull = false;
    private Boolean autoIncrement = false;
    private Object defaultValue;

    private Boolean isPrimary;
    private Boolean isForeign;
    private Boolean isUnique;

    public Column() {
    }

    public Column(String colName, String colType, Boolean autoIncrement, Object defaultValue, Boolean isPrimary, Boolean isUnique, Boolean isNull, Boolean isForeignKey) {
        this.name = colName;
        this.type = colType;
        this.autoIncrement = autoIncrement;
        this.notNull = isNull;
        this.defaultValue = defaultValue;
        this.isPrimary = isPrimary;
        this.isForeign = isForeignKey;
        this.isUnique = isUnique;
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Boolean getForeign() {
        return isForeign;
    }

    public void setForeign(Boolean foreign) {
        isForeign = foreign;
    }

    public Boolean getUnique() {
        return isUnique;
    }

    public void setUnique(Boolean unique) {
        isUnique = unique;
    }
}