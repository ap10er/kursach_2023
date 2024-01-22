package Database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabaseConnect {

    public static void main(String[] argv) {
        System.out.println("-------- MySQL JDBC Connection Testing ------------");

        try {
            Connection connection = Connector.connect();

        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        } finally {
            Connector.close();
        }
    }
}
