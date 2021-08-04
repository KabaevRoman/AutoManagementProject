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

    public Server(int port, DBConnect dbConnect) throws IOException {
        serverSocket = new ServerSocket(port);
        this.dbConnect = dbConnect;
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
            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println("NOT MAIN: " + clientMessage);
                    switch (clientMessage) {
                        case "#UPDATE" -> {
                            try {
                                sendTableToAllClients();
                                String id = inMessage.nextLine();
                                clients.get(Integer.parseInt(id)).sendTable(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        case "#TRUNCATE"->{
                            try {
                                sendTableToAllClients();
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
            } catch (IOException e) {
                System.out.println("socket was closed while listening");
            }
            ClientHandler client = new ClientHandler(clientSocket, this, key, dbConnect);
            clients.put(key, client);
            key++;
            new Thread(client).start();
        }
    }

    public void close() throws IOException {
        running = false;
        if (clientSocket != null) {
            clientSocket.close();
        }
        clearClients();
        System.out.println("Сервер остановлен");
        serverSocket.close();
        ss.close();
        pdoServerInfoSocket.close();
    }

    public void sendTableToAllClients() throws IOException {
        for (Map.Entry<Integer, ClientHandler> client : clients.entrySet()) {
            client.getValue().sendTable(false);
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

    public void clearClients() {
        clients.clear();
    }
}