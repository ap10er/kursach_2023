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

public class UserNotifications {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button UserNotificationsBack;

    @FXML
    private Button UserNotificationsDelete;

    @FXML
    private TextField UserNotificationsLogin;

    @FXML
    private TextField UserNotificationsSender;

    @FXML
    private Button UserNotificationsSend;

    @FXML
    private TextArea UserNotificationsTextField;

    private String loggedInUsername;

    @FXML
    void initialize() {
        UserNotificationsBack.setOnAction(actionEvent -> UserNotificationsBack.getScene().getWindow().hide());
        UserNotificationsDelete.setOnAction(actionEvent -> deleteNotifications());
        UserNotificationsSend.setOnAction(actionEvent -> sendNotification());


    }

    private void sendNotification() {
        String receiverUsername = UserNotificationsLogin.getText();
        String senderUsername = UserNotificationsSender.getText();
        String messageText = UserNotificationsTextField.getText();

        if (!receiverUsername.isEmpty() && !senderUsername.isEmpty() && !messageText.isEmpty()) {
            // Проверяем, существует ли пользователь с указанным логином
            if (userExists(receiverUsername)) {
                // Отправляем уведомление в базу данных
                saveNotification(receiverUsername, senderUsername, messageText);

                // Обновляем отображение уведомлений
                displayMessages();
            } else {
                showErrorDialog("Пользователь с логином " + receiverUsername + " не найден!");
            }
        } else {
            // Вывод сообщения об ошибке (можно изменить)
            showErrorDialog("Заполните все поля для отправки уведомления.");
        }
    }

    private boolean userExists(String username) {
        String query = "SELECT * FROM Employees WHERE Username = ?";
        try (Connection connection = Connector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // Возвращает true, если пользователь существует
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Connector.close();
        }
        return false;
    }

    private void showErrorDialog(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void saveNotification(String receiverUsername, String senderUsername, String messageText) {
        String insertQuery = "INSERT INTO Messages (Username, Sender, Message) VALUES (?, ?, ?)";
        try (Connection connection = Connector.connect();
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, receiverUsername);
            insertStatement.setString(2, senderUsername);
            insertStatement.setString(3, messageText);

            // Выполняем запрос на добавление нового уведомления
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Connector.close();
        }
    }

    private void deleteNotifications() {
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            String deleteQuery = "DELETE FROM Messages WHERE Username = ?";
            try (Connection connection = Connector.connect();
                 PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                deleteStatement.setString(1, loggedInUsername);
                int rowsDeleted = deleteStatement.executeUpdate();

                if (rowsDeleted > 0) {
                    // Уведомления успешно удалены, обновите отображение
                    displayMessages();
                } else {
                    // Сообщение, если нет уведомлений для удаления
                    System.out.println("Нет уведомлений для удаления.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                Connector.close();
            }
        }
    }

    public void displayMessages() {
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            String query = "SELECT * FROM Messages WHERE Username = ?";
            try (Connection connection = Connector.connect();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, loggedInUsername);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder messageText = new StringBuilder();

                    while (resultSet.next()) {
                        String sender = resultSet.getString("Sender");
                        String message = resultSet.getString("Message");

                        messageText.append(String.format("Отправитель: %s%nСообщение:%n %s%n", sender, message));
                    }

                    UserNotificationsTextField.setText(messageText.toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                Connector.close();
            }
        }
    }


    public void setLoggedInUsername(String username) {
        loggedInUsername = username;
        // Дополнительные действия, если необходимо
    }


}
