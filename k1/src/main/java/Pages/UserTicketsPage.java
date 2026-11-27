package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class UserTicketsPage {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button UserTicketsPageBack;
    @FXML private TextArea UserTicketsPageTextArea;

    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadTickets();
    }

    private void loadTickets() {
        try {
            Response resp = ClientContext.client().send(
                    Request.of("GET_PENALTIES").put("username", loggedInUsername));
            if (resp.isOk()) {
                List<String> rows = JsonCodec.parseDataList(resp.getData());
                UserTicketsPageTextArea.setText(String.join("", rows));
            } else {
                UserTicketsPageTextArea.setText("Ошибка: " + resp.getError());
            }
        } catch (IOException e) {
            UserTicketsPageTextArea.setText("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    @FXML void initialize() {
        UserTicketsPageBack.setOnAction(e -> UserTicketsPageBack.getScene().getWindow().hide());
    }
}
