package server.pdo;


import server.DBConnect;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PDOServer extends Thread {

    private Integer key = 0;
    private final Map<Integer, PDOClientHandler> clientsPDO = new HashMap<>();
    private Socket clientSocket;
    private final ServerSocket serverSocket;
    private boolean running = true;
    private Socket pdoInfoSocket;
    private PrintWriter outMessage;
    private Scanner inMessage;
    private final DBConnect dbConnect;

    public PDOServer(int port, DBConnect dbConnect) throws IOException {
        serverSocket = new ServerSocket(port);
        this.dbConnect = dbConnect;
        System.out.println("Admin server launched" + serverSocket.getLocalSocketAddress());

        new Thread(() -> {
            try {
                pdoInfoSocket = new Socket("localhost", 7777);
                outMessage = new PrintWriter(pdoInfoSocket.getOutputStream());
                inMessage = new Scanner(pdoInfoSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (running) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println("NOT MAIN:" + clientMessage);
                    switch (clientMessage) {
                        case "#INSERT": {
                            try {
                                sendTableToAllPDOClients(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "#FREEAUTO":
                        case "#UPDATECONTENT": {
                            try {
                                sendTableToAllPDOClients(false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                clientSocket = serverSocket.accept();
                PDOClientHandler client = new PDOClientHandler(clientSocket, this, key, dbConnect);
                clientsPDO.put(key, client);
                key++;
                new Thread(client).start();
            } catch (IOException | SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        running = false;
        if (nullChecker(clientsPDO))
            clientsPDO.clear();
        if (nullChecker(clientSocket))
            clientSocket.close();
        if (nullChecker(serverSocket))
            serverSocket.close();
        if (nullChecker(pdoInfoSocket))
            pdoInfoSocket.close();
        if (nullChecker(outMessage))
            outMessage.close();
        if (nullChecker(inMessage))
            inMessage.close();
        System.out.println("Server closed");

    }

    public boolean nullChecker(Object object) {
        return object != null;
    }

    public void resetData() throws SQLException {
        Connection connection = dbConnect.getConnection();
        connection.createStatement().executeUpdate("TRUNCATE TABLE summary");

    }


    public void sendTableToAllPDOClients(boolean notify) throws IOException {
        for (Map.Entry<Integer, PDOClientHandler> client : clientsPDO.entrySet()) {
            client.getValue().sendTable(notify);
        }
    }

    public void removeClient(Integer key) {
        System.out.println(clientsPDO);
        clientsPDO.remove(key);
        System.out.println(clientsPDO);
    }

    public void sendMsgToClientServer(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

}