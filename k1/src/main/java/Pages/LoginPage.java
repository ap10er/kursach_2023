package Pages;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientContext;
import protocol.Request;
import protocol.Response;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginPage {

    private String loggedInUsername;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button StartPageAuthButton;

    @FXML
    private TextField StartPageLoginField;

    @FXML
    private TextField StartPagePasswordField;

    @FXML
    private Button StartPageRegButton;

    @FXML
    private ImageView LoginPageHelp;

    @FXML
    void initialize() {
        StartPageRegButton.setOnAction(actionEvent -> openRegPage());
        LoginPageHelp.setOnMouseClicked(mouseEvent -> openHelpPage());
        StartPageAuthButton.setOnAction(actionEvent -> handleLoginButtonAction());

    }

    private void openHelpPage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HelpPage.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getRoleFromDatabase(String username, String password) {
        try {
            Request req = Request.of("LOGIN").put("username", username).put("password", password);
            Response resp = ClientContext.client().send(req);
            if (resp.isOk()) return resp.getData();
            System.out.println("[CLIENT] LOGIN error: " + resp.getError());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleLoginButtonAction() {
        String username = StartPageLoginField.getText();
        String password = StartPagePasswordField.getText();

        if (password.length() < 4){
            showErrorDialog("Слишком короткий пароль!");
            return;
        }

        String role = getRoleFromDatabase(username, password);

        if (checkCredentials(username, password, role)) {
            openMainPage(username, role);
        } else {
            showErrorDialog("Неверный логин или пароль!");
        }
    }
    private boolean checkCredentials(String username, String password, String role) {
        return role != null;
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openMainPage (String username, String role) {

        FXMLLoader loader;
        if (role.equalsIgnoreCase("Admin")) {
            loader = new FXMLLoader(getClass().getResource("MainAdminPage.fxml"));
        } else {
            loader = new FXMLLoader(getClass().getResource("MainUserPage.fxml"));
        }
        try {
            Parent root = loader.load();
            if ("Admin".equals(role)) {
                MainAdminPage mainAdminPageController = loader.getController();
                mainAdminPageController.setLoggedInUsername(username);
            } else {

                MainUserPage mainUserPageController = loader.getController();
                mainUserPageController.setLoggedInUsername(username);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            StartPageAuthButton.getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRegPage()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegPage.fxml"));
        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            stage.show();

            StartPageRegButton.getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}