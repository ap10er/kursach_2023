package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import Database.Connector;
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

            // Проверка существования должности в таблице Positions
            if (!positionExists(positionName)) {
                showErrorDialog("Такой должности нет в таблице Positions.");
                return;
            }

            // Вычисление итоговой зарплаты
            double grossSalary = baseSalary + (hoursWorked * ratePerHour) - ticketsFine + bonus;
            double netSalary = grossSalary * 0.87;

            // Создание подключения к базе данных
            try (Connection connection = Connector.connect()) {
                // SQL-запрос для добавления новой записи в таблицу "Salaries"
                String insertQuery = "INSERT INTO Salaries (PositionName, BaseRate, Bonus, WorkedHours, RatePerHour, Result) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    // Установка значений параметров в запросе
                    preparedStatement.setString(1, positionName);
                    preparedStatement.setDouble(2, baseSalary);
                    preparedStatement.setDouble(3, bonus);
                    preparedStatement.setDouble(4, hoursWorked);
                    preparedStatement.setDouble(5, ratePerHour);
                    preparedStatement.setDouble(6, netSalary);

                    // Выполнение запроса
                    preparedStatement.executeUpdate();

                    // Получение сгенерированного ID записи
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int salaryID = generatedKeys.getInt(1);
                            // Теперь вы можете использовать salaryID по вашему усмотрению
                            System.out.println("Новая запись в таблицу Salaries добавлена успешно. ID записи: " + salaryID);
                        } else {
                            System.out.println("Не удалось получить ID для новой записи в таблицу Salaries.");
                        }
                    }
                }
            } catch (SQLException e) {
                showErrorDialog("Ошибка при выполнении SQL-запроса.");
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Введите числовые значения во все поля");
        }
    }

    // Метод для проверки существования должности в таблице Positions
    private boolean positionExists(String positionName) {
        try (Connection connection = Connector.connect()) {
            String query = "SELECT * FROM Positions WHERE PositionName = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, positionName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // Если запись существует, возвращается true
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Обработка ошибок в реальном приложении может потребоваться иначе
            return false;
        }
    }


    private void saveSalaryTableToFile() {

        String filePath = "D:\\kursach\\k1\\src\\main\\resources\\files\\SalaryTable.txt";
        try (Connection connection = Connector.connect(); /* инициализируйте ваше соединение с базой данных */) {
            String query = "SELECT * FROM Salaries";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    while (resultSet.next()) {
                        int salaryID = resultSet.getInt("SalaryID");
                        String positionName = resultSet.getString("PositionName");
                        double baseRate = resultSet.getDouble("BaseRate");
                        double bonus = resultSet.getDouble("Bonus");
                        int workedHours = resultSet.getInt("WorkedHours");
                        double ratePerHour = resultSet.getDouble("RatePerHour");
                        String effectiveDate = resultSet.getString("EffectiveDate");
                        double result = resultSet.getDouble("Result");

                        writer.write("Номер: " + salaryID + "\n" + "Должность: " + positionName + "\n" +
                                "Базовая ставка: " + baseRate + "\n" + "Премия: " + bonus + "\n" +
                                "Кол-во отработанных часов: " + workedHours + "\n" + "Оплата в час: " + ratePerHour + "\n" +
                                "Дата получения зарплаты: " + effectiveDate + "\n" + "Итого: " + result + "\n");
                        writer.newLine();
                    }
                    System.out.println("Сохранение выполнено успешно.");
                } catch (IOException e) {
                    showErrorDialog("Ошибка при сохранении в файл.");
                }
            }
        } catch (SQLException e) {
            showErrorDialog("Ошибка при выполнении SQL-запроса.");
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


