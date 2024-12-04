package com.org.education_management.upgrade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.upgrade.bean.Column;
import com.org.education_management.upgrade.bean.Table;
import com.org.education_management.upgrade.factory.QueryConstructor;

import java.util.*;

public class PostgresQueryConstructor implements QueryConstructor {
    public Properties constructTableQuery(Table table,int index,Properties queryProp){
        Properties resultQueries = new Properties();
        String resultString = "CREATE TABLE "+table.getTableName()+" ( ";
        LinkedHashMap<String,Column> columns = table.getColumnList();
        Iterator<String> column = columns.keySet().iterator();
        Boolean isTableContainsPk = Boolean.FALSE;
        String primaryKeyString = "CONSTRAINT ${pk_name} PRIMARY KEY (";
        int countOfColumns = table.getColumnList().size();
        String columnQueries = "";
        int countIterationValue = 0;
        String createColumnTemplate = "${column_name} ${data_type} ${is_null_able} ${is_unique} ${default}";
        while (column.hasNext()){
            String query = createColumnTemplate;
            countIterationValue++;
            boolean isNullable = Boolean.FALSE;
            boolean isUniqueString = Boolean.FALSE;
            String defaultValue = "DEFAULT ${value}";
            Column columnObject = columns.get(column.next());
            Boolean isNullAbleColumn = columnObject.getNotNullColumn();
            Boolean isUnique = columnObject.getUnique();
            Boolean isDefault = columnObject.getHasDefaultValue();
            if(isNullAbleColumn){
                isNullable = Boolean.TRUE;
            }
            if(isUnique){
                isUniqueString = Boolean.TRUE;
            }
            query = query.replaceAll("\\$\\{column_name\\}",columnObject.getColumnName());
            if(columnObject.getAutoIncrementColumn()){
                query = query.replaceAll("\\$\\{data_type\\}","SERIAL");
            }
            else{
                query = query.replaceAll("\\$\\{data_type\\}",columnObject.getDataType());
            }
            if(isNullable){
                query = query.replaceAll("\\$\\{is_null_able\\}","NOT NULL");
            }
            else{
                query = query.replaceAll("\\$\\{is_null_able\\}","");
            }
            if(isUniqueString) {
                query = query.replaceAll("\\$\\{is_unique\\}","UNIQUE");
            }
            else{
                query = query.replaceAll("\\$\\{is_unique\\}","");
            }
            if(isDefault){
                query = query.replaceAll("\\$\\{default\\}",defaultValue.replaceAll("\\$\\{value\\}",columnObject.getDefaultValue()!=null?"'"+columnObject.getDefaultValue()+"'":"null"));
            }
            else{
                query = query.replaceAll("\\$\\{default\\}","");
            }
            query = query.replaceAll("\\$\\{column_name\\}",columnObject.getColumnName());
            if (columnObject.getForeignKey()) {
                String foreignKeyQuery = "CONSTRAINT fk_${table_name}_${column_name} FOREIGN KEY (${column_name}) REFERENCES reference_table_name(${reference_column_name}) ON DELETE CASCADE ON UPDATE CASCADE";
                JsonNode foreignKeyDetails = columnObject.foreignKey();
                foreignKeyQuery = foreignKeyQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
                foreignKeyQuery = foreignKeyQuery.replaceAll("\\$\\{column_name\\}",columnObject.getColumnName());
                foreignKeyQuery = foreignKeyQuery.replaceAll("\\$\\{reference_table_name\\}",foreignKeyDetails.get("referencedTable").asText());
                foreignKeyQuery = foreignKeyQuery.replaceAll("\\$\\{reference_column_name\\}",foreignKeyDetails.get("referencedColumn").asText());
                query = query + "," + foreignKeyQuery;
            }
            if(countIterationValue==1){
                columnQueries =  query;
            }
            else{
                columnQueries = columnQueries + "," + query;
            }
        }
        LinkedHashMap<String, List<String>> pkDetails = table.getTablesPkDetails();
        Iterator<String> pkIterator = pkDetails.keySet().iterator();
        String pkQuery = "";
        while (pkIterator.hasNext()){
            String pkName = pkIterator.next();
            pkQuery = primaryKeyString;
            pkName = pkName.replaceAll("\\$\\{pk_name}",pkName);
            List<String> pkColumn = pkDetails.get(pkName);
            Iterator<String> pkColumnIterator = pkColumn.iterator();
            String columnName = "";
            while (pkColumnIterator.hasNext()){
                if(columnName.equalsIgnoreCase("")){
                    columnName = pkColumnIterator.next() + ",";
                }
                else{
                    columnName = columnName + "," + pkColumnIterator.next();
                }
            }
            pkQuery = pkQuery+columnName+")";
        }
        if(isTableContainsPk){
            columnQueries = columnQueries + "," + pkQuery;
        }
        resultString = resultString + columnQueries +")";
        queryProp.put("query_"+index,resultString);
        return resultQueries;
    }

