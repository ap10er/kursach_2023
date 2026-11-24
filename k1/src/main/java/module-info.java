module com.main.k {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens Pages to javafx.fxml;
    exports client;
    exports Pages;
}