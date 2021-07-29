package server.pdo;

import server.DBConnect;
import table.SummaryTable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class PDOClientHandler implements Runnable {
    private final PDOServer server;
    private final Connection connection;
    private final ObjectOutputStream objectOutputStream;
    private final Scanner inMessage;
    private final Socket clientSocket;
    private static int clients_count = 0;
    private boolean running = true;
    private final Integer key;

    public PDOClientHandler(Socket socket, PDOServer server, Integer key, DBConnect dbConnect) throws IOException, SQLException {
        clients_count++;
        this.key = key;
        this.server = server;
        this.clientSocket = socket;
        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inMessage = new Scanner(socket.getInputStream());
        this.connection = dbConnect.getConnection();
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println(clientMessage);
                    switch (clientMessage) {
                        case "#UPDATE" -> {
                            String id = inMessage.nextLine();
                            String gos_num = inMessage.nextLine();
                            String departure_time = inMessage.nextLine();
                            String pdo = inMessage.nextLine();
                            updateRecording(id, gos_num, departure_time, pdo);
                            server.sendTableToAllPDOClients();
                            server.sendMsgToClientServer("#UPDATE");
                            server.sendMsgToClientServer(id);
                        }
                        case "#INITPDOTABLE" -> sendTable();
                        case "##session##end##" -> this.close();
                    }
                }
            } catch (NullPointerException | IOException | SQLException ex) {
                System.out.println("Error in switch statement admin server");
            }
        }
    }
    //TODO оформить подключение к кастомным бд и настройку портов
    //TODO сделать обнуление базы данных пре перезапуске сервера
    public void close() throws IOException {
        running = false;
        clientSocket.close();
        server.removeClient(key);
        clients_count--;
        //objectOutputStream.flush();
        objectOutputStream.close();
    }

    public void updateRecording(String id, String gos_num, String departure_time, String pdo) throws SQLException {
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state = 0 WHERE reg_num=" + gos_num);
        connection.createStatement().executeUpdate("UPDATE summary SET departure_time = '" + departure_time +
                "',gos_num='" + gos_num +
                "',pdo='" + pdo +
                "' WHERE id='" + id + "'"
        );
    }

    public void sendTable() throws IOException {
        ArrayList<Integer> carList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT reg_num FROM car_list where car_state = 1");
            while (rs.next()) {
                carList.add(rs.getInt("reg_num"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println(carList);
        objectOutputStream.writeObject(carList);
        objectOutputStream.flush();

        ArrayList<SummaryTable> arrayList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM summary where pdo = 'On approval'");
            while (rs.next()) {
                arrayList.add(new SummaryTable(
                        rs.getString("id"),
                        rs.getString("fio"),
                        rs.getString("departure_time"),
                        rs.getString("pdo"),
                        rs.getString("note"),
                        rs.getInt("gos_num"),
                        rs.getString("return_time")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        objectOutputStream.writeObject(arrayList);
        objectOutputStream.flush();
    }
}