package client.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class SettingsController implements Initializable {
    @FXML
    public TextField ipTextField;
    @FXML
    public TextField portTextField;
    @FXML
    public Button saveSettings;


    private void loadSceneAndMessage() throws IOException {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveSettings.setOnMouseClicked((ActionEvent) -> {
            String ipAddress = ipTextField.getText();
            String port = portTextField.getText();
            FileWriter myWriter;
            try {
                myWriter = new FileWriter("settings.txt");
                myWriter.write(ipAddress + "\n");
                myWriter.write(port);
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
