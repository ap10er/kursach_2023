package Pages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CalculateSalaryUserPage {

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
    private TextField SalaryPageHours;

    @FXML
    private TextField SalaryPageRatePerHour;

    @FXML
    private TextField SalaryPageRatePerHour1;

    @FXML
    private TextField SalaryPageRatePerHour11;

    @FXML
    private TextField SalaryPageRatePerHour12;

    @FXML
    private TextField SalaryPageRatePerHour13;

    @FXML
    private TextField SalaryPageTickets;

    @FXML
    void initialize() {
        SalaryPageBack.setOnAction(actionEvent -> SalaryPageBack.getScene().getWindow().hide());
        SalaryPageCalculateButton.setOnAction(actionEvent -> calculateSalary());

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
