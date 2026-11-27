package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class UserNotifications {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button UserNotificationsBack;
    @FXML private Button UserNotificationsDelete;
    @FXML private TextField UserNotificationsLogin;
    @FXML private TextField UserNotificationsSender;
    @FXML private Button UserNotificationsSend;
    @FXML private TextArea UserNotificationsTextField;

    private String loggedInUsername;

    @FXML
    void initialize() {
        UserNotificationsBack.setOnAction(e -> UserNotificationsBack.getScene().getWindow().hide());
        UserNotificationsSend.setOnAction(e -> sendNotification());
        UserNotificationsDelete.setOnAction(e -> deleteNotifications());
    }

    private void sendNotification() {
        String receiver = UserNotificationsLogin.getText();
        String sender = UserNotificationsSender.getText();
        String text = UserNotificationsTextField.getText();
        if (receiver == null || receiver.isBlank() || text == null || text.isBlank()) {
            showErrorDialog("Заполните получателя и текст сообщения");
            return;
        }
        try {
            Response resp = ClientContext.client().send(Request.of("SEND_MESSAGE")
                    .put("receiver", receiver).put("sender", sender).put("message", text));
            if (resp.isOk()) {
                UserNotificationsTextField.clear();
                displayMessages();
            } else {
                showErrorDialog(resp.getError());
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    private void deleteNotifications() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) return;
        try {
            Response resp = ClientContext.client().send(
                    Request.of("DELETE_MESSAGES").put("username", loggedInUsername));
            if (resp.isOk()) displayMessages();
            else showErrorDialog(resp.getError());
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    public void displayMessages() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) return;
        try {
            Response resp = ClientContext.client().send(
                    Request.of("GET_MESSAGES").put("username", loggedInUsername));
            if (resp.isOk()) {
                List<String> rows = JsonCodec.parseDataList(resp.getData());
                UserNotificationsTextField.setText(String.join("", rows));
            } else {
                showErrorDialog(resp.getError());
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    public void setLoggedInUsername(String username) {
        loggedInUsername = username;
        displayMessages();
    }

    private void showErrorDialog(String errorMessage) {
        new Alert(Alert.AlertType.ERROR, errorMessage).showAndWait();
    }
}
