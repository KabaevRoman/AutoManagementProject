package server.client;

import server.DBConnect;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server extends Thread {
    private final Map<Integer, ClientHandler> clients = new HashMap<>();
    private final ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean running = true;
    private Integer key = 0;
    private PrintWriter outMessage;
    private Scanner inMessage;

    private ServerSocket ss;
    private Socket pdoServerInfoSocket;
    private final DBConnect dbConnect;
    private boolean saveToggled;


    public void saveToggledMode(boolean saveToggled) {
        this.saveToggled = saveToggled;
    }

    public Server(int port, DBConnect dbConnect) throws IOException {
        serverSocket = new ServerSocket(port);
        this.dbConnect = dbConnect;
        System.out.println("Client server launched " + serverSocket.getLocalSocketAddress());
        //System.out.println("Client server launched on host"+);

        new Thread(() -> {
            try {
                ss = new ServerSocket(7777);
                pdoServerInfoSocket = ss.accept();
                outMessage = new PrintWriter(pdoServerInfoSocket.getOutputStream());
                inMessage = new Scanner(pdoServerInfoSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (running) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println("NOT MAIN: " + clientMessage);
                    switch (clientMessage) {
                        case "#UPDATE": {
                            try {
                                sendTableToAllClients();
                                String pdo = inMessage.nextLine();
                                String id = inMessage.nextLine();
                                if (pdo.equals("Одобрено"))
                                    clients.get(Integer.parseInt(id)).sendTable(1);
                                else if (pdo.equals("Отказ"))
                                    clients.get(Integer.parseInt(id)).sendTable(2);
                                else
                                    clients.get(Integer.parseInt(id)).sendTable(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "#UPDATEUI": {
                            try {
                                sendTableToAllClients();
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
            } catch (IOException e) {
                System.out.println("socket was closed while listening");
            }
            ClientHandler client = new ClientHandler(clientSocket, this, key, dbConnect, saveToggled);
            clients.put(key, client);
            key++;
            new Thread(client).start();
        }
    }

    public void close() throws IOException {
        running = false;
        if (nullChecker(clients))
            clients.clear();
        if (nullChecker(serverSocket))
            serverSocket.close();
        if (nullChecker(serverSocket))
            serverSocket.close();
        if (nullChecker(clientSocket))
            clientSocket.close();
        if (nullChecker(outMessage))
            outMessage.close();
        if (nullChecker(inMessage))
            inMessage.close();
        if (nullChecker(ss))
            ss.close();
        if (nullChecker(pdoServerInfoSocket))
            pdoServerInfoSocket.close();
        System.out.println("Сервер остановлен");

    }

    public boolean nullChecker(Object object) {
        return object != null;
    }

    public void sendTableToAllClients() throws IOException {
        for (Map.Entry<Integer, ClientHandler> client : clients.entrySet()) {
            client.getValue().sendTable(0);
        }
    }

    public void removeClient(Integer key) {
        System.out.println(clients);
        clients.remove(key);
        System.out.println(clients);
    }

    public void sendMsgToPDOServer(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

}