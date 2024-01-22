package Pages;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import Database.Connector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class UserEditInfoPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text MainPageName;

    @FXML
    private Button UserEditInfoPageBack;

    @FXML
    private Button UserEditInfoPageLastname;

    @FXML
    private Button UserEditInfoPageLogin;

    @FXML
    private Button UserEditInfoPageMiddlename;

    @FXML
    private Button UserEditInfoPageName;

    @FXML
    private Button UserEditInfoPageNumber;

    @FXML
    private Button UserEditInfoPagePassword;

    private String loggedInUsername;

    @FXML
    void initialize() {
        setupButtons();
    }

    private void setupButtons()
    {
        UserEditInfoPageBack.setOnAction(actionEvent -> UserEditInfoPageBack.getScene().getWindow().hide());
        UserEditInfoPageLastname.setOnAction(actionEvent -> changeLastname());
        UserEditInfoPageName.setOnAction(actionEvent -> changeName());
        UserEditInfoPageMiddlename.setOnAction(actionEvent -> changeMiddlename());
        UserEditInfoPageNumber.setOnAction(actionEvent -> changeNumber());
        UserEditInfoPageLogin.setOnAction(actionEvent -> changeLogin());
        UserEditInfoPagePassword.setOnAction(actionEvent -> changePassword());
    }

    private void changeLogin() {
        showInputDialog("Введите текущий логин:", currentLogin -> {
            if (currentLogin.equals(loggedInUsername)) {
                showInputDialog("Введите новый логин:", newLogin -> {
                    if (!newLogin.equals(currentLogin)) {
                        updateLogin(newLogin);
                    } else {
                        showErrorDialog("Новый логин должен отличаться от текущего.");
                    }
                });
            } else {
                showErrorDialog("Текущий логин введен неверно.");
            }
        });
    }

    private void updateLogin(String newLogin) {
        try {
            Connection connection = Connector.connect();
            connection.setAutoCommit(false); // Отключаем автоматическую фиксацию транзакций

            // Обновляем Username в таблице Users
            String updateUserQuery = "UPDATE Users SET Username=? WHERE Username=?";
            try (PreparedStatement updateUserStatement = connection.prepareStatement(updateUserQuery)) {
                updateUserStatement.setString(1, newLogin);
                updateUserStatement.setString(2, loggedInUsername);
                updateUserStatement.executeUpdate();
            }

            // Обновляем Username в таблице Employees
            String updateEmployeeQuery = "UPDATE Employees SET Username=? WHERE Username=?";
            try (PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeQuery)) {
                updateEmployeeStatement.setString(1, newLogin);
                updateEmployeeStatement.setString(2, loggedInUsername);
                updateEmployeeStatement.executeUpdate();
            }

            // Обновляем Username в таблице Penalties
            String updatePenaltiesQuery = "UPDATE Penalties SET Username=? WHERE Username=?";
            try (PreparedStatement updatePenaltiesStatement = connection.prepareStatement(updatePenaltiesQuery)) {
                updatePenaltiesStatement.setString(1, newLogin);
                updatePenaltiesStatement.setString(2, loggedInUsername);
                updatePenaltiesStatement.executeUpdate();
            }

            connection.commit(); // Фиксируем транзакцию

            loggedInUsername = newLogin; // Обновляем текущий логин

            showSuccessDialog("Логин успешно обновлен!\n Для обновления информации авторизируйтесь заново!");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении логина!");
        } finally {
            Connector.close();
        }
    }

    private void changePassword() {
        showInputDialog("Введите текущий пароль:", currentPassword -> {
            if (checkPassword(currentPassword)) {
                showInputDialog("Введите новый пароль:", this::updatePassword);
            } else {
                showErrorDialog("Текущий пароль введен неверно.");
            }
        });
    }

    private boolean checkPassword(String currentPassword) {
        try {
            Connection connection = Connector.connect();
            String query = "SELECT Password FROM Users WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, loggedInUsername);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("Password");
                        return currentPassword.equals(storedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Connector.close();
        }
        return false;
    }


    private void updatePassword(String newPassword) {
        try {
            Connection connection = Connector.connect();
            String query = "UPDATE Users SET Password=? WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newPassword);
                statement.setString(2, loggedInUsername);
                statement.executeUpdate();

                showSuccessDialog("Пароль успешно обновлен!\n Для обновления информации авторизируйтесь заново!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении пароля!");
        } finally {
            Connector.close();
        }
    }

    private void changeNumber()
    {
        showInputDialog("Введите новый номер телефона: ", this::updateNumber);
    }

    private void changeMiddlename()
    {
        showInputDialog("Введите новое отчество: ", this::updateMiddlename);

    }

    private void updateNumber(String newLastname) {
        try {
            Connection connection = Connector.connect();
            String query = "UPDATE Employees SET Number=? WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newLastname);
                statement.setString(2, loggedInUsername);
                statement.executeUpdate();

                showSuccessDialog("Номер успешно обновлена!");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении номера телефона");
        } finally {
            Connector.close();
        }
    }

    private void updateMiddlename(String newLastname) {
        try {
            Connection connection = Connector.connect();
            String query = "UPDATE Employees SET Middlename=? WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newLastname);
                statement.setString(2, loggedInUsername);
                statement.executeUpdate();

                showSuccessDialog("Отчество успешно обновлена!");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении отчества");
        } finally {
            Connector.close();
        }
    }
    
    private void changeName()
    {
        showInputDialog("Введите новое имя: ", this::updateName);
    }

    private void updateName(String newLastname) {
        try {
            Connection connection = Connector.connect();
            String query = "UPDATE Employees SET Firstname=? WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newLastname);
                statement.setString(2, loggedInUsername);
                statement.executeUpdate();

                showSuccessDialog("Имя успешно обновлена!");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении имени");
        } finally {
            Connector.close();
        }
    }

    public void setUserInfo(String username) {
        loggedInUsername = username;
    }

    private void changeLastname() {
        showInputDialog("Введите новую фамилию: ", this::updateLastname);
    }

    private void updateLastname(String newLastname) {
        try {
            Connection connection = Connector.connect();
            String query = "UPDATE Employees SET Lastname=? WHERE Username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newLastname);
                statement.setString(2, loggedInUsername);
                statement.executeUpdate();

                showSuccessDialog("Фамилия успешно обновлена!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Ошибка при изменении фамилии");
        } finally {
            Connector.close();
        }
    }

    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText("Ошибка!");
        alert.showAndWait();
    }

    private void showInputDialog(String promptText, Consumer<String> callback) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Изменение данных");
        dialog.setHeaderText(null);
        dialog.setContentText(promptText);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(callback);
    }

}