    public String contructNewColumnQuery(Table table,Column column) throws Exception{
        String resultString = "ALTER TABLE ${table_name} ADD COLUMN ${column_name} ${data_type} ${contraint};";
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{column_name\\}",column.getColumnName());
        String constraint = "";
        if(column.getForeignKey()){
            throw new Exception("cannot create new column as foreign key...");
        }
        if(column.getHasDefaultValue()){
            constraint = " DEFAULT "+column.getDefaultValue()+" ";
        }
        if(column.getNotNullColumn()){
            constraint = " NOT NULL ";
        }
        if(column.getUnique()){
            constraint = "UNIQUE";
        }
        if(column.getAutoIncrementColumn()){
            resultString = resultString.replaceAll("\\$\\{data_type\\}","SERIAL");
        }
        else{
            resultString = resultString.replaceAll("\\$\\{data_type\\}",column.getDataType());
        }
        resultString = resultString.replaceAll("\\$\\{contraint}",constraint);
        return resultString;
    }
    public String constructDeleteColumnQuery(Table table, Column oldColumn){
        String resultString = "ALTER TABLE ${table_name} DROP COLUMN ${column_name} CASCADE;";
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{column_name\\}",oldColumn.getColumnName());
        return resultString;
    }
    public String alterExistingColumnsDataType(Table table, Column newColumn,Column oldColumn){
        String tableName = table.getTableName();
        String columnName = newColumn.getColumnName();
        String dataType = newColumn.getDataType();
        String createColumnTemplate = "ALTER TABLE ${table_name} ALTER COLUMN ${column_name} TYPE ${data_type};";
        String result = createColumnTemplate.replaceAll("\\$\\{table_name\\}", tableName);
        result = result.replaceAll("\\$\\{column_name\\}",columnName);
        result = result.replaceAll("\\$\\{data_type\\}",dataType);
        return result;
    }
    public String alterExistingColumnAddUniqueConstraint(Table table, Column newColumn,Column oldColumn){
        String resultString = "ALTER TABLE ${table_name} ADD CONSTRAINT UNIQUE (${column_name});";
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{column_name\\}",newColumn.getColumnName());
        return resultString;
    }
    public String alterExistingColumnDropUniqueConstraint(Table table, Column newColumn,Column oldColumn){
        String resultString = "ALTER TABLE ${table_name} DROP CONSTRAINT IF EXISTS ${column_name};";
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{column_name\\}",oldColumn.getColumnName());
        return resultString;
    }
    public String alterExistingColumnDropForeignKey(Table table, Column newColumn,Column oldColumn){
        String resultString = "ALTER TABLE ${table_name} DROP CONSTRAINT ${constraint_name};";
        JsonNode foreignKey = oldColumn.foreignKey();
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{constraint_name\\}",foreignKey.get("fk_name").asText());
        return resultString;
    }
    public String alterExistingColumnAddForignKey(Table table, Column newColumn,Column oldColumn){
        String resultString = "ALTER TABLE ${table_name} ADD CONSTRAINT fk_${table_name}_${column_name} FOREIGN KEY (${column_name}) REFERENCES ${reference_table_name}(reference_column_name) ON DELETE CASCADE ON UPDATE CASCADE;";
        JsonNode foreignKey = newColumn.foreignKey();
        resultString = resultString.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultString = resultString.replaceAll("\\$\\{column_name\\}",newColumn.getColumnName());
        resultString = resultString.replaceAll("\\$\\{reference_table_name\\}",foreignKey.get("referencedTable").asText());
        resultString = resultString.replaceAll("\\$\\{reference_column_name\\}",foreignKey.get("referencedColumn").asText());
        return resultString;
    }
    public String alterExistingColumnAddNotNullContraint(Table table, Column newColumn,Column oldColumn){
        String resultQuery ="ALTER TABLE ${table_name} ALTER COLUMN ${column_name} SET NOT NULL;";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultQuery = resultQuery.replaceAll("\\$\\{column_name\\}",newColumn.getColumnName());
        return resultQuery;
    }
    public String alterExistingColumnDropNotNullContraint(Table table, Column newColumn,Column oldColumn){
        String resultQuery ="ALTER TABLE ${table_name} ALTER COLUMN ${column_name} DROP NOT NULL;";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultQuery = resultQuery.replaceAll("\\$\\{column_name\\}",oldColumn.getColumnName());
        return resultQuery;
    }
    public String alterExistingColumnAddDefault(Table table, Column newColumn,Column oldColumn){
        String resultQuery ="ALTER TABLE ${table_name} ALTER COLUMN ${column_name} SET DEFAULT ${default_value};";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultQuery = resultQuery.replaceAll("\\$\\{column_name\\}",newColumn.getColumnName());
        resultQuery = resultQuery.replaceAll("\\$default_valuecolumn_name\\}","'"+newColumn.getDefaultValue()+"'");
        return resultQuery;
    }
    public String alterExistingColumnDropDefault(Table table, Column newColumn,Column oldColumn){
        String resultQuery ="ALTER TABLE ${table_name} ALTER COLUMN ${column_name} DROP DEFAULT;";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultQuery = resultQuery.replaceAll("\\$\\{column_name\\}",oldColumn.getColumnName());
        return resultQuery;
    }
    public String alterTableDropPrimaryKey(Table table,Column column){
        String resultQuery = "ALTER TABLE ${table_name} DROP CONSTRAINT ${pk_name};";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        resultQuery = resultQuery.replaceAll("\\$\\{column_name\\}",column.getPkName());
        return resultQuery;
    }
    public Properties alterTableAddPrimaryKey(Table table,Column column,Properties properties,int proIndex){
        String resultQuery = "ALTER TABLE ${table_name} ADD CONSTRAINT ${pk_name} PRIMARY KEY (${column_name});";
        resultQuery = resultQuery.replaceAll("\\$\\{table_name\\}",table.getTableName());
        LinkedHashMap<String,List<String>> primaryKeyColumns = table.getTablesPkDetails();
        Iterator<String> primaryKey = primaryKeyColumns.keySet().iterator();
        while (primaryKey.hasNext()){
            String query = resultQuery;
            String primaryKeyName = primaryKey.next();
            query = query.replaceAll("\\$\\{pk_name\\}",primaryKeyName);
            List<String> columnList = primaryKeyColumns.get(primaryKeyName);
            String columnsName ="";
            for (int index=0;index<columnList.size();index++){
                if (index==0) {
                    columnsName = columnList.get(index);
                } else{
                    columnsName = columnsName + "," + columnList.get(index);
                }
            }
            query = query.replaceAll("\\$\\{column_name\\}",columnsName);
            properties.put("query_" + proIndex++, query);
        }
        return properties;
    }
}
