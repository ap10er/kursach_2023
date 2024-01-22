package Pages;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static Database.Connector.connect;

public class MainAdminPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button AdminBack;

    @FXML
    private Button AdminCalculateSalary;

    @FXML
    private Button AdminEditInfo;

    @FXML
    private Button AdminExit;

    @FXML
    private Button AdminSearch;

    @FXML
    private Button AdminAddEmployee;

    @FXML
    private Button AdminRemoveEmployee;

    @FXML
    private Button AdminNotifications;

    @FXML
    private Button AdminTickets;

    @FXML
    private Text AdminUsername;

    @FXML
    private Text MainPageName;

    @FXML
    void initialize() {
        setupButtons();
    }

    private void setupButtons() {
        AdminBack.setOnAction(actionEvent -> openLoginPage());
        AdminExit.setOnAction(actionEvent -> Platform.exit());
        AdminEditInfo.setOnAction(actionEvent -> employeesList());
        AdminSearch.setOnAction(actionEvent -> enterInfo());
        AdminAddEmployee.setOnAction(actionEvent -> addEmployee());
        AdminRemoveEmployee.setOnAction(actionEvent -> enterToRemove());
        AdminCalculateSalary.setOnAction(actionEvent -> CalculateSalary());
        AdminNotifications.setOnAction(actionEvent -> sendMessage());
        AdminTickets.setOnAction(actionEvent -> openTickets());
    }

    private void openTickets()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PenaltiesPage.fxml"));
        try {
            Parent root = loader.load();
            openStage(root);
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void sendMessage()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MessagePage.fxml"));
        try {
            Parent root = loader.load();
            openStage(root);
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void CalculateSalary()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CalculateSalaryAdminPage.fxml"));
        try {
            Parent root = loader.load();
            openStage(root);
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void addEmployee() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddEmployee.fxml"));
        try {
            Parent root = loader.load();
            AddEmployee addEmployeeController = loader.getController();
            addEmployeeController.initialize();
            openStage(root);
        } catch (IOException e) {
            handleException(e);
        }
    }

    
    private void enterToRemove()
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удаление сотрудника");
        dialog.setHeaderText("Введите Логин сотрудника");
        dialog.setContentText("Логин:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            try {
                removeEmployee(username);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeEmployee(String fullname) throws SQLException {
        String deleteEmployeeQuery = "DELETE FROM employees WHERE Username = ?";
        String deleteUserQuery = "DELETE FROM users WHERE Username = ?";

        try (Connection connection = connect();
             PreparedStatement deleteEmployeeStatement = connection.prepareStatement(deleteEmployeeQuery);
             PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery)) {

            deleteEmployeeStatement.setString(1, fullname);
            int employeeDeleted = deleteEmployeeStatement.executeUpdate();

            deleteUserStatement.setString(1, fullname);
            deleteUserStatement.executeUpdate();

            if (employeeDeleted > 0) {
                showSuccessDialog("Сотрудник успешно удален!");
            } else {
                showErrorDialog("Сотрудника с таким именем пользователя не найдено!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void enterInfo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск сотрудника");
        dialog.setHeaderText("Введите полное имя сотрудника");
        dialog.setContentText("Полное имя:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::searchEmployee);
    }

    private void openStage(Parent root) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        showErrorDialog();
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText("Ошибка!");
        alert.showAndWait();
    }

    private void searchEmployee(String fullName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchEmployee.fxml"));
        try {
            Parent root = loader.load();

            SearchEmployee searchEmployee = loader.getController();
            searchEmployee.search(fullName);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoggedInUsername(String username) {
        AdminUsername.setText(username);
    }

    private void employeesList() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Employees.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void openLoginPage() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();

            AdminBack.getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


