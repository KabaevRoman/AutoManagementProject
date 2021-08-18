package cli;

import javafx.scene.control.*;
import server.DBConnect;
import server.client.Server;
import server.pdo.PDOServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Controller {

    private String dbAddressString = "";
    private String dbNameString = "";
    private String dbUserString = "";
    private String portUserString = "0";
    private String portAdminString = "0";
    private String dbPasswordString = "";
    private DBConnect dbConnect;
    public Server serverUser;
    public PDOServer serverPdo;

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
        this.portUserString = portUserString;
    }

    public void setPortAdminString(String portAdminString) {
        this.portAdminString = portAdminString;
    }

    public void setDbPasswordString(String dbPasswordString) {
        this.dbPasswordString = dbPasswordString;
    }

    public void printSettings() {
        System.out.println("Адрес базы данных: " + dbAddressString);
        System.out.println("Имя базы данных: " + dbNameString);
        System.out.println("Имя пользователя базы данных: " + dbUserString);
        System.out.println("Порт для клиентов базы данных: " + portUserString);
        System.out.println("Порт для администраторов базы данных: " + portAdminString);
        System.out.println("Пароль базы данных: " + dbPasswordString);
    }

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
                    portUserString = sc.nextLine();
                    break;
                }
                case 5: {
                    portAdminString = sc.nextLine();
                    break;
                }
            }
        }

    }

    public void closeProgram() throws SQLException {
        try {
            serverPdo.resetData();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public void shutdown() throws IOException, SQLException {
        serverPdo.close();
        serverUser.close();
    }

    public void setSettings(String dbAddress, String dbName, String dbUser, String dbPassword, String portUser, String portAdmin) {
        try {
            FileWriter myWriter = new FileWriter("ServerSettings.txt");
            myWriter.write(dbAddress + "\n");
            myWriter.write(dbName + "\n");
            myWriter.write(dbUser + "\n");
            myWriter.write(dbPassword + "\n");
            myWriter.write(portUser + "\n");
            myWriter.write(portAdmin);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initServer() throws IOException {
        getSettings();
        dbConnect = new DBConnect(dbAddressString, dbNameString, dbUserString, dbPasswordString);
        serverUser = new Server(Integer.parseInt(portUserString), dbConnect);
        serverUser.start();
        serverPdo = new PDOServer(Integer.parseInt(portAdminString), dbConnect);
        serverPdo.start();
    }
}
