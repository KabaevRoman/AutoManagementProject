package client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileNotFoundException;
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

    public void getSettings() throws FileNotFoundException {
        File file = new File("settings.txt");
        Scanner sc = new Scanner(file);
        if (sc.hasNext()) {
            ipTextField.setText(sc.nextLine());
        }
        if (sc.hasNext()) {
            portTextField.setText(sc.nextLine());
        }
        sc.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            getSettings();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
