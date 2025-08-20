package com.pahanaedu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());

    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/pahana_edu";
    private static final String USER = "root";
    private static final String PASSWORD = "Sally(2000)";
    private static final int CONNECTION_TIMEOUT = 5; // seconds

    // Static initializer to load the JDBC driver
    static {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver successfully loaded");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load MySQL JDBC driver", e);
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }

    private DBConnection() {
        throw new AssertionError("DBConnection should not be instantiated");
    }

    /**
     * Gets a database connection with proper configuration
     * @return A valid database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            // Configure connection properties
            String connectionUrl = URL +
                    "?useSSL=false" +
                    "&autoReconnect=true" +
                    "&failOverReadOnly=false" +
                    "&maxReconnects=10" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF-8" +
                    "&serverTimezone=UTC" +  // Important for timestamp handling
                    "&connectTimeout=" + (CONNECTION_TIMEOUT * 1000);

            logger.log(Level.INFO, "Attempting to connect to database...");
            conn = DriverManager.getConnection(connectionUrl, USER, PASSWORD);

            // Additional validation
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Failed to establish database connection");
            }

            logger.log(Level.INFO, "Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed", e);

            // Close connection if it was partially opened
            closeConnectionQuietly(conn);

            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }


    public static void closeConnectionQuietly(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.log(Level.FINE, "Database connection closed successfully");
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }

    public static boolean isValidConnection(Connection connection) {
        if (connection == null) {
            return false;
        }

        try {
            return !connection.isClosed() && connection.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Connection validation failed", e);
            return false;
        }
    }

    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return isValidConnection(conn);
        } catch (SQLException e) {
            return false;
        } finally {
            closeConnectionQuietly(conn);
        }
    }
}