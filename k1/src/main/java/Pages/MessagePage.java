package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class MessagePage {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button MessagePageBack;
    @FXML private Button MessagePageDelete;
    @FXML private Button MessagePageShow;
    @FXML private TextField MessagePageLogin;
    @FXML private Button MessagePageSend;
    @FXML private TextArea MessagePageTextField;
    @FXML private TextField MessagePageSender;
    @FXML private ImageView MessagePageSaveToFile;

    @FXML void initialize() {
        MessagePageBack.setOnAction(e -> MessagePageBack.getScene().getWindow().hide());
        MessagePageSend.setOnAction(e -> sendMessage());
        MessagePageDelete.setOnAction(e -> deleteMessage());
        MessagePageShow.setOnAction(e -> showMessage());
        MessagePageSaveToFile.setOnMouseClicked(e -> saveMessagesToFile());
    }

    private void deleteMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удаление сообщений");
        dialog.setContentText("Логин:");
        dialog.showAndWait().ifPresent(login -> {
            try {
                Response resp = ClientContext.client().send(
                        Request.of("DELETE_MESSAGES").put("username", login));
                showAlert(resp.isOk() ? resp.getData() : resp.getError(),
                        resp.isOk() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            } catch (IOException e) {
                showAlert("Ошибка связи с сервером.", Alert.AlertType.ERROR);
            }
        });
    }

    private void sendMessage() {
        String login = MessagePageLogin.getText();
        String sender = MessagePageSender.getText();
        String message = MessagePageTextField.getText();
        try {
            Response resp = ClientContext.client().send(Request.of("SEND_MESSAGE")
                    .put("receiver", login).put("sender", sender).put("message", message));
            showAlert(resp.isOk() ? "Сообщение отправлено" : resp.getError(),
                    resp.isOk() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            if (resp.isOk()) MessagePageTextField.clear();
        } catch (IOException e) {
            showAlert("Ошибка связи с сервером.", Alert.AlertType.ERROR);
        }
    }

    private void saveMessagesToFile() {
        try {
            Response resp = ClientContext.client().send("GET_ALL_MESSAGES");
            if (!resp.isOk()) { showAlert(resp.getError(), Alert.AlertType.ERROR); return; }
            Path path = Path.of("src/main/resources/files/MessagesTable.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                for (String row : JsonCodec.parseDataList(resp.getData())) writer.write(row);
            }
        } catch (IOException e) {
            showAlert("Ошибка сохранения: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Просмотр сообщений");
        dialog.setContentText("Логин:");
        dialog.showAndWait().ifPresent(login -> {
            try {
                Response resp = ClientContext.client().send(
                        Request.of("GET_MESSAGES").put("username", login));
                if (resp.isOk()) {
                    List<String> rows = JsonCodec.parseDataList(resp.getData());
                    MessagePageTextField.setText(String.join("", rows));
                } else {
                    showAlert(resp.getError(), Alert.AlertType.ERROR);
                }
            } catch (IOException e) {
                showAlert("Ошибка связи с сервером.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
