package server;

import javafx.application.Platform;
import msg.ScreenLock;
import msg.UserInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSeparator implements Runnable {
    private Socket clientSocket;
    private Connection connection;
    private DBConnect dbConnect;
    private Server server;

    public UserSeparator(Socket clientSocket, Connection connection, Server server, DBConnect dbConnect) {
        this.clientSocket = clientSocket;
        this.connection = connection;
        this.server = server;
        this.dbConnect = dbConnect;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            UserInfo userInfo = (UserInfo) objectInputStream.readObject();//1 - admin, 2 - user, 404 - not found
            String username = userInfo.getUsername();
            switch (userStatus(userInfo)) {
                case 1:
                    AdminHandler admin = new AdminHandler(objectOutputStream, objectInputStream,
                            server, dbConnect, userInfo.getUsername());
                    server.adminClients.put(username, admin);
                    new Thread(admin).start();
                    System.out.println("Админы: " + server.adminClients);
                    break;
                case 0:
                    UserHandler user = new UserHandler(objectOutputStream, objectInputStream,
                            server, dbConnect, server.saveToggled, username);
                    server.userClients.put(username, user);
                    if (!server.userMetaData.containsKey(username)) {
                        server.userMetaData.put(username, new UserMetaData(ScreenLock.UNLOCKED));
                        user.setLock(ScreenLock.UNLOCKED);
                    } else {
                        String id = server.userMetaData.get(username).id;
                        String gos_num = server.userMetaData.get(username).gos_num;
                        ScreenLock lockState = server.userMetaData.get(userInfo.getUsername()).lock;
                        user.setLock(lockState);
                        user.setGos_num(gos_num);
                        user.setId(id);
                    }
                    new Thread(user).start();
                    System.out.println("Клиенты: " + server.userClients);
                    break;
                case 2:
                    AdminHandler maintenance = new AdminHandler(objectOutputStream, objectInputStream,
                            server, dbConnect, userInfo.getUsername());
                    server.maintenanceClients.put(userInfo.getUsername(), maintenance);
                    System.out.println("Обслуживание:" + server.maintenanceClients);
                    new Thread(maintenance).start();
                    break;
                case 404:
                    clientSocket.close();
                    break;
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

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
}
