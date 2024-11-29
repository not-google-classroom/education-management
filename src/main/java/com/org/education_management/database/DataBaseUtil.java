package com.org.education_management.database;

import com.org.education_management.util.AppProperty;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class DataBaseUtil {

    private static DSLContext dslContext;
    private static final Logger logger = Logger.getLogger(DataBaseUtil.class.getName());

    private DataBaseUtil() {
    }

    public static DSLContext getDSLContext() {
        if (dslContext == null) {
            try {
                String url = AppProperty.getInstance().getProperty("spring.datasource.url");
                String user = AppProperty.getInstance().getProperty("spring.datasource.username");
                String password = AppProperty.getInstance().getProperty("spring.datasource.password");

                Connection connection = DriverManager.getConnection(url, user, password);
                dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Error creating DSL context", e);
            }
        }
        return dslContext;
    }

    /**
     * Inserts data into a table.
     *
     * @param tableName The name of the table.
     * @param dataMap   A map of column names and their corresponding values.
     */
    public static void insertData(String tableName, Map<String, Object> dataMap) {
        try {
            DSLContext context = getDSLContext();
            // Build the INSERT statement dynamically
            context.insertInto(table(tableName))
                    .set((Map<?, ?>) dataMap.entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    entry -> field(entry.getKey()), // Column name
                                    Map.Entry::getValue            // Column value
                            )))
                    .execute();

            logger.log(Level.INFO, "Data inserted successfully into table: " + tableName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inserting data into table: " + tableName, e);
        }
    }

    /**
     * Executes a SELECT query and returns the result.
     *
     * @param tableName The name of the table.
     * @param columns   The columns to select (use "*" for all columns).
     * @return A Result object containing the selected data.
     */
    public static Result<?> selectData(String tableName, String... columns) {
        try {
            DSLContext context = getDSLContext();
            return context.select(columns.length > 0 ? field(Arrays.toString(columns)) : DSL.asterisk())
                    .from(table(tableName))
                    .fetch();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing SELECT query on table: " + tableName, e);
            throw new RuntimeException(e);
        }
    }

    public static void batchUpdateQueries(String sqlList) throws Exception {
        if (sqlList != null && !sqlList.isEmpty()) {
            getDSLContext().execute(sqlList);
            logger.log(Level.INFO, "Updated list of queries to the database");
            return;
        }
        throw new Exception("Exception when inserting or updating queries to database!");
    }
}