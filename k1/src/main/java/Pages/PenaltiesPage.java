package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PenaltiesPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button TicketPageAddTicket;

    @FXML
    private Button TicketPageBack;

    @FXML
    private TextField TicketPageDescription;

    @FXML
    private Button TicketPageRemoveTicket;

    @FXML
    private Button TicketPageShowAll;

    @FXML
    private Button TicketPageShowTickets;

    @FXML
    private TextField TicketsPageLogin;

    @FXML
    private TextField TicketsPageTicket;

    @FXML
    private ImageView TicketsPageSaveToFile;

    @FXML
    void initialize() {
        TicketPageBack.setOnAction(actionEvent -> TicketPageBack.getScene().getWindow().hide());
        TicketPageAddTicket.setOnAction(actionEvent -> addTicket());
        TicketPageRemoveTicket.setOnAction(actionEvent -> removeTicket());
        TicketPageShowTickets.setOnAction(actionEvent -> showTickets());
        TicketPageShowAll.setOnAction(actionEvent -> showAllTickets());
        TicketsPageSaveToFile.setOnMouseClicked(mouseEvent -> saveTicketsToFile());

    }

    private void saveTicketsToFile() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM Penalties";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder penalties = new StringBuilder();

                while (resultSet.next()) {
                    String username = resultSet.getString("Username");
                    BigDecimal fineAmount = resultSet.getBigDecimal("FineAmount");
                    String penaltyDate = resultSet.getString("PenaltyDate");
                    String description = resultSet.getString("Description");

                    penalties.append("Логин пользователя: ").append(username).append("\n")
                            .append("Сумма штрафа: ").append(fineAmount).append(" BYN\n")
                            .append("Дата штрафа: ").append(penaltyDate).append("\n")
                            .append("Описание: ").append(description).append("\n\n");
                }

                // Путь к файлу
                String filePath = "D:\\kursach\\k1\\src\\main\\resources\\files\\PenaltiesTable.txt";

                // Сохранение данных в файл
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(penalties.toString());
                } catch (IOException e) {
                    System.out.println("Ошибка при сохранении данных в файл.");
                    e.printStackTrace();
                    showAlert("Ошибка при сохранении данных в файл.", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
        }
    }

    private void showAllTickets() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM Penalties";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                // Проверяем, есть ли записи в таблице
                if (resultSet.next()) {
                    openShowTicketsPageForAll();
                } else {
                    showAlert("Нет данных для отображения", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
        }
    }

    private void openShowTicketsPageForAll() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ShowPenaltiesPage.fxml"));
            Parent root = loader.load();

            ShowPenaltiesPage showPenaltiesPageController = loader.getController();
            showPenaltiesPageController.showAllTickets();

            // Открываем новое окно ShowTicketsPage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Штрафы всех пользователей");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка при открытии окна ShowTicketsPage", Alert.AlertType.ERROR);
        }
    }


    private void showTickets() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Просмотр штрафов");
        dialog.setHeaderText("Введите логин пользователя:");
        dialog.setContentText("Логин:");

        dialog.showAndWait().ifPresent(login -> {
            // Проверяем, есть ли штрафы для введенного логина
            if (hasPenaltiesForUser(login)) {
                openShowTicketsPage(login);
            } else {
                showAlert("Штрафов для пользователя с таким логином не найдено", Alert.AlertType.ERROR);
            }
        });
    }

    private void openShowTicketsPage(String login) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ShowPenaltiesPage.fxml"));
            Parent root = loader.load();

            ShowPenaltiesPage showPenaltiesPageController = loader.getController();

            // Устанавливаем логин пользователя для отображения
            showPenaltiesPageController.setUsername(login);

            // Открываем новое окно ShowTicketsPage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Штрафы пользователя");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка при открытии окна ShowTicketsPage", Alert.AlertType.ERROR);
        }
    }

    private boolean hasPenaltiesForUser(String login) {
        // Проверяем, есть ли штрафы для пользователя с указанным логином в базе данных
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM Penalties WHERE Username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, login);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
            return false;
        }
    }


    private void removeTicket() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удалить штраф");
        dialog.setHeaderText("Введите логин пользователя:");
        dialog.setContentText("Логин:");

        dialog.showAndWait().ifPresent(login -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
                String query = "SELECT * FROM Penalties WHERE Username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, login);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String deleteQuery = "DELETE FROM Penalties WHERE Username = ?";
                        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                            deleteStatement.setString(1, login);
                            deleteStatement.executeUpdate();
                            showAlert("Штраф успешно удален", Alert.AlertType.INFORMATION);
                        }
                    } else {
                        showAlert("Штрафов для пользователя с таким логином не найдено", Alert.AlertType.ERROR);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
            }
        });
    }

    private void addTicket() {
        String login = TicketsPageLogin.getText();
        String ticketAmount = TicketsPageTicket.getText();
        String description = TicketPageDescription.getText();

        if (login.isEmpty() || ticketAmount.isEmpty() || description.isEmpty()) {
            showAlert("Заполните все поля", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String userQuery = "SELECT * FROM Users WHERE Username = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, login);
                ResultSet userResultSet = userStatement.executeQuery();

                if (userResultSet.next()) {
                    String insertQuery = "INSERT INTO Penalties (Username, FineAmount, PenaltyDate, Description) VALUES (?, ?, CURDATE(), ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, login);
                        insertStatement.setBigDecimal(2, new BigDecimal(ticketAmount));
                        insertStatement.setString(3, description);
                        insertStatement.executeUpdate();
                        showAlert("Штраф успешно добавлен", Alert.AlertType.INFORMATION);
                        TicketsPageLogin.clear();
                        TicketsPageTicket.clear();
                        TicketPageDescription.clear();
                    }
                } else {
                    showAlert("Пользователя с таким логином не существует", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

}
