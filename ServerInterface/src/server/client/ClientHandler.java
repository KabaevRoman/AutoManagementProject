package server.client;

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

public class ClientHandler implements Runnable {
    private Server server;
    private Connection connection;
    private ObjectOutputStream objectOutputStream;
    private Scanner inMessage;
    private Socket clientSocket;
    private static int clients_count = 0;
    private boolean running = true;
    private Integer key;

    public ClientHandler(Socket socket, Server server, Integer key, DBConnect dbConnect) {
        try {
            clients_count++;
            this.key = key;
            this.server = server;
            this.clientSocket = socket;
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
            this.connection = dbConnect.getConnection();
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    //TODO добавить аварийное обслуживание со стороны пдошников где они могут просматривать всю базу и редачить что надо
    @Override
    public void run() {
        while (running) {
            try {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println(clientMessage);
                    System.out.println("nigger? ");
                    switch (clientMessage) {
                        case "#INSERT" -> {
                            String name = inMessage.nextLine();
                            String time = inMessage.nextLine();
                            insertRecording(name, time);
                            server.sendTableToAllClients();
                            server.sendMsgToPDOServer("#INSERT");
                        }
                        case "#FREEAUTO" -> {
                            changeAutoState();
                            String date = inMessage.nextLine();
                            saveReturnTime(date);
                        }
                        case "#INITTABLE" -> sendTable(false);
                        case "##session##end##" -> this.close();
                    }
                }
            } catch (NullPointerException | IOException | SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error in switch");
            }
        }
    }

    private void saveReturnTime(String date) {
        System.out.println("UPDATE summary SET return_time =" + date + " WHERE id=" + key);
        try {
            connection.createStatement().executeUpdate(
                    "UPDATE summary SET return_time =" + "'" + date + "'" + " WHERE id=" + key);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void close() throws IOException {
        running = false;
        clientSocket.close();
        server.removeClient(key);
        clients_count--;
        objectOutputStream.close();
    }

    public void insertRecording(String name, String time) throws SQLException {
        connection.createStatement().executeUpdate("INSERT INTO summary(id,fio,departure_time,pdo) VALUES(" +
                "'" + key + "'," +
                "'" + name + "'" + "," + "'" + time + "'" + "," + "'" + "On approval" + "')");
    }

    public void changeAutoState() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery(
                "select gos_num from summary where id = " + "'" + key + "'");
        Integer gos_num = null;
        while (rs.next()) {
            gos_num = rs.getInt("gos_num");
        }
        System.out.println(gos_num);
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state = 1 WHERE reg_num=" + gos_num);
    }


    //TODO добавить количество машин, если человек возвращается должно ставиться фактическое время возвращения
    public void sendTable(boolean lock) throws IOException {
        objectOutputStream.writeBoolean(lock);
        objectOutputStream.flush();

        int numOfAvailableCars = 0;
        try {
            ResultSet rs = connection.createStatement().executeQuery("select count(*) from car_list where car_state = 1");
            while (rs.next()) {
                numOfAvailableCars = rs.getInt("count");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println(numOfAvailableCars);
        objectOutputStream.writeInt(numOfAvailableCars);
        objectOutputStream.flush();

        ArrayList<SummaryTable> arrayList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM summary");
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