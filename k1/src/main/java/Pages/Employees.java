package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import Database.Connector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class Employees {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button EmployeesBack;

    @FXML
    private ImageView EmployeesSaveToFile;

    @FXML
    private TextArea EmployeesTextArea;

    @FXML
    void initialize() {
        try {
            Connection connection = Connector.connect();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM employees");
            ResultSet resultSet = statement.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            while (resultSet.next()) {
                int employeeId = resultSet.getInt("EmployeeID");
                String lastName = resultSet.getString("LastName");
                String firstName = resultSet.getString("FirstName");
                String middleName = resultSet.getString("MiddleName");
                String number = resultSet.getString("Number");
                String position = resultSet.getString("Position");
                double salary = resultSet.getDouble("Salary");
                Date hireDate = resultSet.getDate("HireDate");
                String username = resultSet.getString("Username");
                String department = resultSet.getString("DepartmentName");

                stringBuilder.append("Номер сотрудника: ").append(employeeId).append("\n")
                        .append("Фамилия: ").append(lastName).append("\n")
                        .append("Имя: ").append(firstName).append("\n")
                        .append("Отчество: ").append(middleName).append("\n")
                        .append("Номер телефона: ").append(number).append("\n")
                        .append("Должность: ").append(position).append("\n")
                        .append("Зарплата: ").append(salary).append("\n")
                        .append("Дата начала работы:\n ").append(hireDate).append("\n")
                        .append("Логин: ").append(username).append("\n")
                        .append("Отдел: ").append(department).append("\n\n");
            }

            Platform.runLater(() -> EmployeesTextArea.setText(stringBuilder.toString()));

        } catch (SQLException e) {
            System.out.println("Failed to close resources!");
            e.printStackTrace();
        } finally {
            Connector.close();
        }


        EmployeesBack.setOnAction(actionEvent -> EmployeesBack.getScene().getWindow().hide());
        EmployeesSaveToFile.setOnMouseClicked(mouseEvent -> saveEmployeesToFile());

    }

    // Метод для сохранения данных в файл
    private void saveEmployeesToFile() {
        try {
            Connection connection = Connector.connect();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM employees");
            ResultSet resultSet = statement.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            while (resultSet.next()) {
                int employeeId = resultSet.getInt("EmployeeID");
                String lastName = resultSet.getString("LastName");
                String firstName = resultSet.getString("FirstName");
                String middleName = resultSet.getString("MiddleName");
                String number = resultSet.getString("Number");
                String position = resultSet.getString("Position");
                double salary = resultSet.getDouble("Salary");
                Date hireDate = resultSet.getDate("HireDate");
                String username = resultSet.getString("Username");
                String department = resultSet.getString("DepartmentName");

                stringBuilder.append("Номер сотрудника: ").append(employeeId).append("\n")
                        .append("Фамилия: ").append(lastName).append("\n")
                        .append("Имя: ").append(firstName).append("\n")
                        .append("Отчество: ").append(middleName).append("\n")
                        .append("Номер телефона: ").append(number).append("\n")
                        .append("Должность: ").append(position).append("\n")
                        .append("Зарплата: ").append(salary).append("\n")
                        .append("Дата начала работы:\n").append(hireDate).append("\n")
                        .append("Логин: ").append(username).append("\n")
                        .append("Отдел: ").append(department).append("\n\n");
            }

            // Путь к файлу
            String filePath = "D:\\kursach\\k1\\src\\main\\resources\\files\\EmployeesTable.txt";

            // Сохранение данных в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(stringBuilder.toString());
                System.out.println("Данные успешно сохранены в файл: " + filePath);
            } catch (IOException e) {
                System.out.println("Ошибка при сохранении данных в файл.");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.out.println("Failed to close resources!");
            e.printStackTrace();
        } finally {
            Connector.close();
        }
    }
}


