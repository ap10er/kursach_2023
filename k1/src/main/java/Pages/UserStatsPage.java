package Pages;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class UserStatsPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button UserStatsPageBack;

    @FXML
    private TextArea UserStatsTextField;


    @FXML
    void initialize() {

        UserStatsPageBack.setOnAction(actionEvent -> UserStatsPageBack.getScene().getWindow().hide());


    }

    public void setUserInfo(String userInfo) {
        UserStatsTextField.setText(userInfo);
    }

}
