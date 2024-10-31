package com.org.education_management.database;

import com.org.education_management.util.AppProperty;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseUtil {
    public static DSLContext createDSLContext() {
        String url = AppProperty.getInstance().getProperty("spring.datasource.url");
        String user = AppProperty.getInstance().getProperty("spring.datasource.username");
        String password = AppProperty.getInstance().getProperty("spring.datasource.password");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return DSL.using(conn, SQLDialect.MYSQL); // Use the appropriate SQL dialect for your database
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating DSL context", e);
        }
    }
}
