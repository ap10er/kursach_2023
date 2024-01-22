package Pages;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class HelpPage {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text AdminUsername;

    @FXML
    private Button HelpPageAccount;

    @FXML
    private Button HelpPageApp;

    @FXML
    private Button HelpPageBack;

    @FXML
    private Button HelpPageData;

    @FXML
    private Button HelpPageHowToReg;

    @FXML
    private TextArea HelpPageTextArea;

    @FXML
    private Text MainPageName;

    @FXML
    void initialize() {
        HelpPageBack.setOnAction(actionEvent -> HelpPageBack.getScene().getWindow().hide());

    }

}
