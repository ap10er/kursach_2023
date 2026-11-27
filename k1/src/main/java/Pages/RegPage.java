package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientContext;
import protocol.Request;
import protocol.Response;
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
        RegPageReg.setOnAction(actionEvent -> registerUser());

    }

    private String determineRole(String departmentName) {
        if ("it".equalsIgnoreCase(departmentName)) {
            return "Admin";
        } else {
            return "User";
        }
    }

    private void registerUser() {
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

        try {
            // Сначала узнаём отдел сотрудника, чтобы сервер определил роль
            Request find = Request.of("SEARCH_EMPLOYEE")
                    .put("fullName", lastname + " " + firstname + " " + middlename);
            Response found = ClientContext.client().send(find);
            if (!found.isOk()) {
                showErrorDialog("Пользователь с такими ФИО не найден в таблице Employees");
                return;
            }
            String department = "";
            java.util.List<String> rows = protocol.JsonCodec.parseDataList(found.getData());
            for (String row : rows) {
                if (row.contains("Отдел:")) {
                    department = row.substring(row.indexOf("Отдел:") + 6).trim();
                    break;
                }
            }

            Request req = Request.of("REGISTER")
                    .put("login", login).put("password", password)
                    .put("lastname", lastname).put("firstname", firstname)
                    .put("middlename", middlename).put("department", department);
            Response resp = ClientContext.client().send(req);
            if (resp.isOk()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.setContentText(resp.getData());
                alert.showAndWait();
                RegPageLastName.clear();
                RegPageName.clear();
                RegPageNameMiddleName.clear();
                RegPageLogin.clear();
                RegPagePassword.clear();
                RegPagePasswordAccept.clear();
                openLoginPage();
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


