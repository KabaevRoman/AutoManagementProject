package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import server.DBConnect;
import server.client.Server;
import server.pdo.PDOServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {
    @FXML
    public TextField dbAddress;
    @FXML
    public TextField dbName;
    @FXML
    public TextField dbUser;
    @FXML
    public TextField portUser;
    @FXML
    public TextField portAdmin;
    @FXML
    public PasswordField dbPassword;
    @FXML
    public Button saveBtn;
    @FXML
    public Button startServerBtn;
    @FXML
    public Label numOfClientsLabel;
    @FXML
    public RadioButton toggleSaveOn;
    @FXML
    public RadioButton toggleSaveOff;

    private String dbAddressString = "";
    private String dbNameString = "";
    private String dbUserString = "";
    private String portUserString = "0";
    private String portAdminString = "0";
    private String dbPasswordString = "";
    private DBConnect dbConnect;
    public Server serverUser;
    private PDOServer serverPdo;


    public void getSettings() throws IOException {
        File file = new File("ServerSettings.txt");
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
                case 0: {
                    dbAddressString = sc.nextLine();
                    dbAddress.setText(dbAddressString);
                    break;
                }
                case 1: {
                    dbNameString = sc.nextLine();
                    dbName.setText(dbNameString);
                    break;
                }
                case 2: {
                    dbUserString = sc.nextLine();
                    dbUser.setText(dbUserString);
                    break;
                }
                case 3: {
                    dbPasswordString = sc.nextLine();
                    dbPassword.setText(dbPasswordString);
                    break;
                }
                case 4: {
                    portUserString = sc.nextLine();
                    portUser.setText(portUserString);
                    break;
                }
                case 5: {
                    portAdminString = sc.nextLine();
                    portAdmin.setText(portAdminString);
                    break;
                }
            }
        }

    }

    public void setSettings() {
        try {
            FileWriter myWriter = new FileWriter("ServerSettings.txt");
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

    public void closeProgram() throws SQLException {
        try {
            serverPdo.resetData();
            serverPdo.close();
            serverUser.close();
        } catch (NullPointerException | IOException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public void startServer() throws IOException {
        getSettings();
        dbConnect = new DBConnect(dbAddressString, dbNameString, dbUserString, dbPasswordString);
        serverUser = new Server(Integer.parseInt(portUserString), dbConnect, this);
        serverPdo = new PDOServer(Integer.parseInt(portAdminString), dbConnect);
        serverUser.start();
        serverPdo.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            getSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
