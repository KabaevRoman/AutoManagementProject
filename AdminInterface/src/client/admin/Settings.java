package client.admin;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Settings {
    private String serverHost;
    private int serverPort;
    private String username;
    private String password;

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void getSettings() throws IOException {
        File file = new File("AdminSettings.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner sc = new Scanner(file);
        if (sc.hasNext()) {
            serverHost = sc.nextLine();
        }
        if (sc.hasNext()) {
            serverPort = Integer.parseInt(sc.nextLine());
        }
        if (sc.hasNext()) {
            username = sc.nextLine();
        }
        if (sc.hasNext()) {
            password = sc.nextLine();
        }
        sc.close();
    }
}
