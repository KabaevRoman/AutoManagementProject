package server;

import server.DBConnect;
import server.client.Server;
import server.pdo.PDOServer;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter hostname / ip-address. (By default localhost, press Enter to select default option)");
        String hostname = sc.nextLine();
        System.out.println("Enter the name of the database (UMTSIK by default, press Enter to select default option)");
        String dbName = sc.nextLine();
        System.out.println("Enter your database username (Postgres by default, press Enter to select default option)");
        String dbUser = sc.nextLine();
        System.out.println("Enter the password of the database user (2019 by default, press Enter to select default option)");
        String password = sc.nextLine();
        DBConnect dbConnect = new DBConnect(hostname, dbName, dbUser, password);
        Server serverUser = new Server(3443, dbConnect);
        serverUser.start();
        PDOServer serverPdo = new PDOServer(5555, dbConnect);
        serverPdo.start();
        String control = "";
        while (!control.equals("exit")) {
            System.out.println("Type exit to close close server");
            control = sc.next();
            if (control.equals("exit")) {
                serverPdo.close();
                serverUser.close();
            }
        }
    }
}
