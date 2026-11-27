package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

public class PenaltiesPage {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button TicketPageAddTicket;
    @FXML private Button TicketPageBack;
    @FXML private TextField TicketPageDescription;
    @FXML private Button TicketPageRemoveTicket;
    @FXML private Button TicketPageShowAll;
    @FXML private Button TicketPageShowTickets;
    @FXML private TextField TicketsPageLogin;
    @FXML private TextField TicketsPageTicket;
    @FXML private ImageView TicketsPageSaveToFile;

    @FXML void initialize() {
        TicketPageBack.setOnAction(e -> TicketPageBack.getScene().getWindow().hide());
        TicketPageAddTicket.setOnAction(e -> addTicket());
        TicketPageRemoveTicket.setOnAction(e -> removeTicket());
        TicketPageShowTickets.setOnAction(e -> showTickets());
        TicketPageShowAll.setOnAction(e -> openShowTicketsPageForAll());
        TicketsPageSaveToFile.setOnMouseClicked(e -> saveTicketsToFile());
    }

    private void saveTicketsToFile() {
        try {
            Response resp = ClientContext.client().send("GET_ALL_PENALTIES");
            if (!resp.isOk()) { showAlert(resp.getError(), Alert.AlertType.ERROR); return; }
            Path path = Path.of("src/main/resources/files/PenaltiesTable.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                for (String row : JsonCodec.parseDataList(resp.getData())) writer.write(row);
            }
        } catch (IOException e) {
            showAlert("Ошибка сохранения: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openShowTicketsPageForAll() {
        openShowTicketsPage("");
    }

    private void showTickets() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск штрафов");
        dialog.setHeaderText("Введите логин сотрудника");
        dialog.showAndWait().ifPresent(this::openShowTicketsPage);
    }

    private void openShowTicketsPage(String login) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ShowPenaltiesPage.fxml"));
            Parent root = loader.load();
            ShowPenaltiesPage page = loader.getController();
            if (login == null || login.isBlank()) page.showAllTickets();
            else page.setUsername(login);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Ошибка открытия страницы: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean hasPenaltiesForUser(String login) {
        try {
            Response resp = ClientContext.client().send(
                    Request.of("PENALTY_EXISTS").put("username", login));
            return resp.isOk() && Boolean.parseBoolean(resp.getData());
        } catch (IOException e) {
            return false;
        }
    }

    private void removeTicket() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удаление штрафа");
        dialog.setContentText("Логин:");
        dialog.showAndWait().ifPresent(login -> {
            try {
                Response resp = ClientContext.client().send(
                        Request.of("DELETE_PENALTIES").put("username", login));
                showAlert(resp.isOk() ? "Штраф успешно удален" : resp.getError(),
                        resp.isOk() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            } catch (IOException e) {
                showAlert("Ошибка связи с сервером.", Alert.AlertType.ERROR);
            }
        });
    }

    private void addTicket() {
        String login = TicketsPageLogin.getText();
        String amount = TicketsPageTicket.getText();
        String description = TicketPageDescription.getText();
        try {
            new BigDecimal(amount.replace(',', '.'));
        } catch (NumberFormatException e) {
            showAlert("Некорректная сумма штрафа", Alert.AlertType.ERROR);
            return;
        }
        try {
            Response resp = ClientContext.client().send(Request.of("ADD_PENALTY")
                    .put("username", login).put("amount", amount.replace(',', '.'))
                    .put("description", description));
            showAlert(resp.isOk() ? "Штраф успешно добавлен" : resp.getError(),
                    resp.isOk() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            if (resp.isOk()) {
                TicketsPageLogin.clear();
                TicketsPageTicket.clear();
                TicketPageDescription.clear();
            }
        } catch (IOException e) {
            showAlert("Ошибка связи с сервером.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        new Alert(alertType, message, ButtonType.OK).showAndWait();
    }
}
