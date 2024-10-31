package com.org.education_management;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.*;

public class TestTable {
    private final DSLContext dslContext = DataBaseUtil.createDSLContext();

    public void createTable() {
        dslContext.createSchemaIfNotExists("my_schema").execute();
        dslContext.createTableIfNotExists("example_table")
                .column("id", SQLDataType.INTEGER.identity(true))
                .column("name", SQLDataType.VARCHAR(50))
                .column("age", SQLDataType.INTEGER)
                .constraints(
                        primaryKey("id")
                )
                .execute();
    }

    public void insertRecord(String name, int age) {
        dslContext.insertInto(table("example_table"))
                .columns(field("name"), field("age"))
                .values(name, age)
                .execute();
    }

    public Result<Record> getAllRecords() {
        return dslContext.select()
                .from("example_table")
                .fetch();
    }

    public Record getRecordById(int id) {
        return dslContext.select()
                .from("example_table")
                .where(field("id").eq(id))
                .fetchOne();
    }

    public Result<Record> getRecordsByAgeGreaterThan(int age) {
        return dslContext.select()
                .from("example_table")
                .where(field("age").greaterThan(age))
                .fetch();
    }

    public void updateRecord(int id, String name, int age) {
        dslContext.update(table("example_table"))
                .set(field("name"), name)
                .set(field("age"), age)
                .where(field("id").eq(id))
                .execute();
    }

    public void deleteRecordById(int id) {
        dslContext.deleteFrom(table("example_table"))
                .where(field("id").eq(id))
                .execute();
    }

    public void deleteRecordsByAgeLessThan(int age) {
        dslContext.deleteFrom(table("example_table"))
                .where(field("age").lessThan(age))
                .execute();
    }

    // Example usage method
    public void exampleUsage() {
        // Create table
        createTable();

        // Insert a record
        insertRecord("John Doe", 25);

        // Select records
        Result<Record> allRecords = getAllRecords();
        Record singleRecord = getRecordById(1);
        Result<Record> filteredRecords = getRecordsByAgeGreaterThan(20);

        // Update a record
        updateRecord(1, "Jane Doe", 26);

        // Delete records
        deleteRecordById(1);
        deleteRecordsByAgeLessThan(20);
    }
}