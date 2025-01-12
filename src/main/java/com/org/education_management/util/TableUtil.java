package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.model.Column;
import com.org.education_management.model.ForeignKey;
import com.org.education_management.model.Table;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.ArrayList;
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
            Result<Record> result = dslContext.select().from("TableDetails").innerJoin(table("ColumnDetails")).on(field(name("tabledetails", "table_id")).eq(field(name("columndetails", "table_id")))).where(field("table_name").equalIgnoreCase(tableName)).fetch();
            List<Column> columns = new ArrayList<>();
            String tableDesc = null;
            for(Record record : result) {
                String colName = record.get(field(name("columndetails", "column_name"))).toString();
                String colType = record.get(field(name("columndetails", "column_type"))).toString();
                tableDesc = (String) record.get(field(name("tabledetails", "description")));
                Boolean isPrimary = (Boolean) record.get(field(name("columndetails", "is_primary_key")));
                Boolean isUnique = (Boolean) record.get(field(name("columndetails", "is_unique")));
                String fkeyName = (String) record.get(field(name("columndetails", "fkey_gen_name")));
                Boolean isNull = (Boolean) record.get(field(name("columndetails", "is_nullable")));
                ForeignKey foreignKey = null;
                if(fkeyName != null && !fkeyName.equals("--")) {
                    foreignKey = getFKDetailsByGenName(fkeyName);
                }
                Column column = new Column(colName, colType, isPrimary, isUnique, isNull, foreignKey);
                columns.add(column);
            }
            return new Table(tableName, tableDesc, columns);
        }
        return null;
    }

    private ForeignKey getFKDetailsByGenName(String fkeyName) {
        ForeignKey foreignKey = null;
        if(fkeyName != null && !fkeyName.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select()
                    .from(DSL.table("fkdetails"))
                    .where(DSL.field("fkey_gen_name").eq(fkeyName))
                    .fetchOne();
            if (record != null) {
                Long fkTId = (Long) record.get("fkey_table_id");
                Long fkCId = (Long) record.get("fkey_column__id");
                String refTableName = findTableName(fkTId);
                String refColName = findColumnName(fkCId, fkTId);
                if(refTableName != null && refColName != null) {
                    return new ForeignKey(fkeyName, refTableName, refColName);
                }
            }
        }
        return foreignKey;
    }

    public Long findTableId(String tableName) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("table_id", Long.class))
                .from(DSL.table("tabledetails"))
                .where(DSL.field("table_name").eq(tableName))
                .fetchOne();
        return record != null ? (Long) record.get(field("table_id")) : null;
    }

    public Long findColumnId(String columnName, Long tableId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("column_id", Long.class))
                .from(DSL.table("columndetails"))
                .where(DSL.field("column_name").eq(columnName))
                .and(DSL.field("table_id").eq(tableId))
                .fetchOne();
        return record != null ? (Long) record.get(field("column_id")) : null;
    }

    public String findTableName(Long tableID) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("table_name", String.class))
                .from(DSL.table("tabledetails"))
                .where(DSL.field("table_id").eq(tableID))
                .fetchOne();
        return record != null ? (String) record.get(field("table_name")) : null;
    }

    public String findColumnName(Long columnID, Long tableId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(DSL.field("column_name", String.class))
                .from(DSL.table("columndetails"))
                .where(DSL.field("column_id").eq(columnID))
                .and(DSL.field("table_id").eq(tableId))
                .fetchOne();
        return record != null ? (String) record.get(field("column_name")) : null;
    }
}
