package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientContext;
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
            protocol.Response resp = ClientContext.client().send(
                    protocol.Request.of("SALARY_CHART").put("username", loggedInUsername));
            if (resp.isOk()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Зарплата");
                for (String point : protocol.JsonCodec.parseDataList(resp.getData())) {
                    int sep = point.lastIndexOf('|');
                    if (sep > 0) {
                        series.getData().add(new XYChart.Data<>(
                                point.substring(0, sep),
                                Double.parseDouble(point.substring(sep + 1))));
                    }
                }
                salaryData.add(series);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        salaryChart.setData(salaryData);
    }

    @FXML
    void initialize() {
        GraphPageBack.setOnAction(actionEvent -> GraphPageBack.getScene().getWindow().hide());
    }

    private void showErrorDialog() {

    }
}
