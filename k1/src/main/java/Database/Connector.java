package Database;

import Config.EnvConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    private static Connection connection = null;
    private static boolean initialized = false;

    public static String getUser() {
        return EnvConfig.required("DEFAULT_USER");
    }

    public static String getPassword() {
        return EnvConfig.required("DEFAULT_PASSWORD");
    }

    public static String getUrl() {
        return EnvConfig.required("DEFAULT_URL");
    }

    public static String buildUrl(String databaseName) {
        String url = getUrl();
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash == -1) {
            return url + "/" + databaseName;
        }
        return url.substring(0, lastSlash + 1) + databaseName;
    }

    public static synchronized Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        if (!initialized) {
            DatabaseInitializer.ensureDatabase();
            initialized = true;
        }

        if (connection == null || connection.isClosed()) {
            // Работаем с целевой БД salarysystem, а не со служебной postgres
            connection = DriverManager.getConnection(
                    buildUrl(DatabaseInitializer.getDatabaseName()), getUser(), getPassword());
        }

        return connection;
    }

    public static synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }
}
