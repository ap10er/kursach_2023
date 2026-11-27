package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class Employees {

    @FXML private Button EmployeesBack;
    @FXML private ImageView EmployeesSaveToFile;
    @FXML private TextArea EmployeesTextArea;

    private String employeesText = "";

    @FXML
    void initialize() {
        try {
            Response resp = ClientContext.client().send("GET_EMPLOYEES");
            if (resp.isOk()) {
                List<String> rows = JsonCodec.parseDataList(resp.getData());
                employeesText = String.join("", rows);
                EmployeesTextArea.setText(employeesText);
            } else {
                EmployeesTextArea.setText("Ошибка: " + resp.getError());
            }
        } catch (IOException e) {
            EmployeesTextArea.setText("Ошибка связи с сервером: " + e.getMessage());
        }

        EmployeesBack.setOnAction(actionEvent -> EmployeesBack.getScene().getWindow().hide());
        EmployeesSaveToFile.setOnMouseClicked(mouseEvent -> saveEmployeesToFile());
    }

    private void saveEmployeesToFile() {
        Path path = Path.of("src/main/resources/files/EmployeesTable.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(employeesText);
            System.out.println("Данные сохранены в " + path);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}
