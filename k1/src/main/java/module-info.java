module com.main.k {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;          // только серверная часть использует JDBC
    requires org.postgresql.jdbc;

    opens Pages to javafx.fxml;
    exports client;
    exports Pages;
    exports Config;
    exports Database;
    exports protocol;
    exports server;
    exports server.services;
}
