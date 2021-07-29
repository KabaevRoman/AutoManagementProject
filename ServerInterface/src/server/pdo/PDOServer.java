package server.pdo;


import server.DBConnect;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
        System.out.println("Admin server launched!");

        new Thread(() -> {
            try {
                pdoInfoSocket = new Socket("localhost", 7777);
                outMessage = new PrintWriter(pdoInfoSocket.getOutputStream());
                inMessage = new Scanner(pdoInfoSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println("NOT MAIN:" + clientMessage);
                    switch (clientMessage) {
                        case "#INSERT", "#FREEAUTO" -> {
                            try {
                                sendTableToAllPDOClients();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
            } catch (IOException | SQLException e) {
                System.out.println("socket was closed while listening it is fine");
            }
        }
    }

    public void close() throws IOException {
        running = false;
        if (clientSocket != null) {
            clientSocket.close();
        }
        this.clearClients();
        System.out.println("Server closed");
        serverSocket.close();
    }

    public void sendTableToAllPDOClients() throws IOException {
        for (Map.Entry<Integer, PDOClientHandler> client : clientsPDO.entrySet()) {
            client.getValue().sendTable();
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

    public void clearClients() {
        clientsPDO.clear();
    }
}