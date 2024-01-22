package Pages;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import Database.Connector;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class UserTicketsPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text MainPageName;

    @FXML
    private Button UserTicketsPageBack;

    @FXML
    private TextArea UserTicketsPageTextField;

    private String loggedInUsername;

    @FXML
    private TextField UserTicketsSum;

    @FXML
    void initialize() {
        UserTicketsPageBack.setOnAction(actionEvent -> UserTicketsPageBack.getScene().getWindow().hide());
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        loadPenaltiesData(); // Загрузка данных при установке loggedInUsername
    }

    private void loadPenaltiesData() {
        // Загрузка данных из базы данных и отображение их в UserTicketsPageTextField
        try {
            Connection connection = Connector.connect();
            String query = "SELECT * FROM Penalties WHERE Username=?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);

                try (ResultSet resultSet = statement.executeQuery()) {
                    StringBuilder penaltiesInfo = new StringBuilder();
                    double totalSum = 0.0;

                    while (resultSet.next()) {
                        double fineAmount = resultSet.getDouble("FineAmount");
                        String penaltyDate = resultSet.getString("PenaltyDate");
                        String description = resultSet.getString("Description");

                        totalSum += fineAmount;

                        penaltiesInfo.append(String.format(
                                "Размер штрафа:\n %.2f BYN\n Дата получения:\n %s\n Описание:\n %s%n",
                                fineAmount, penaltyDate, description));
                    }

                    UserTicketsPageTextField.setText(penaltiesInfo.toString());
                    UserTicketsSum.setText(String.format("%.2f", totalSum));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog();
        } finally {
            Connector.close();
        }
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText("Ошибка!");
        alert.showAndWait();
    }
}

