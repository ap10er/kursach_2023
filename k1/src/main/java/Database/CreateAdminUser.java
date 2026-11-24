package Database;

import Config.EnvConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateAdminUser {

    public static void main(String[] args) {
        String username = EnvConfig.required("ADMIN_USERNAME");
        String password = EnvConfig.required("ADMIN_PASSWORD");
        String role = EnvConfig.required("ADMIN_ROLE");
        try (Connection connection = Connector.connect()) {
            if (exists(connection, username)) {
                String update = "UPDATE users SET Password = ?, Role = ? WHERE Username = ?";
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, password);
                    statement.setString(2, role);
                    statement.setString(3, username);
                    statement.executeUpdate();
                }
            } else {
                String insert = "INSERT INTO users (Username, Password, Role) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setString(1, username);
                    statement.setString(2, password);
                    statement.setString(3, role);
                    statement.executeUpdate();
                }
            }

            String verify = "SELECT Role FROM users WHERE Username = ? AND Password = ?";
            try (PreparedStatement statement = connection.prepareStatement(verify)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Проверка логина: роль = " + rs.getString("Role"));
                    } else {
                        System.out.println("Проверка логина НЕ прошла!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Connector.close();
        }
    }

    private static boolean exists(Connection connection, String username) throws SQLException {
        String query = "SELECT 1 FROM users WHERE Username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
