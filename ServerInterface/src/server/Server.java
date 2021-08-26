package server;

import javafx.application.Platform;
import msg.ScreenLock;
import msg.UserInfo;
import ui.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Server extends Thread {
    public Map<String, AdminHandler> adminClients;
    public Map<String, UserHandler> userClients;
    public Map<String, AdminHandler> maintenanceClients;
    public Map<String, UserMetaData> userMetaData;
    private final ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean running;

    private final DBConnect dbConnect;
    public boolean saveToggled = true;
    private final Connection connection;
    public Controller controller;

    public Server(int port, DBConnect dbConnect, Controller controller) throws IOException, SQLException {
        this.controller = controller;
        this.running = true;
        this.userClients = new HashMap<>();
        this.adminClients = new HashMap<>();
        this.maintenanceClients = new HashMap<>();
        this.userMetaData = new HashMap<>();
        this.serverSocket = new ServerSocket(port);
        this.dbConnect = dbConnect;
        connection = dbConnect.getConnection();
        System.out.println("Server launched" + serverSocket.getLocalSocketAddress());
    }

    @Override
    public void run() {
        while (running) {
            try {
                clientSocket = serverSocket.accept();
                new Thread(new UserSeparator(clientSocket, connection, this, dbConnect)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeClient(String username) {
        if (userClients.containsKey(username)) {
            userClients.remove(username);
            System.out.println("Клиенты: " + userClients);
            Platform.runLater(() -> controller.numOfClientsLabel.setText(String.valueOf(userClients.size())));
        } else if (adminClients.containsKey(username)) {
            adminClients.remove(username);
            System.out.println("Администраторы: " + adminClients);
            Platform.runLater(() -> controller.numOfAdminLabel.setText(String.valueOf(adminClients.size())));
        }
    }

    public void close() throws IOException {
        running = false;
        try {
            adminClients.clear();
            userClients.clear();
            serverSocket.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void sendTableToAllClients(boolean notifyAdmin) throws IOException, SQLException {
        for (Map.Entry<String, UserHandler> client : userClients.entrySet()) {
            client.getValue().sendTable();
        }
        for (Map.Entry<String, AdminHandler> client : adminClients.entrySet()) {
            client.getValue().sendTable(notifyAdmin);
        }
    }

    public void resetData() throws SQLException {
        connection.createStatement().executeUpdate("TRUNCATE TABLE summary");
    }

    public void toggleSaveMode(boolean saveToggled) {
        this.saveToggled = saveToggled;
    }
}
