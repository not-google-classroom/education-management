package com.org.education_management.upgrade.factory;

import com.org.education_management.upgrade.bean.Column;
import com.org.education_management.upgrade.bean.Table;

import java.util.Properties;

public interface QueryConstructor {
    Properties constructTableQuery(Table table,int index,Properties properties);
    String constructNewColumnQuery(Table table, Column column) throws Exception;
    String constructDeleteColumnQuery(Table table, Column oldColumn);
    String alterExistingColumnsDataType(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnAddUniqueConstraint(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnDropUniqueConstraint(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnDropForeignKey(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnAddForignKey(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnAddNotNullContraint(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnDropNotNullContraint(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnAddDefault(Table table, Column newColumn,Column oldColumn);
    String alterExistingColumnDropDefault(Table table, Column newColumn,Column oldColumn);
    String alterTableDropPrimaryKey(Table table,Column column);
    Properties alterTableAddPrimaryKey(Table table,Column column,Properties properties,int proIndex);
}
