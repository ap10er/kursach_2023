package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для подключения к базе данных PostgreSQL.
 *
 * Параметры подключения можно переопределить через переменные окружения:
 *   DB_URL      - JDBC URL (по умолчанию jdbc:postgresql://localhost:5433/salarysystem)
 *   DB_USER     - имя пользователя (по умолчанию postgres)
 *   DB_PASSWORD - пароль (по умолчанию postgres)
 *
 * При первом подключении автоматически вызывается {@link DatabaseInitializer},
 * который создаёт базу и таблицы, если они отсутствуют.
 */
public class Connector {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5433/salarysystem";
    private static final String DEFAULT_USER = "postgres";
    // Пароль НЕ хардкодим: задаётся переменной окружения DB_PASSWORD
    // или системным свойством -DDB_PASSWORD=... при запуске.
    private static final String DEFAULT_PASSWORD = "1212";

    private static final String URL = getEnv("DB_URL", DEFAULT_URL);
    private static final String USER = getEnv("DB_USER", DEFAULT_USER);
    private static final String PASSWORD = getEnv("DB_PASSWORD", DEFAULT_PASSWORD);

    private static Connection connection = null;
    private static boolean initialized = false;

    private static String getEnv(String name, String defaultValue) {
        String value = System.getProperty(name, System.getenv(name));
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Возвращает имя пользователя БД (используется инициализатором). */
    public static String getUser() {
        return USER;
    }

    /** Возвращает пароль БД (используется инициализатором). */
    public static String getPassword() {
        return PASSWORD;
    }

    /**
     * Строит JDBC URL для указанной базы, сохраняя хост, порт и параметры
     * из основного URL. Например, из {@code jdbc:postgresql://localhost:5433/salarysystem}
     * получается {@code jdbc:postgresql://localhost:5433/postgres}.
     */
    public static String buildUrl(String databaseName) {
        int lastSlash = URL.lastIndexOf('/');
        if (lastSlash == -1) {
            return URL + "/" + databaseName;
        }
        return URL.substring(0, lastSlash + 1) + databaseName;
    }

    /**
     * Возвращает активное соединение с PostgreSQL.
     * Если база или таблицы отсутствуют — создаёт их автоматически.
     */
    public static synchronized Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your PostgreSQL JDBC Driver?");
            e.printStackTrace();
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        if (!initialized) {
            DatabaseInitializer.ensureDatabase();
            initialized = true;
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Successfully connected to PostgreSQL!");
        }

        return connection;
    }

    /**
     * Закрывает текущее соединение с базой данных.
     */
    public static synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Closing connection!\n\n");
            }
        } catch (SQLException e) {
            System.out.println("Failed to close connection!\n\n");
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }
}
