package ui;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import server.DBConnect;
import server.client.Server;
import server.pdo.PDOServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {
    public TextField dbAddress;
    public TextField dbName;
    public TextField dbUser;
    public TextField portUser;
    public TextField portAdmin;
    public PasswordField dbPassword;
    public Button saveBtn;
    public Button relaunchBtn;
    public Label numOfClientsLabel;

    private String dbAddressString = "";
    private String dbNameString = "";
    private String dbUserString = "";
    private String portUserString = "0";
    private String portAdminString = "0";
    private String dbPasswordString = "";
    private DBConnect dbConnect;
    private Server serverUser;
    private PDOServer serverPdo;


    public void getSettings() throws IOException {
        File file = new File("settings.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner sc = new Scanner(file);
        for (int i = 0; i < 6; i++) {
            check(sc, i);
        }
        sc.close();
    }

    public void check(Scanner sc, int index) {
        if (sc.hasNext()) {
            switch (index) {
                case 0 -> {
                    dbAddressString = sc.nextLine();
                    dbAddress.setText(dbAddressString);
                }
                case 1 -> {
                    dbNameString = sc.nextLine();
                    dbName.setText(dbNameString);
                }
                case 2 -> {
                    dbUserString = sc.nextLine();
                    dbUser.setText(dbUserString);
                }
                case 3 -> {
                    dbPasswordString = sc.nextLine();
                    dbPassword.setText(dbPasswordString);
                }
                case 4 -> {
                    portUserString = sc.nextLine();
                    portUser.setText(portUserString);
                }
                case 5 -> {
                    portAdminString = sc.nextLine();
                    portAdmin.setText(portAdminString);
                }
            }
        }

    }

    public void setSettings() {
        try {
            FileWriter myWriter = new FileWriter("settings.txt");
            myWriter.write(dbAddress.getText() + "\n");
            myWriter.write(dbName.getText() + "\n");
            myWriter.write(dbUser.getText() + "\n");
            myWriter.write(dbPassword.getText() + "\n");
            myWriter.write(portUser.getText() + "\n");
            myWriter.write(portAdmin.getText());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        getSettings();
        dbConnect = new DBConnect(dbAddressString, dbNameString, dbUserString, dbPasswordString);
        serverUser = new Server(Integer.parseInt(portUserString), dbConnect, this);
        serverUser.start();
        serverPdo = new PDOServer(Integer.parseInt(portAdminString), dbConnect);
        serverPdo.start();
    }

    public void shutdown() throws IOException {
        serverUser.close();
        serverPdo.close();
    }

    public void closeProgram() {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveBtn.setOnMouseClicked((ActionEvent) -> setSettings());
        relaunchBtn.setOnMouseClicked((ActionEvent) -> {
            try {
                shutdown();
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
