package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.sql.*;

import Database.Connector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button RegPageBack;

    @FXML
    private TextField RegPageLastName;

    @FXML
    private TextField RegPageLogin;

    @FXML
    private TextField RegPageName;

    @FXML
    private TextField RegPageNameMiddleName;

    @FXML
    private TextField RegPageNumber;

    @FXML
    private PasswordField RegPagePassword;

    @FXML
    private PasswordField RegPagePasswordAccept;

    @FXML
    private Button RegPageReg;

    @FXML
    void initialize() {

        RegPageBack.setOnAction(actionEvent -> openLoginPage());
        RegPageReg.setOnAction(actionEvent -> {
            try {
                registerUser();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private String determineRole(String departmentName) {
        if ("it".equalsIgnoreCase(departmentName)) {
            return "Admin";
        } else {
            return "User";
        }
    }

    //cчитывание данных
    private void registerUser() throws SQLException {

        String login = RegPageLogin.getText();
        String password = RegPagePassword.getText();
        String passwordAccept = RegPagePasswordAccept.getText();
        String lastname = RegPageLastName.getText();
        String firstname = RegPageName.getText();
        String middlename = RegPageNameMiddleName.getText();

        if (!password.equals(passwordAccept)) {
            showErrorDialog("Пароли не совпадают");
            return;
        }

        //проверка на дубликат логина
        try {
            Connection connection = Connector.connect();

            // Проверка наличия пользователя в таблице Employees и получение DepartmentName
            String selectEmployeeQuery = "SELECT * FROM Employees WHERE Lastname = ? AND Firstname = ? AND Middlename = ?";
            try (PreparedStatement selectEmployeeStatement = connection.prepareStatement(selectEmployeeQuery)) {
                selectEmployeeStatement.setString(1, lastname);
                selectEmployeeStatement.setString(2, firstname);
                selectEmployeeStatement.setString(3, middlename);

                try (ResultSet employeeResultSet = selectEmployeeStatement.executeQuery()) {
                    if (employeeResultSet.next()) {

                        String departmentName = employeeResultSet.getString("DepartmentName");
                        String existingUsername = employeeResultSet.getString("Username");
                        if (existingUsername != null) {
                            showErrorDialog("Этот пользователь уже зарегистрирован");
                            RegPageLastName.clear();
                            RegPageName.clear();
                            RegPageNameMiddleName.clear();
                            RegPageLogin.clear();
                            RegPagePassword.clear();
                            RegPagePasswordAccept.clear();
                            RegPageNumber.clear();
                            return;
                        }

                        // Определение роли в зависимости от DepartmentName
                        String role = determineRole(departmentName);  // Предполагаю, что вы проверяете только "it"

                        // Ваш код для создания пользователя в таблице users
                        String insertUserQuery = "INSERT INTO users (Username, Password, Role) VALUES (?, ?, ?)";
                        try (PreparedStatement insertUserStatement = connection.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertUserStatement.setString(1, login);
                            insertUserStatement.setString(2, password);
                            insertUserStatement.setString(3, role);

                            insertUserStatement.executeUpdate();
                        }

                        // Обновление записи в таблице Employees с сохранением Username
                        String updateEmployeeQuery = "UPDATE Employees SET Username = ? WHERE Lastname = ? AND Firstname = ? AND Middlename = ?";
                        try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeQuery)) {
                            updateEmployeeStatement.setString(1, login);
                            updateEmployeeStatement.setString(2, lastname);
                            updateEmployeeStatement.setString(3, firstname);
                            updateEmployeeStatement.setString(4, middlename);

                            updateEmployeeStatement.executeUpdate();
                        }

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("");
                        alert.setHeaderText(null);
                        alert.setContentText("Регистрация прошла успешно!");
                        alert.showAndWait();

                        RegPageLastName.clear();
                        RegPageName.clear();
                        RegPageNameMiddleName.clear();
                        RegPageLogin.clear();
                        RegPagePassword.clear();
                        RegPagePasswordAccept.clear();

                        openLoginPage();
                    } else {
                        showErrorDialog("Пользователь с такими ФИО не найден в таблице Employees");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при работе с базой данных.");
        } finally {
            Connector.close();
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openLoginPage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();

            RegPageBack.getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


