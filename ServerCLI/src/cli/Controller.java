package cli;

import javafx.fxml.Initializable;
import server.DBConnect;
import server.Server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller {
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
                    break;
                }
                case 1: {
                    dbNameString = sc.nextLine();
                    break;
                }
                case 2: {
                    dbUserString = sc.nextLine();
                    break;
                }
                case 3: {
                    dbPasswordString = sc.nextLine();
                    break;
                }
                case 4: {
                    serverPortString = sc.nextLine();
                    break;
                }
            }
        }

    }

    public void setDbAddressString(String dbAddressString) {
        this.dbAddressString = dbAddressString;
    }

    public void setDbNameString(String dbNameString) {
        this.dbNameString = dbNameString;
    }

    public void setDbUserString(String dbUserString) {
        this.dbUserString = dbUserString;
    }

    public void setPortUserString(String portUserString) {
        this.serverPortString = portUserString;
    }

    public void setDbPasswordString(String dbPasswordString) {
        this.dbPasswordString = dbPasswordString;
    }


    public void printSettings() {
        System.out.println("Адрес базы данных: " + dbAddressString);
        System.out.println("Имя базы данных: " + dbNameString);
        System.out.println("Имя пользователя базы данных: " + dbUserString);
        System.out.println("Порт: " + serverPortString);
        System.out.println("Пароль базы данных: " + dbPasswordString);
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

    public void setSettings(String dbAddress, String dbName, String dbUser, String dbPassword, String serverPort) {
        try {
            FileWriter myWriter = new FileWriter("ServerSettings.txt");
            myWriter.write(dbAddress + "\n");
            myWriter.write(dbName + "\n");
            myWriter.write(dbUser + "\n");
            myWriter.write(dbPassword + "\n");
            myWriter.write(serverPort);
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
}
