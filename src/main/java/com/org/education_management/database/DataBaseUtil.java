package com.org.education_management.database;

import com.org.education_management.util.AppProperty;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBaseUtil {

    private static DSLContext dslContext;
    private static final Logger logger = Logger.getLogger(DataBaseUtil.class.getName());

    private DataBaseUtil() {}

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

    public static void batchUpdateQueries(String sqlList) throws Exception {
        if(sqlList != null && !sqlList.isEmpty()) {
            getDSLContext().execute(sqlList);
            logger.log(Level.INFO, "Updated list of queries to the database");
            return;
        }
        throw new Exception("Exception when inserting or updating queries to database!");
    }
}