package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection utility.
 * All DAO classes obtain their connection from here.
 */
public class DBConnection {

    // ── Configure these for your local MySQL setup ──────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/food_order_system"
                                         + "?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "your_mysql_password";   // ← change this
    // ────────────────────────────────────────────────────────────

    private static Connection connection = null;

    /** Returns a shared connection; creates one if not yet open. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connection established.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. "
                        + "Add mysql-connector-j-*.jar to your classpath.", e);
            }
        }
        return connection;
    }

    /** Gracefully close the connection (call on app exit). */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
