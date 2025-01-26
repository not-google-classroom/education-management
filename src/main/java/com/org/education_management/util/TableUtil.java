package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

public class TableUtil {
    private static TableUtil tableUtil = null;

    public static TableUtil getInstance() {
        if(tableUtil == null) {
            tableUtil = new TableUtil();
        }
        return tableUtil;
    }


    public Table getTableDetailsByName(String tableName) {
        if(tableName != null && !tableName.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Result<Record> result = dslContext.select().from("TableDetails")
                    .innerJoin(table("ColumnDetails")).on(field(name("tabledetails", "table_id")).eq(field(name("columndetails", "table_id"))))
                    .leftJoin(table("pkdetails")).on(field(name("pkdetails","pk_col_id")).eq(field(name("columndetails","column_id"))))
                    .leftJoin(table("ukdetails")).on(field(name("ukdetails","uk_col_id")).eq(field(name("columndetails","column_id"))))
                    .leftJoin(table("fkdetails")).on(field(name("fkdetails","fk_col_id")).eq(field(name("columndetails","column_id"))))
                    .leftJoin(table("ikdetails")).on(field(name("ikdetails","ik_col_id")).eq(field(name("columndetails","column_id"))))
                    .where(field("table_name").equalIgnoreCase(tableName)).fetch();
            List<Column> columns = new ArrayList<>();
            String tableDesc = null;
            ForeignKey foreignKey = null;
            PrimaryKey primaryKey = null;
            UniqueKey uniqueKey = null;
            IndexKey indexKey = null;
            HashMap<String, List<String>> pkMap = new HashMap<>();
            HashMap<String, List<String>> fkMap = new HashMap<>();
            HashMap<String, List<String>> ukMap = new HashMap<>();
            HashMap<String, List<String>> ikMap = new HashMap<>();
            for(Record record : result) {
                String colName = record.get(field(name("columndetails", "column_name"))).toString();
                String colType = record.get(field(name("columndetails", "column_type"))).toString();
                tableDesc = (String) record.get(field(name("tabledetails", "description")));
                Boolean isPrimary = (Boolean) record.get(field(name("columndetails", "is_primary_key")));
                Boolean isUnique = (Boolean) record.get(field(name("columndetails", "is_unique")));
                Boolean isForeign = (Boolean) record.get(field(name("columndetails", "is_foreign_key")));
                Boolean isIndex = record.get(field(name("ikdetails", "ik_gen_name"))) != null;
                Object defaultValue = record.get(field(name("columndetails","default_value")));
                Boolean isNull = (Boolean) record.get(field(name("columndetails", "is_nullable")));
                Column column = new Column(colName, colType, false, defaultValue, isPrimary, isUnique, isNull, isForeign);
                if(isPrimary) {
                    String pkGenName = record.get(field(name("pkdetails", "pk_gen_name"))).toString();
                    List<String> pkColList;
                    if(pkMap.get(pkGenName) == null) {
                        pkColList = new LinkedList<>();
                    } else {
                        pkColList = pkMap.get(pkGenName);
                    }
                    pkColList.add(colName);
                    pkMap.put(pkGenName, pkColList);
                }
                if(isForeign) {
                    String fkGenName = record.get(field(name("fkdetails", "fk_gen_name"))).toString();
                    String fkRefColName = findColumnName((Long) record.get(field(name("fkdetails", "fk_ref_col_id"))), (Long) record.get(field(name("fkdetails", "fk_ref_table_id"))));
                    String fkRefTableName = findTableName((Long) record.get(field(name("fkdetails", "fk_ref_table_id"))));
                    List<String> fkColList;
                    List<String> fkRefColList;
                    if(fkMap.get(fkGenName) == null) {
                        fkColList = new LinkedList<>();
                        fkRefColList = new LinkedList<>();
                    } else {
                        fkColList = fkMap.get(fkGenName);
                        fkRefColList = fkMap.get(fkRefColName);
                    }
                    fkColList.add(colName);
                    fkRefColList.add(fkRefColName);
                    fkMap.put(fkGenName, fkColList);
                }
                if(isUnique) {
                    String ukGenName = record.get(field(name("ukdetails", "uk_gen_name"))).toString();
                    List<String> ukColList;
                    if(ukMap.get(ukGenName) == null) {
                        ukColList = new LinkedList<>();
                    } else {
                        ukColList = ukMap.get(ukGenName);
                    }
                    ukColList.add(colName);
                    ukMap.put(ukGenName, ukColList);
                }
                if(isIndex) {
                    String ikGenName = record.get(field(name("pkdetails", "pk_gen_name"))).toString();
                    List<String> ikColList;
                    if(ikMap.get(ikGenName) == null) {
                        ikColList = new LinkedList<>();
                    } else {
                        ikColList = ikMap.get(ikGenName);
                    }
                    ikColList.add(colName);
                    ikMap.put(ikGenName, ikColList);
                }
                columns.add(column);
            }
            if(!pkMap.isEmpty()) {
                primaryKey = new PrimaryKey(pkMap);
            }
            if(!fkMap.isEmpty()) {
                foreignKey = new ForeignKey(fkMap);
            }
            if(!ukMap.isEmpty()) {
                //uniqueKey = new UniqueKey(ukMap);
            }
            if(!ikMap.isEmpty()) {
                //indexKey = new IndexKey(ikMap);
            }
            return new Table(tableName, tableDesc, columns, primaryKey, foreignKey, uniqueKey, indexKey);
        }
        return null;
    }

    private ForeignKey getFKDetailsByGenName(String fkeyName) {
        ForeignKey foreignKey = null;
        if(fkeyName != null && !fkeyName.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select()
                    .from(table("fkdetails"))
                    .where(field("fkey_gen_name").eq(fkeyName))
                    .fetchOne();
            if (record != null) {
                Long fkTId = (Long) record.get("fkey_table_id");
                Long fkCId = (Long) record.get("fkey_column__id");
                String refTableName = findTableName(fkTId);
                String refColName = findColumnName(fkCId, fkTId);
                if(refTableName != null && refColName != null) {
                   // return new ForeignKey(fkeyName, refTableName, refColName);
                }
            }
        }
        return foreignKey;
    }

    public Long findTableId(String tableName) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(field("table_id", Long.class))
                .from(table("tabledetails"))
                .where(field("table_name").eq(tableName))
                .fetchOne();
        return record != null ? (Long) record.get(field("table_id")) : null;
    }

    public Long findColumnId(String columnName, Long tableId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(field("column_id", Long.class))
                .from(table("columndetails"))
                .where(field("column_name").eq(columnName))
                .and(field("table_id").eq(tableId))
                .fetchOne();
        return record != null ? (Long) record.get(field("column_id")) : null;
    }

    public String findTableName(Long tableID) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(field("table_name", String.class))
                .from(table("tabledetails"))
                .where(field("table_id").eq(tableID))
                .fetchOne();
        return record != null ? (String) record.get(field("table_name")) : null;
    }

    public String findColumnName(Long columnID, Long tableId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(field("column_name", String.class))
                .from(table("columndetails"))
                .where(field("column_id").eq(columnID))
                .and(field("table_id").eq(tableId))
                .fetchOne();
        return record != null ? (String) record.get(field("column_name")) : null;
    }
}
