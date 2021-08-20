package client.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;

    public void getSettings() throws FileNotFoundException {
        File file = new File("AdminSettings.txt");
        Scanner sc = new Scanner(file);
        if (sc.hasNext()) {
            ipTextField.setText(sc.nextLine());
        }
        if (sc.hasNext()) {
            portTextField.setText(sc.nextLine());
        }
        if (sc.hasNext()) {
            usernameField.setText(sc.nextLine());
        }
        if (sc.hasNext()) {
            passwordField.setText(sc.nextLine());
        }
        sc.close();
    }

    public void save() {
        String ipAddress = ipTextField.getText();
        String port = portTextField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        FileWriter myWriter;
        try {
            myWriter = new FileWriter("AdminSettings.txt");
            myWriter.write(ipAddress + "\n");
            myWriter.write(port + "\n");
            myWriter.write(username + "\n");
            myWriter.write(password);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            getSettings();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
