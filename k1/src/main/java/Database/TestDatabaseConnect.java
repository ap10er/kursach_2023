package Database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnect {

    public static void main(String[] argv) {
        System.out.println("-------- PostgreSQL JDBC Connection Testing ------------");
        System.out.println("URL:      " + Connector.buildUrl("salarysystem"));
        System.out.println("User:     " + Connector.getUser());
        System.out.println("Password: " + mask(Connector.getPassword()));
        System.out.println();

        try (Connection connection = Connector.connect()) {
            if (connection != null) {
                System.out.println("Connection established: " + connection.getMetaData().getURL());
            }
        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
            System.out.println();
            System.out.println("Если ошибка связана с аутентификацией, передайте правильный пароль:");
            System.out.println("  -DDB_PASSWORD=ваш_пароль  (в VM options конфигурации запуска)");
            System.out.println("или задайте переменную окружения DB_PASSWORD.");
        } finally {
            Connector.close();
        }
    }

    private static String mask(String password) {
        if (password == null || password.isEmpty()) {
            return "<пустой>";
        }
        return "*".repeat(password.length());
    }
}
