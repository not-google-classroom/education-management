package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Row;
import org.jooq.Rows;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.jooq.impl.DSL.table;

public class UniqueValueGenerator {
    // Map to store counters for each table
    private static final ConcurrentHashMap<String, AtomicLong> tableCounters = new ConcurrentHashMap<>();
    private static final Map<String, Long> allUVGValues = new HashMap<>();
    private static final Map<String, Long> updateRequiredValues = new HashMap<>();

    private static UniqueValueGenerator uniqueValueGenerator = null;

    public UniqueValueGenerator() {
        getAllUVGValues();
    }

    public static UniqueValueGenerator getInstance() {
        if(uniqueValueGenerator == null) {
            uniqueValueGenerator = new UniqueValueGenerator();
        }
        return uniqueValueGenerator;
    }

    // Method to get the next unique ID for a given table
    public long getNextId(String tableName, String columnName) {
        tableCounters.putIfAbsent(tableName, new AtomicLong(getStartIDForTable(tableName, columnName)));
        return tableCounters.get(tableName).getAndIncrement();
    }

    private static long getStartIDForTable(String tableName, String columnName) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        if(tableName != null && columnName != null && !columnName.isEmpty() && !tableName.isEmpty()) {
            String seqName = tableName.toLowerCase() + "_" + columnName.toLowerCase() + "_seq";
            return (long) dslContext.fetchValue("SELECT nextval('" + seqName +"')");
        } else {
            return -1L;
        }
    }

    private static void getAllUVGValues() {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<Record> result = dslContext.select().from("uvhdetails").fetch();
        for(Record record : result) {
            allUVGValues.put((String) record.get(record.field("uvh_name")), (Long) record.get(result.field("uvh_value")));
        }
    }

    public Long getUVGFor(String uvgName) {
        Long value = null;
        if(!allUVGValues.isEmpty() && allUVGValues.containsKey(uvgName)) {
            value = allUVGValues.get(uvgName);
        }
        return value;
    }


    public void updateUniqueValueToMap(String columnValue, Long uniqueGenValue) {
        if(columnValue.contains("uvg") && uniqueGenValue != null) {
            allUVGValues.put(columnValue, uniqueGenValue);
            updateRequiredValues.put(columnValue, uniqueGenValue);
        }
    }

    public void updateValuesToDB() {
        if (!updateRequiredValues.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            List<? extends TableRecord<?>> records = updateRequiredValues.entrySet().stream()
                    .map(entry -> {
                        Table<?> uvhDetailsTable = table(DSL.name("uvhdetails"));
                        TableRecord<?> record = (TableRecord<?>) dslContext.newRecord(uvhDetailsTable);
                        record.set(DSL.field(DSL.name("uvh_name")), entry.getKey());
                        record.set(DSL.field(DSL.name("uvh_value")), entry.getValue());

                        return record;
                    })
                    .toList();
            dslContext.batchInsert(records).execute();
        }
    }
}
