package Pages;

import Database.Connector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GraphPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button GraphPageBack;

    @FXML
    private BarChart<String, Number> salaryChart;

    @FXML
    private Text GraphPageText;

    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        loggedInUsername = username;
        updateText();
        loadSalaryData();
    }

    private void updateText() {
        if (loggedInUsername != null) {
            GraphPageText.setText("График зарплаты пользователя " + loggedInUsername + " из отдела");
        }
    }

    private void loadSalaryData() {
        ObservableList<XYChart.Series<String, Number>> salaryData = FXCollections.observableArrayList();

        try {
            Connection connection = Connector.connect();
            String query = "SELECT EffectiveDate, Result FROM Salaries WHERE PositionName = (SELECT Position FROM Employees WHERE Username = ?)";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);

                try (ResultSet resultSet = statement.executeQuery()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Зарплата");

                    while (resultSet.next()) {
                        series.getData().add(new XYChart.Data<>(resultSet.getString("EffectiveDate"), resultSet.getDouble("Result")));
                    }

                    salaryData.add(series);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog();
        } finally {
            Connector.close();
        }

        salaryChart.setData(salaryData);
    }

    @FXML
    void initialize() {
        GraphPageBack.setOnAction(actionEvent -> GraphPageBack.getScene().getWindow().hide());
    }

    private void showErrorDialog() {
        // Implement your error dialog logic here
    }
}
