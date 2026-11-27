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
import javafx.scene.text.Text;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class ShowPenaltiesPage {
    private String username;

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button ShowTicketsBack;
    @FXML private TextArea ShowTicketsTextArea;
    @FXML private Text ShowTicketsUsername;

    @FXML void initialize() {
        ShowTicketsBack.setOnAction(e -> ShowTicketsBack.getScene().getWindow().hide());
    }

    public void setUsername(String username) {
        this.username = username;
        ShowTicketsUsername.setText("Штрафы пользователя: " + username);
        showTicketsForUser();
    }

    public void showAllTickets() {
        load("GET_ALL_PENALTIES", null);
    }

    private void showTicketsForUser() {
        load("GET_PENALTIES", username);
    }

    private void load(String action, String user) {
        try {
            Request req = Request.of(action);
            if (user != null) req.put("username", user);
            Response resp = ClientContext.client().send(req);
            if (resp.isOk()) {
                List<String> rows = JsonCodec.parseDataList(resp.getData());
                ShowTicketsTextArea.setText(String.join("", rows));
            } else {
                showAlert(resp.getError());
            }
        } catch (IOException e) {
            showAlert("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
