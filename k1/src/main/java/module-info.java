module com.main.k {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens Pages to javafx.fxml;
    exports client;
    exports Pages;
}