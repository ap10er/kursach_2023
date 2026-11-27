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

public class SearchEmployee {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button SearchEmployeeBack;
    @FXML private TextArea SearchEmployeeTextArea;

    @FXML
    void initialize() {
        SearchEmployeeBack.setOnAction(actionEvent -> SearchEmployeeBack.getScene().getWindow().hide());
    }

    public void search(String fullName) {
        try {
            Response resp = ClientContext.client().send(
                    Request.of("SEARCH_EMPLOYEE").put("fullName", fullName));
            if (resp.isOk()) {
                List<String> rows = JsonCodec.parseDataList(resp.getData());
                SearchEmployeeTextArea.setText(rows.isEmpty() ? "Сотрудник не найден" : String.join("", rows));
            } else {
                SearchEmployeeTextArea.setText("Ошибка: " + resp.getError());
            }
        } catch (IOException e) {
            SearchEmployeeTextArea.setText("Ошибка связи с сервером: " + e.getMessage());
        }
    }
}
