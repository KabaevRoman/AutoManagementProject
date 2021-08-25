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
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private final DBConnect dbConnect;
    private boolean saveToggled = true;
    private Connection connection;
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
                //ОТДЕЛЬНЫЙ ПОТОК ??
                try {
                    objectOutputStream = null;
                    objectInputStream = null;
                    objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                    objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    UserInfo userInfo = (UserInfo) objectInputStream.readObject();//1 - admin, 2 - user, 404 - not found
                    String username = userInfo.getUsername();
                    switch (userStatus(userInfo)) {
                        case 1:
                            AdminHandler admin = new AdminHandler(objectOutputStream, objectInputStream,
                                    this, dbConnect, userInfo.getUsername());
                            adminClients.put(username, admin);
                            new Thread(admin).start();
                            Platform.runLater(() -> controller.numOfAdminLabel
                                    .setText(String.valueOf(adminClients.size())));
                            System.out.println("Админы: " + adminClients);
                            break;
                        case 0:
                            UserHandler user = new UserHandler(objectOutputStream, objectInputStream,
                                    this, dbConnect, saveToggled, username);
                            userClients.put(username, user);
                            if (!userMetaData.containsKey(username)) {
                                userMetaData.put(username, new UserMetaData(ScreenLock.UNLOCKED));
                                user.setLock(ScreenLock.UNLOCKED);
                            } else {
                                String id = userMetaData.get(username).id;
                                String gos_num = userMetaData.get(username).gos_num;
                                ScreenLock lockState = userMetaData.get(userInfo.getUsername()).lock;
                                user.setLock(lockState);
                                user.setGos_num(gos_num);
                                user.setId(id);
                            }
                            new Thread(user).start();
                            Platform.runLater(() -> controller.numOfClientsLabel
                                    .setText(String.valueOf(userClients.size())));
                            System.out.println("Клиенты: " + userClients);
                            break;
                        case 2:
                            AdminHandler maintenance = new AdminHandler(objectOutputStream, objectInputStream,
                                    this, dbConnect, userInfo.getUsername());
                            maintenanceClients.put(userInfo.getUsername(), maintenance);
                            System.out.println("Обслуживание:" + maintenanceClients);
                            new Thread(maintenance).start();
                            break;
                        case 404:
                            clientSocket.close();
                            break;
                    }
                } catch (IOException | ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                //TODO мб придется оборачивать в отдельный поток
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
            objectInputStream.close();
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


    public int userStatus(UserInfo userInfo) throws SQLException {
        //делаем запрос в юзерс и если во первых юзер есть пароль равен и получаем статус
        connection = dbConnect.getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select exists(select from " +
                "users where username='" + userInfo.getUsername() + "'and password='" + userInfo.getPassword() + "')");
        rs.next();
        if (rs.getBoolean("exists")) {
            rs = connection.createStatement()
                    .executeQuery("select admin from users where username ='" + userInfo.getUsername() + "'");
            rs.next();
            if (rs.getBoolean("admin")) {
                if (userInfo.isMaintenance()) {
                    return 2;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            return 404;
        }
    }

    public void toggleSaveMode(boolean saveToggled) {
        this.saveToggled = saveToggled;
    }
}
