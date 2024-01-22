package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    private static Connection connection = null;

    public static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.");
        }

        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/salarysystem?useUnicode=true&serverTimezone=UTC", "root", "13979");

        if (connection == null) {
            throw new SQLException("Failed to establish connection to the database.");
        } else {
            System.out.println("Successfully connected!");
        }

        return connection;
    }

    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Closing connection!\n\n");
            }
        } catch (SQLException e) {
            System.out.println("Failed to close connection!\n\n");
            e.printStackTrace();
        }
    }
}
