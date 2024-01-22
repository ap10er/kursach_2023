package Pages;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class UserPage {

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button UserPageBack;

    @FXML
    private Text UserPageName;

    @FXML
    private TextArea UserPageTextField;

    @FXML
    void initialize() {
        setupButtonEvents();
    }

    private void setupButtonEvents() {
        UserPageBack.setOnAction(actionEvent -> closeWindow());
    }

    private void closeWindow() {
        UserPageBack.getScene().getWindow().hide();
    }

    public void setLoggedInUsername(String username) {
        UserPageName.setText(username);
        setUsername(username);

    }
}

