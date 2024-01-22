package Pages;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;


public class ShowPenaltiesPage {

    private String username;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button ShowTicketsBack;

    @FXML
    private TextArea ShowTicketsTextArea;

    @FXML
    private Text ShowTicketsUsername;

    @FXML
    void initialize() {

        ShowTicketsBack.setOnAction(actionEvent -> ShowTicketsBack.getScene().getWindow().hide());

    }

    public void showAllTickets() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM Penalties";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder ticketsInfo = new StringBuilder();
                BigDecimal totalAmount = BigDecimal.ZERO;

                while (resultSet.next()) {
                    String fineAmount = resultSet.getString("FineAmount");
                    String penaltyDate = resultSet.getString("PenaltyDate");
                    String description = resultSet.getString("Description");

                    totalAmount = totalAmount.add(new BigDecimal(fineAmount));

                    ticketsInfo.append("Пользователь: ").append(resultSet.getString("Username"))
                            .append(", Штраф: ").append(fineAmount).append(" BYN, Дата: ").append(penaltyDate)
                            .append(", Описание: ").append(description).append("\n");
                }

                ticketsInfo.append("Общая сумма: ").append(totalAmount).append(" BYN");

                ShowTicketsTextArea.setText(ticketsInfo.toString());

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
        }
    }


    public void setUsername(String username) {
        this.username = username;
        ShowTicketsUsername.setText("пользователя " + username);
        showTicketsForUser();
    }

    private void showTicketsForUser() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM Penalties WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder ticketsInfo = new StringBuilder();
                BigDecimal totalAmount = BigDecimal.ZERO;

                while (resultSet.next()) {
                    String fineAmount = resultSet.getString("FineAmount");
                    String penaltyDate = resultSet.getString("PenaltyDate");
                    String description = resultSet.getString("Description");

                    totalAmount = totalAmount.add(new BigDecimal(fineAmount));

                    ticketsInfo.append("Штраф: ").append(fineAmount).append(" BYN, Дата: ").append(penaltyDate)
                            .append(", Описание: ").append(description).append("\n");
                }

                ticketsInfo.append("Общая сумма: ").append(totalAmount).append(" BYN");

                ShowTicketsTextArea.setText(ticketsInfo.toString());

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
