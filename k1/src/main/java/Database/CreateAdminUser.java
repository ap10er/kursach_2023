package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Утилита для создания администратора в базе данных.
 *
 * Логин:   admin
 * Пароль:  admin
 * Роль:    Admin (именно это значение ожидает LoginPage для входа в меню админа)
 *
 * Запуск: mvn exec:java -Dexec.mainClass=Database.CreateAdminUser -DDB_PASSWORD=ваш_пароль
 */
public class CreateAdminUser {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String ROLE = "Admin";

    public static void main(String[] args) {
        try (Connection connection = Connector.connect()) {
            if (exists(connection, USERNAME)) {
                // Если пользователь уже есть — обновляем роль и пароль до admin/Admin.
                String update = "UPDATE users SET Password = ?, Role = ? WHERE Username = ?";
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, PASSWORD);
                    statement.setString(2, ROLE);
                    statement.setString(3, USERNAME);
                    statement.executeUpdate();
                }
                System.out.println("Пользователь '" + USERNAME + "' обновлён: password=" + PASSWORD + ", role=" + ROLE);
            } else {
                String insert = "INSERT INTO users (Username, Password, Role) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setString(1, USERNAME);
                    statement.setString(2, PASSWORD);
                    statement.setString(3, ROLE);
                    statement.executeUpdate();
                }
                System.out.println("Пользователь '" + USERNAME + "' создан: password=" + PASSWORD + ", role=" + ROLE);
            }

            // Проверяем, что по логину/паролю действительно находится роль Admin.
            String verify = "SELECT Role FROM users WHERE Username = ? AND Password = ?";
            try (PreparedStatement statement = connection.prepareStatement(verify)) {
                statement.setString(1, USERNAME);
                statement.setString(2, PASSWORD);
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
