package Pages;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import Database.Connector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class SearchEmployee {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button SearchEmployeeBack;

    @FXML
    private TextArea SearchEmployeeTextArea;

    @FXML
    void initialize() {
        SearchEmployeeBack.setOnAction(actionEvent -> SearchEmployeeBack.getScene().getWindow().hide());
    }

    public void search(String fullName) {
        try {
            Connection connection = Connector.connect();
            String query = "SELECT * FROM Employees WHERE CONCAT(Lastname, ' ', Firstname, ' ', Middlename) = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, fullName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    updateUIWithEmployeeInfo(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Лучше создать объект Alert и уведомить пользователя
        } finally {
            Connector.close();
        }
    }

    private void updateUIWithEmployeeInfo(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            String employeeInfo = "Номер сотрудника: " + resultSet.getString("EmployeeID") + "\n" +
                    "Фамилия: " + resultSet.getString("Lastname") + "\n" +
                    "Имя: " + resultSet.getString("Firstname") + "\n" +
                    "Отчество: " + resultSet.getString("Middlename") + "\n" +
                    "Должность: " + resultSet.getString("Position") + "\n" +
                    "Номер телефона: " + resultSet.getString("Number") + "\n" +
                    "Зарплата: " + resultSet.getString("Salary") + "\n" +
                    "Дата начала работы: \n" + resultSet.getString("HireDate") + "\n" +
                    "Логин: " + resultSet.getString("Username") + "\n" +
                    "Отдел: " + resultSet.getString("DepartmentName");

            SearchEmployeeTextArea.setText(employeeInfo);
        } else {
            SearchEmployeeTextArea.setText("Сотрудник не найден");
        }
    }
}

