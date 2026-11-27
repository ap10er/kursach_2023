package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import client.ClientContext;
import protocol.Request;
import protocol.Response;
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
            } catch (RuntimeException e) {
                throw e;
            }
        });
    }

    private void closeStage() {
        Stage stage = (Stage) AddEmployeeBack.getScene().getWindow();
        stage.close();
    }

    private void addEmployee() {
        String lastName = AddEmployeeLastName.getText();
        String firstName = AddEmployeeFirstName.getText();
        String middleName = AddEmployeeMiddleName.getText();
        String number = AddEmployeeNumber.getText();
        String position = AddEmployeePosition.getText();
        String salaryText = AddEmployeeSalary.getText();
        String hireDate = AddEmployeeHireDate.getValue().toString();
        String departmentName = AddEmployeeDepartmentName.getText();

        // PostgreSQL не преобразует varchar в double precision автоматически.
        double salary;
        java.sql.Date sqlHireDate;
        try {
            salary = Double.parseDouble(salaryText.replace(',', '.'));
            sqlHireDate = java.sql.Date.valueOf(hireDate);
        } catch (IllegalArgumentException e) {
            showErrorDialog("Некорректный формат зарплаты или даты найма.");
            return;
        }

        try {
            Request req = Request.of("ADD_EMPLOYEE")
                    .put("lastName", lastName).put("firstName", firstName)
                    .put("middleName", middleName).put("number", number)
                    .put("position", position).put("salary", String.valueOf(salary))
                    .put("hireDate", sqlHireDate.toString()).put("department", departmentName);
            Response resp = ClientContext.client().send(req);
            if (resp.isOk()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.setContentText(resp.getData());
                alert.showAndWait();
                clearFields();
            } else {
                showErrorDialog(resp.getError());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка связи с сервером.");
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
