package com.pahanaedu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DBConnection {
    // Database configuration - consider moving these to a config file
    private static final String URL = "jdbc:mysql://localhost:3306/pahana_edu";
    private static final String USER = "root";
    private static final String PASSWORD = "Sally(2000)";

    // Static initializer to load the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }

    // Private constructor to prevent instantiation
    private DBConnection() {}

    /**
     * Gets a database connection with auto-reconnect and validation
     * @return A valid database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            // Add connection properties for better reliability
            String connectionUrl = URL + "?useSSL=false" +
                    "&autoReconnect=true" +
                    "&failOverReadOnly=false" +
                    "&maxReconnects=10" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF-8";

            conn = DriverManager.getConnection(connectionUrl, USER, PASSWORD);

            // Validate the connection
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Failed to establish database connection");
            }

            return conn;
        } catch (SQLException e) {
            // Close connection if it was partially opened
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException suppressed) {
                    e.addSuppressed(suppressed);
                }
            }
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to quietly close a connection
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (Objects.nonNull(connection)) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Validates if a connection is still active
     * @param connection The connection to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidConnection(Connection connection) {
        if (Objects.isNull(connection)) {
            return false;
        }

        try {
            return !connection.isClosed() && connection.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            return false;
        }
    }
}