package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class CalculateSalaryAdminPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button SalaryPageBack;

    @FXML
    private TextField SalaryPageBase;

    @FXML
    private TextField SalaryPageBonus;

    @FXML
    private Button SalaryPageCalculateButton;

    @FXML
    private Button SalaryAdminPageAdd;

    @FXML
    private TextField SalaryPageHours;

    @FXML
    private TextField SalaryPageRatePerHour;

    @FXML
    private TextField CalculateSalaryAdminPagePositionName;

    @FXML
    private TextField SalaryPageTickets;

    @FXML
    private ImageView SaveTable;

    @FXML
    void initialize() {
        SalaryPageBack.setOnAction(actionEvent -> SalaryPageBack.getScene().getWindow().hide());
        SalaryPageCalculateButton.setOnAction(actionEvent -> calculateSalary());
        SaveTable.setOnMouseClicked(mouseEvent -> saveSalaryTableToFile());
        SalaryAdminPageAdd.setOnAction(actionEvent -> addSalaryRecord());

    }

    private void addSalaryRecord() {
        try {
            double baseSalary = Double.parseDouble(SalaryPageBase.getText());
            double bonus = Double.parseDouble(SalaryPageBonus.getText());
            double hoursWorked = Double.parseDouble(SalaryPageHours.getText());
            double ratePerHour = Double.parseDouble(SalaryPageRatePerHour.getText());
            double ticketsFine = Double.parseDouble(SalaryPageTickets.getText());
            String positionName = CalculateSalaryAdminPagePositionName.getText();

            if (!positionExists(positionName)) {
                showErrorDialog("Такой должности нет в таблице Positions.");
                return;
            }

            double grossSalary = baseSalary + (hoursWorked * ratePerHour) - ticketsFine + bonus;
            double netSalary = grossSalary * 0.87;

            Request req = Request.of("ADD_SALARY")
                    .put("positionName", positionName)
                    .put("baseRate", String.valueOf(baseSalary))
                    .put("bonus", String.valueOf(bonus))
                    .put("workedHours", String.valueOf(hoursWorked))
                    .put("ratePerHour", String.valueOf(ratePerHour))
                    .put("result", String.valueOf(netSalary));
            Response resp = ClientContext.client().send(req);
            if (resp.isOk()) {
                System.out.println("[CLIENT] " + resp.getData());
            } else {
                showErrorDialog(resp.getError());
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Введите числовые значения во все поля");
        } catch (IOException e) {
            showErrorDialog("Ошибка связи с сервером.");
        }
    }

    private boolean positionExists(String positionName) {
        try {
            Response resp = ClientContext.client().send(
                    Request.of("POSITION_EXISTS").put("positionName", positionName));
            return resp.isOk() && Boolean.parseBoolean(resp.getData());
        } catch (IOException e) {
            return false;
        }
    }


    private void saveSalaryTableToFile() {
        Path path = Path.of("src/main/resources/files/SalaryTable.txt");
        try {
            Response resp = ClientContext.client().send("GET_SALARIES");
            if (!resp.isOk()) {
                showErrorDialog(resp.getError());
                return;
            }
            List<String> rows = JsonCodec.parseDataList(resp.getData());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                for (String row : rows) {
                    writer.write(row);
                    writer.newLine();
                }
                System.out.println("Сохранение выполнено успешно: " + path);
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка при сохранении в файл.");
        }
    }

    public void calculateSalary() {
        try {
            double baseSalary = Double.parseDouble(SalaryPageBase.getText());
            double bonus = Double.parseDouble(SalaryPageBonus.getText());
            double hoursWorked = Double.parseDouble(SalaryPageHours.getText());
            double ratePerHour = Double.parseDouble(SalaryPageRatePerHour.getText());
            double ticketsFine = Double.parseDouble(SalaryPageTickets.getText());

            double grossSalary = baseSalary + (hoursWorked * ratePerHour) - ticketsFine + bonus;
            double netSalary = grossSalary * 0.87;  // 13% tax

            BigDecimal SalaryBYN = BigDecimal.valueOf(netSalary).setScale(2, RoundingMode.HALF_UP);
            BigDecimal SalaryDollar = SalaryBYN.divide(BigDecimal.valueOf(3.1), 2, RoundingMode.HALF_UP);
            BigDecimal SalaryRUB = SalaryBYN.multiply(BigDecimal.valueOf(30)).setScale(2, RoundingMode.HALF_UP);

            String message = "Зарплата:\n" +
                    "BYN: " + SalaryBYN + "\n" +
                    "USD: " + SalaryDollar + "\n" +
                    "RUB: " + SalaryRUB;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Расчет зарплаты");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (NumberFormatException e) {
            showErrorDialog("Введите числовые значения во все поля");
        }
    }


    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


