package persistence;

import util.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection = null;
    private static int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    private DBConnection() {}

    // get connection (retry if fails)
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName(Constants.DB_DRIVER);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("JDBC driver not found", e);
                }

                connection = DriverManager.getConnection(
                        Constants.DB_URL,
                        Constants.DB_USER,
                        Constants.DB_PASS
                );

                reconnectAttempts = 0;
                System.out.println("Database connected");
            }
            return connection;

        } catch (SQLException e) {
            reconnectAttempts++;
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                System.err.println("Retrying connection... (" + reconnectAttempts + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return getConnection();
            } else {
                reconnectAttempts = 0;
                throw new SQLException("Failed after " + MAX_RECONNECT_ATTEMPTS + " tries", e);
            }
        }
    }

    // close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    // test connection
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
