package com.org.education_management.database;

import com.org.education_management.util.AppProperty;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseUtil {

    private static DSLContext dslContext;

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
}