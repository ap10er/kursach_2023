package Database;

import Config.EnvConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public final class DatabaseInitializer {

    private static final String DB_NAME = "salarysystem";

    private DatabaseInitializer() {
    }

    public static void ensureDatabase() throws SQLException {
        if (!databaseExists()) {
            createDatabase();
        }
        createTables();
        createAdminUser();
    }


    private static boolean databaseExists() throws SQLException {
        String adminUrl = Connector.buildUrl("salarysystem");
        try (Connection adminConnection = DriverManager.getConnection(
                adminUrl, Connector.getUser(), Connector.getPassword());
             Statement statement = adminConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'")) {
            return resultSet.next();
        }
    }

    /**
     * Создаёт базу данных {@code salarysystem}.
     */
    private static void createDatabase() throws SQLException {
        String adminUrl = Connector.buildUrl("salarysystem");
        try (Connection adminConnection = DriverManager.getConnection(
                adminUrl, Connector.getUser(), Connector.getPassword());
             Statement statement = adminConnection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + DB_NAME + " ENCODING 'UTF8'");
            System.out.println("Database '" + DB_NAME + "' created.");
        }
    }

    private static void createTables() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                Connector.buildUrl(DB_NAME), Connector.getUser(), Connector.getPassword());
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        userid   SERIAL PRIMARY KEY,
                        username VARCHAR(100) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        role     VARCHAR(50)  NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS employees (
                        employeeid     SERIAL PRIMARY KEY,
                        lastname       VARCHAR(100),
                        firstname      VARCHAR(100),
                        middlename     VARCHAR(100),
                        number         VARCHAR(50),
                        position       VARCHAR(100),
                        salary         DECIMAL,
                        hiredate       DATE,
                        username       VARCHAR(100),
                        departmentname VARCHAR(100)
                    )
                    """);

            // Должности
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS positions (
                        positionid   SERIAL PRIMARY KEY,
                        positionname VARCHAR(100) NOT NULL UNIQUE
                    )
                    """);

            // Начисления зарплаты
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS salaries (
                        salaryid      SERIAL PRIMARY KEY,
                        positionname  VARCHAR(100),
                        baserate      DOUBLE PRECISION,
                        bonus         DOUBLE PRECISION,
                        workedhours   INTEGER,
                        rateperhour   DOUBLE PRECISION,
                        effectivedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        result        DOUBLE PRECISION
                    )
                    """);

            // Штрафы
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS penalties (
                        penaltyid   SERIAL PRIMARY KEY,
                        username    VARCHAR(100),
                        fineamount  NUMERIC(12, 2),
                        penaltydate DATE DEFAULT CURRENT_DATE,
                        description TEXT
                    )
                    """);

            // Сообщения / уведомления
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS messages (
                        messageid SERIAL PRIMARY KEY,
                        sender    VARCHAR(100),
                        username  VARCHAR(100),
                        message   TEXT
                    )
                    """);

            System.out.println("Database schema is ready.");
        }
    }

    private static void createAdminUser() throws SQLException {
        String adminUsername = EnvConfig.required("ADMIN_USERNAME");
        String adminPassword = EnvConfig.required("ADMIN_PASSWORD");
        String adminRole = EnvConfig.required("ADMIN_ROLE");
        try (Connection connection = DriverManager.getConnection(
                Connector.buildUrl(DB_NAME), Connector.getUser(), Connector.getPassword());
             Statement statement = connection.createStatement()) {

            ResultSet check = statement.executeQuery(
                    "SELECT 1 FROM users WHERE username = '" + adminUsername + "'");
            if (!check.next()) {
                statement.executeUpdate(
                        "INSERT INTO users (username, password, role) VALUES ('"
                                + adminUsername + "', '" + adminPassword + "', '" + adminRole + "')");
            } else {
                statement.executeUpdate(
                        "UPDATE users SET password = '" + adminPassword
                                + "', role = '" + adminRole + "' WHERE username = '" + adminUsername + "'");
            }
            check.close();
        }
    }
}
