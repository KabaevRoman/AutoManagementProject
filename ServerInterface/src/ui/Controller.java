package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import server.DBConnect;
import server.Server;

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
    public TextField serverPort;
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
    @FXML
    public Label numOfAdminLabel;


    private String dbAddressString = "";
    private String dbNameString = "";
    private String dbUserString = "";
    private String serverPortString = "0";
    private String dbPasswordString = "";
    private DBConnect dbConnect;
    public Server server;

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
                    serverPortString = sc.nextLine();
                    serverPort.setText(serverPortString);
                    break;
                }
            }
        }

    }

    public void getSettings() throws IOException {
        File file = new File("ServerSettings.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner sc = new Scanner(file);
        for (int i = 0; i < 5; i++) {
            check(sc, i);
        }
        sc.close();
    }

    public void setSettings() {
        try {
            FileWriter myWriter = new FileWriter("ServerSettings.txt");
            myWriter.write(dbAddress.getText() + "\n");
            myWriter.write(dbName.getText() + "\n");
            myWriter.write(dbUser.getText() + "\n");
            myWriter.write(dbPassword.getText() + "\n");
            myWriter.write(serverPort.getText());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeProgram() throws SQLException {
        try {
            server.resetData();
            server.close();
        } catch (NullPointerException | IOException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public void startServer() throws IOException, SQLException {
        getSettings();
        dbConnect = new DBConnect(dbAddressString, dbNameString, dbUserString, dbPasswordString);
        server = new Server(Integer.parseInt(serverPortString), dbConnect, this);
        server.start();
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
