package Pages;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import Database.Connector;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddEmployee {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button AddEmployeeBack;

    @FXML
    private Button AddEmployeeAdd;

    @FXML
    private TextField AddEmployeeLastName;

    @FXML
    private TextField AddEmployeeFirstName;

    @FXML
    private TextField AddEmployeeMiddleName;

    @FXML
    private TextField AddEmployeeNumber;

    @FXML
    private TextField AddEmployeePosition;

    @FXML
    private TextField AddEmployeeSalary;

    @FXML
    private DatePicker AddEmployeeHireDate;

    @FXML
    private TextField AddEmployeeUsername;

    @FXML
    private TextField AddEmployeeDepartmentName;

    @FXML
    void initialize() {
        setupButtons();
    }

    private void setupButtons() {
        AddEmployeeBack.setOnAction(actionEvent -> closeStage());
        AddEmployeeAdd.setOnAction(actionEvent -> {
            try {
                addEmployee();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void closeStage() {
        Stage stage = (Stage) AddEmployeeBack.getScene().getWindow();
        stage.close();
    }

    private void addEmployee() throws SQLException {
        String lastName = AddEmployeeLastName.getText();
        String firstName = AddEmployeeFirstName.getText();
        String middleName = AddEmployeeMiddleName.getText();
        String number = AddEmployeeNumber.getText();
        String position = AddEmployeePosition.getText();
        String salary = AddEmployeeSalary.getText();
        String hireDate = AddEmployeeHireDate.getValue().toString();
        String departmentName = AddEmployeeDepartmentName.getText();

        try {
            Connection connection = Connector.connect();
            String insertQuery = "INSERT INTO Employees (Lastname, Firstname, Middlename, Number, Position, Salary, HireDate, DepartmentName) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                insertStatement.setString(1, lastName);
                insertStatement.setString(2, firstName);
                insertStatement.setString(3, middleName);
                insertStatement.setString(4, number);
                insertStatement.setString(5, position);
                insertStatement.setString(6, salary);
                insertStatement.setString(7, hireDate);
                insertStatement.setString(8, departmentName);

                insertStatement.executeUpdate();
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText(null);
            alert.setContentText("Сотрудник успешно добавлен!");
            alert.showAndWait();

            clearFields();

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

    private void clearFields() {
        AddEmployeeLastName.clear();
        AddEmployeeFirstName.clear();
        AddEmployeeMiddleName.clear();
        AddEmployeeNumber.clear();
        AddEmployeePosition.clear();
        AddEmployeeSalary.clear();
        AddEmployeeHireDate.getEditor().clear();  // Clear the DatePicker text
        AddEmployeeDepartmentName.clear();
    }
}
