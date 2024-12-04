package com.org.education_management.upgrade.impl;

import com.org.education_management.upgrade.bean.Column;
import com.org.education_management.upgrade.bean.Table;
import com.org.education_management.upgrade.factory.QueryConstructor;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class PostgresQueryConstructor implements QueryConstructor {
    public String constructTableQuery(Table table){
        String resultString = "CREATE TABLE "+table.getTableName()+" ( ";
        LinkedHashMap<String,Column> columns = table.getColumnList();
        Iterator<String> column = columns.keySet().iterator();
        while (column.hasNext()){
            String columnName = column.next();
            Column columnObject = columns.get(columnName);
            String dataType = columnObject.getDataType();
            Boolean isPrimaryKey = columnObject.getIsPrimaryKey();

        }
        return resultString;
    }
    public String constructNewColumnQuery(Table table, Column column){
        String resultString = null;
        if(column.getIsPrimaryKey()){

        }
        return resultString;
    }
    public String constructDeleteColumnQuery(Table table, Column column){
        String resultString = null;
        if(column.getIsPrimaryKey()){

        }
        return resultString;
    }
    public String alterExistingColumnsDataType(Table table, Column column){
        return "";
    }
    public String alterExistingColumnAddPrimaryKey(Table table, Column column){
        return "";
    }
    public String alterExistingColumnDropPrimaryKey(Table table, Column column){
        return "";
    }
    public String alterExistingColumnAddUniqueConstraint(Table table, Column column){
        return "";
    }
    public String alterExistingColumnDropUniqueConstraint(Table table, Column column){
        return "";
    }
    public String alterExistingColumnAddForeignKey(Table table, Column column){
        return "";
    }
    public String alterExistingColumnDropForeignKey(Table table, Column column){
        return "";
    }
    public String alterExistingColumnAddNotNullContraint(Table table, Column column){
        return "";
    }
    public String alterExistingColumnDropNotNullContraint(Table table, Column column){
        return "";
    }
    public String alterExistingColumnAddIsAutoIncrementContraint(Table table, Column column){
        return "";
    }
    public String alterExistingColumnDropIsAutoIncrementContraint(Table table, Column column){
        return "";
    }
}
