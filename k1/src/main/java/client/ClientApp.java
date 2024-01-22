package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        openLoginPage(primaryStage);
    }

    private void openLoginPage(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Pages/LoginPage.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("LoginPage");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchClient() {
        launch();
    }
}
