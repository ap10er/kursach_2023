package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import client.ClientContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import protocol.Request;
import protocol.Response;

public class UserEditInfoPage {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Text MainPageName;
    @FXML private Button UserEditInfoPageBack;
    @FXML private Button UserEditInfoPageLastname;
    @FXML private Button UserEditInfoPageLogin;
    @FXML private Button UserEditInfoPageMiddlename;
    @FXML private Button UserEditInfoPageName;
    @FXML private Button UserEditInfoPageNumber;
    @FXML private Button UserEditInfoPagePassword;

    private String loggedInUsername;

    @FXML void initialize() {
        UserEditInfoPageBack.setOnAction(e -> UserEditInfoPageBack.getScene().getWindow().hide());
        UserEditInfoPageLastname.setOnAction(e -> changeLastname());
        UserEditInfoPageName.setOnAction(e -> changeName());
        UserEditInfoPageMiddlename.setOnAction(e -> changeMiddlename());
        UserEditInfoPageNumber.setOnAction(e -> changeNumber());
        UserEditInfoPageLogin.setOnAction(e -> changeLogin());
        UserEditInfoPagePassword.setOnAction(e -> changePassword());
    }

    public void setUserInfo(String username) {
        loggedInUsername = username;
    }

    private void changeLogin() {
        showInputDialog("Введите текущий логин:", current -> {
            if (current.equals(loggedInUsername)) {
                showInputDialog("Введите новый логин:", newLogin -> {
                    if (!newLogin.equals(current)) updateLogin(newLogin);
                    else showErrorDialog("Новый логин должен отличаться от текущего.");
                });
            } else showErrorDialog("Текущий логин введен неверно.");
        });
    }

    private void updateLogin(String newLogin) {
        try {
            Response resp = ClientContext.client().send(Request.of("CHANGE_USERNAME")
                    .put("oldUsername", loggedInUsername).put("newUsername", newLogin));
            if (resp.isOk()) {
                loggedInUsername = newLogin;
                showSuccessDialog("Логин успешно обновлен!\nДля обновления информации авторизируйтесь заново!");
            } else showErrorDialog("Ошибка при изменении логина!");
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером.");
        }
    }

    private void changePassword() {
        showInputDialog("Введите текущий пароль:", current -> {
            if (checkPassword(current)) showInputDialog("Введите новый пароль:", this::updatePassword);
            else showErrorDialog("Текущий пароль введен неверно.");
        });
    }

    private boolean checkPassword(String currentPassword) {
        try {
            Response resp = ClientContext.client().send(
                    Request.of("GET_PASSWORD").put("username", loggedInUsername));
            return resp.isOk() && currentPassword.equals(resp.getData());
        } catch (IOException e) {
            return false;
        }
    }

    private void updatePassword(String newPassword) {
        try {
            Response resp = ClientContext.client().send(Request.of("CHANGE_PASSWORD")
                    .put("username", loggedInUsername).put("newPassword", newPassword));
            showSuccessDialog(resp.isOk()
                    ? "Пароль успешно обновлен!\nДля обновления информации авторизируйтесь заново!"
                    : "Ошибка при изменении пароля!");
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером.");
        }
    }

    private void changeNumber() { showInputDialog("Введите новый номер телефона: ", v -> updateField("Number", v, "Номер успешно обновлен!")); }
    private void changeMiddlename() { showInputDialog("Введите новое отчество: ", v -> updateField("Middlename", v, "Отчество успешно обновлено!")); }
    private void changeName() { showInputDialog("Введите новое имя: ", v -> updateField("Firstname", v, "Имя успешно обновлено!")); }
    private void changeLastname() { showInputDialog("Введите новую фамилию: ", v -> updateField("Lastname", v, "Фамилия успешно обновлена!")); }

    private void updateField(String field, String value, String successMessage) {
        try {
            Response resp = ClientContext.client().send(Request.of("UPDATE_EMPLOYEE_FIELD")
                    .put("username", loggedInUsername).put("field", field).put("value", value));
            if (resp.isOk()) showSuccessDialog(successMessage);
            else showErrorDialog(resp.getError());
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером.");
        }
    }

    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText("Ошибка! " + text);
        alert.showAndWait();
    }

    private void showInputDialog(String promptText, Consumer<String> callback) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Изменение данных");
        dialog.setHeaderText(null);
        dialog.setContentText(promptText);
        dialog.showAndWait().ifPresent(callback);
    }
}
