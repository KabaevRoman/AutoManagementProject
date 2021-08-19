package server.client;

import server.DBConnect;
import ui.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server extends Thread {
    private Map<String, ClientHandler> clients;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean running;
    private Integer key;
    private PrintWriter outMessage;
    private Scanner inMessage;
    private Scanner userReader;

    private ServerSocket ss;
    private Socket pdoServerInfoSocket;
    private final DBConnect dbConnect;
    private boolean saveToggled;

    public Controller controller;

    public void saveToggledMode(boolean saveToggled) {
        this.saveToggled = saveToggled;
        System.out.println(this.saveToggled);
    }

    public Server(int port, DBConnect dbConnect, Controller controller) throws IOException {
        this.controller = controller;
        this.running = true;
        this.clients = new HashMap<>();
        this.serverSocket = new ServerSocket(port);
        this.dbConnect = dbConnect;
        this.key = 0;
        System.out.println("Client server launched");

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
                    System.out.println("CLIENT SERVER: " + clientMessage);
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
            String username = null;
            try {
                clientSocket = serverSocket.accept();
                userReader = new Scanner(clientSocket.getInputStream());
                username = userReader.nextLine();
                System.out.println(username);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClientHandler client = new ClientHandler(clientSocket, this, dbConnect, saveToggled);
            clients.put(username, client);
            System.out.println(username);
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
        for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
            client.getValue().sendTable(0);
        }
    }

    public void removeClient(String key) {
        System.out.println(clients);
        clients.remove(key);
        System.out.println(clients);
    }

    public void sendMsgToPDOServer(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

}