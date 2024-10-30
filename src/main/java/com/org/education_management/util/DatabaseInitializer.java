package com.org.education_management.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(String.valueOf(DatabaseInitializer.class));

    // Utility method to get a connection to the PostgreSQL server
    private static Connection getConnection(String url, String adminUser, String adminPassword) throws SQLException {
        url = url + "postgres"; // Connect to the default 'postgres' database
        return DriverManager.getConnection(url, adminUser, adminPassword);
    }

    // Method to drop the database if it exists
    public static void dropDatabaseIfExists(String url, String adminUser, String adminPassword, String databaseName) {
        try (Connection connection = getConnection(url, adminUser, adminPassword);
             Statement statement = connection.createStatement()) {

            // Check if the database exists
            String checkDbExistsSql = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
            var resultSet = statement.executeQuery(checkDbExistsSql);

            if (resultSet.next()) {
                // Database exists, terminate connections and drop it
                terminateConnections(connection, databaseName);
                String dropDbSql = "DROP DATABASE " + databaseName;
                statement.executeUpdate(dropDbSql);
                logger.log(Level.INFO, "Database {0} dropped successfully.", databaseName);
            } else {
                logger.log(Level.INFO, "Database {0} doesn't exists , so skipping...", databaseName);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle database errors
            throw new RuntimeException("Failed to drop database.", e);
        }
    }

    // Method to create the database if it does not exist
    public static void createDatabaseIfNotExists(String url, String adminUser, String adminPassword, String databaseName) {
        try (Connection connection = getConnection(url, adminUser, adminPassword);
             Statement statement = connection.createStatement()) {

            // Check if the database exists
            String checkDbExistsSql = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
            var resultSet = statement.executeQuery(checkDbExistsSql);

            if (!resultSet.next()) {
                // Database does not exist, create it
                String createDbSql = "CREATE DATABASE " + databaseName;
                statement.executeUpdate(createDbSql);
                logger.log(Level.INFO, "Database {0} created successfully.", databaseName);
            } else {
                logger.log(Level.INFO, "Database {0} already exists so skipping creation process.", databaseName);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle database errors
            throw new RuntimeException("Failed to create database.", e);
        }
    }

    // Utility method to terminate active connections to the database
    private static void terminateConnections(Connection connection, String databaseName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String terminateConnectionsSql = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
                    "FROM pg_stat_activity WHERE pg_stat_activity.datname = '" + databaseName + "' " +
                    "AND pid <> pg_backend_pid()";
            statement.execute(terminateConnectionsSql);
            logger.log(Level.INFO, "Active connections to the database {0} have been terminated.", databaseName);
        }
    }
}