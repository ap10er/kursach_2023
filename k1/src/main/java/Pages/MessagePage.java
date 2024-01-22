package Pages;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class MessagePage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button MessagePageBack;

    @FXML
    private Button MessagePageDelete;

    @FXML
    private Button MessagePageShow;

    @FXML
    private TextField MessagePageLogin;

    @FXML
    private Button MessagePageSend;

    @FXML
    private TextArea MessagePageTextField;

    @FXML
    private TextField MessagePageSender;

    @FXML
    private ImageView MessagePageSaveToFile;

    @FXML
    void initialize() {

        MessagePageBack.setOnAction(actionEvent -> MessagePageBack.getScene().getWindow().hide());
        MessagePageSend.setOnAction(actionEvent -> sendMessage());
        MessagePageShow.setOnAction(actionEvent -> showMessage());
        MessagePageDelete.setOnAction(actionEvent -> deleteMessage());
        MessagePageSaveToFile.setOnMouseClicked(mouseEvent ->saveMessagesToFile());
    }

    private void deleteMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удалить сообщение");
        dialog.setHeaderText("Введите логин пользователя:");
        dialog.setContentText("Логин:");

        dialog.showAndWait().ifPresent(login -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
                String query = "DELETE FROM messages WHERE Username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, login);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert("Уведомление успешно удалено", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Пользователя с таким логином не существует или уведомление уже удалено", Alert.AlertType.ERROR);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
            }
        });
    }


    private void sendMessage() {
        String login = MessagePageLogin.getText();
        String sender = MessagePageSender.getText(); // Получаем значение отправителя
        String message = MessagePageTextField.getText();

        if (login.isEmpty() || sender.isEmpty() || message.isEmpty()) {
            showAlert("Заполните все поля", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String userQuery = "SELECT * FROM users WHERE Username = ?";
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery)) {
                userStatement.setString(1, login);
                ResultSet userResultSet = userStatement.executeQuery();

                if (userResultSet.next()) {
                    // Добавление сообщения в таблицу messages
                    String insertQuery = "INSERT INTO messages (Sender, Username, Message) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, sender);
                        insertStatement.setString(2, login);
                        insertStatement.setString(3, message);
                        insertStatement.executeUpdate();

                        showAlert("Сообщение успешно отправлено", Alert.AlertType.INFORMATION);
                        MessagePageLogin.clear();
                        MessagePageSender.clear();
                        MessagePageTextField.clear();
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

    private void saveMessagesToFile() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
            String query = "SELECT * FROM messages";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder messages = new StringBuilder();

                while (resultSet.next()) {
                    String sender = resultSet.getString("Sender");
                    String username = resultSet.getString("Username");
                    String userMessage = resultSet.getString("Message");

                    messages.append("Отправитель: ").append(sender).append("\n")
                            .append("Логин пользователя: ").append(username).append("\n")
                            .append("Сообщение: ").append(userMessage).append("\n\n");
                }

                // Путь к файлу
                String filePath = "D:\\kursach\\k1\\src\\main\\resources\\files\\MessagesTable.txt";

                // Сохранение данных в файл
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(messages.toString());
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

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Показать сообщение");
        dialog.setHeaderText("Введите логин пользователя:");
        dialog.setContentText("Логин:");

        dialog.showAndWait().ifPresent(login -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/SalarySystem", "root", "13979")) {
                String query = "SELECT * FROM messages WHERE Username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, login);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    StringBuilder messages = new StringBuilder();

                    while (resultSet.next()) {
                        String userMessage = resultSet.getString("Message");
                        messages.append(userMessage).append("\n");
                    }

                    if (messages.length() > 0) {
                        MessagePageTextField.setText(messages.toString());
                    } else {
                        showAlert("Пользователя с таким логином не существует", Alert.AlertType.ERROR);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Ошибка при работе с базой данных", Alert.AlertType.ERROR);
            }
        });
    }

}


