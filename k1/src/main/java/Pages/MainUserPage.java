package Pages;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

import Database.Connector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainUserPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button MainPageLast;

    @FXML
    public Button MainPageBack;

    @FXML
    private Button MainPageCalculateSalary;

    @FXML
    private Button MainPageEditInfo;

    @FXML
    private Button MainPageExit;

    @FXML
    private Button MainPageHistory;

    @FXML
    private Text MainPageName;

    @FXML
    private Button MainPageShowInfo;

    @FXML
    private Button MainPageAllInfo;

    @FXML
    private Button MainPageTickets;

    @FXML
    private Text MainPageUsername;

    private String loggedInUsername;

    @FXML
    void initialize() {
        setupButtons();
    }

    public void setLoggedInUsername(String username) {
        loggedInUsername = username;
        MainPageUsername.setText(username);
    }

    private void setupButtons() {
        MainPageExit.setOnAction(actionEvent -> Platform.exit());
        MainPageBack.setOnAction(actionEvent -> openLoginPage());
        MainPageShowInfo.setOnAction(actionEvent -> showUserInfo());
        MainPageCalculateSalary.setOnAction(actionEvent -> calculateSalary());
        MainPageEditInfo.setOnAction(actionEvent -> editInfo());
        MainPageTickets.setOnAction(actionEvent -> showTickets());
        MainPageHistory.setOnAction(actionEvent -> notificationsMenu());
        MainPageLast.setOnAction(actionEvent -> openGraphPage());


    }

    private void openGraphPage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GraphPage.fxml"));
        try {
            Parent root = loader.load();
            GraphPage graphPage = loader.getController();
            graphPage.setLoggedInUsername(loggedInUsername); // Pass the username to GraphPage

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void notificationsMenu() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserNotifications.fxml"));
        try {
            Parent root = loader.load();
            UserNotifications userNotifications = loader.getController();
            userNotifications.setLoggedInUsername(loggedInUsername); // Передаем значение loggedInUsername
            userNotifications.displayMessages(); // Добавляем вызов метода для отображения сообщений

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showTickets() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserTicketsPage.fxml"));
        try {
            Parent root = loader.load();
            UserTicketsPage userTicketsPage = loader.getController();
            userTicketsPage.setLoggedInUsername(loggedInUsername);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editInfo() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserEditInfoPage.fxml"));
        try {
            Parent root = loader.load();
            UserEditInfoPage userEditInfoPage = loader.getController();
            userEditInfoPage.setUserInfo(loggedInUsername);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateSalary()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CalculateSalaryUserPage.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showUserInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserStatsPage.fxml"));
            Parent root = loader.load();
            UserStatsPage userStatsPage = loader.getController();

            // Получение информации о текущем пользователе из базы данных
            String userInfo = getUserInfoFromDatabase(loggedInUsername);

            userStatsPage.setUserInfo(userInfo);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private String getUserInfoFromDatabase(String username) {
        String userInfo = "";

        try {
            Connection connection = Connector.connect();
            String query = "SELECT * FROM Employees WHERE Username=?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int employeeId = resultSet.getInt("EmployeeID");
                        String lastName = resultSet.getString("Lastname");
                        String firstName = resultSet.getString("Firstname");
                        String middleName = resultSet.getString("Middlename");
                        String employeeNumber = resultSet.getString("Number");
                        double salary = resultSet.getDouble("Salary");
                        Date hireDate = resultSet.getDate("HireDate");
                        String employeeUsername = resultSet.getString("Username");
                        String departmentName = resultSet.getString("DepartmentName");
                        String employeePosition = resultSet.getString("Position");

                        userInfo = String.format(
                                "Айди сотрудника: %d\nФамилия: %s\nИмя: %s\nОтчество: %s\nНомер телефона: %s\nЗарплата: %.2f\nДата найма: %s\nЛогин: %s\nНазвание отдела:\n %s\nДолжность: %s",
                                employeeId, lastName, firstName, middleName, employeeNumber, salary, hireDate, employeeUsername, departmentName, employeePosition);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog();
        } finally {
            Connector.close();
        }

        return userInfo;
    }




    public void openLoginPage() {
        openNewStage();
    }
    private void handleException(Exception e) {
        e.printStackTrace();
        showErrorDialog();
    }

    private void openNewStage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        try {
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            MainPageBack.getScene().getWindow().hide();
        } catch (IOException e) {
            handleException(e);
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
