package server.pdo;

import server.DBConnect;
import table.ArchiveTable;
import table.SummaryTable;
import table.VehicleTable;

import java.awt.*;
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
                        case "#UPDATE": {
                            String id = inMessage.nextLine();
                            String gos_num = inMessage.nextLine();
                            String departure_time = inMessage.nextLine();
                            String pdo = inMessage.nextLine();
                            updateRecording(id, gos_num, departure_time, pdo);
                            server.sendTableToAllPDOClients(false);
                            server.sendMsgToClientServer("#UPDATE");
                            server.sendMsgToClientServer(pdo);
                            server.sendMsgToClientServer(id);
                            break;
                        }
                        case "#INITPDOTABLE":
                            sendTable(false);
                            break;
                        case "#TRUNCATE": {
                            server.resetData();
                            server.sendTableToAllPDOClients(false);
                            server.sendMsgToClientServer("#UPDATEUI");
                            break;
                        }
                        case "#RESETVEHSTATE": {
                            resetVehicleState();
                            server.sendTableToAllPDOClients(false);
                            server.sendMsgToClientServer("#UPDATEUI");
                            break;
                        }
                        case "#GETARCHIVE": {
                            getArchive();
                            break;
                        }
                        case "#DBMAINTENANCE":
                            getAllRecords();
                            break;
                        case "#REGNUMMAINTENANCE":
                            sendRegNumRecords();
                            break;
                        case "#VEHICLESTATECHANGED": {
                            String reg_num = inMessage.nextLine();
                            String state = inMessage.nextLine();
                            updateRegNum(reg_num, state);
                            //server.sendTableToAllPDOClients(false);//вот тут может быть косяк если ты меняешь стейты
                            server.sendMsgToClientServer("#UPDATEUI");
                            break;
                        }
                        case "#ADDVEHICLE": {
                            String reg_num = inMessage.nextLine();
                            String state = inMessage.nextLine();
                            addVehicle(reg_num, state);
                            //server.sendTableToAllPDOClients(false); ломает взаимодействие с редактором
                            server.sendMsgToClientServer("#UPDATEUI");
                            break;
                        }
                        case "#DELETEVEHICLE": {
                            String reg_num = inMessage.nextLine();
                            deleteVehicle(reg_num);
                            //server.sendTableToAllPDOClients(false);
                            server.sendMsgToClientServer("#UPDATEUI");
                            break;
                        }
                        case "#REGNUMMAINTENANCECLOSE": {
                            server.sendTableToAllPDOClients(false);
                            break;
                        }
                        case "##session##end##":
                            this.close();
                            break;
                        case "#EDIT":
                            String id = inMessage.nextLine();
                            String departure_time = inMessage.nextLine();
                            String return_time = inMessage.nextLine();
                            String pdo = inMessage.nextLine();
                            String note = inMessage.nextLine();
                            String gos_num = inMessage.nextLine();
                            emergencyUpdate(id, departure_time, return_time, pdo, note, gos_num);
                            server.sendTableToAllPDOClients(false);
                            server.sendMsgToClientServer("#UPDATEUI");
                    }
                }
            } catch (NullPointerException | IOException | SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void emergencyUpdate(String id, String departure_time, String return_time, String pdo, String note, String gos_num) throws SQLException {
        System.out.println(departure_time);
        System.out.println(return_time);
        connection.createStatement().executeUpdate("UPDATE summary SET departure_time = '" + departure_time +
                "',gos_num='" + gos_num +
                "',note='" + note +
                "',return_time='" + return_time +
                "',pdo='" + pdo +
                "' WHERE id='" + id + "'"
        );
    }


    private void getArchive() throws SQLException, IOException {
        ArrayList<ArchiveTable> arrayList = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM archive");
        while (rs.next()) {
            arrayList.add(new ArchiveTable(
                    rs.getString("id"),
                    rs.getString("old_id"),
                    rs.getString("fio"),
                    rs.getString("departure_time"),
                    rs.getString("pdo"),
                    rs.getString("note"),
                    rs.getString("gos_num"),
                    rs.getString("return_time")));
        }
        objectOutputStream.writeObject(arrayList);
        objectOutputStream.flush();
    }

    public void deleteVehicle(String reg_num) throws SQLException {
        connection.createStatement().executeUpdate("DELETE FROM car_list WHERE reg_num='" + reg_num + "'");
    }

    private void addVehicle(String reg_num, String state) throws SQLException {
        connection.createStatement().executeUpdate("INSERT INTO car_list(reg_num,car_state) VALUES('" + reg_num + "'," +
                "'" + state + "')");
    }

    public void close() throws IOException {
        running = false;
        clientSocket.close();
        server.removeClient(key);
        clients_count--;
        objectOutputStream.close();
    }

//    public void resetData() throws SQLException {
//        connection.createStatement().executeUpdate("TRUNCATE TABLE summary");
//    }

    public void resetVehicleState() throws SQLException {
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state = 1");
    }

    private void sendRegNumRecords() throws SQLException, IOException {
        ArrayList<VehicleTable> arrayList = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM car_list");
        while (rs.next()) {
            arrayList.add(new VehicleTable(
                            rs.getString("reg_num"),
                            rs.getInt("car_state") == 1 ? "Свободна" :
                                    rs.getInt("car_state") == 0 ? "Занята" : "На обслуживании"
                            //rs.getString("car_state")
                    )
            );
        }
        System.out.println(arrayList);
        objectOutputStream.writeObject(arrayList);
        objectOutputStream.flush();
    }

    public void updateRegNum(String reg_num, String state) throws SQLException {
        System.out.println(reg_num);
        System.out.println(state);
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state =" + state + " WHERE reg_num='" + reg_num + "'");
    }

    public void updateRecording(String id, String reg_num, String departure_time, String pdo) throws SQLException {
        System.out.println(reg_num);
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state = 0 WHERE reg_num='" + reg_num + "'");
        connection.createStatement().executeUpdate("UPDATE summary SET departure_time = '" + departure_time +
                "',gos_num='" + reg_num +
                "',pdo='" + pdo +
                "' WHERE id='" + id + "'"
        );
    }

    public void getAllRecords() throws SQLException, IOException {
        ArrayList<SummaryTable> arrayList = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM summary");
        while (rs.next()) {
            arrayList.add(new SummaryTable(
                    rs.getString("id"),
                    rs.getString("fio"),
                    rs.getString("departure_time"),
                    rs.getString("pdo"),
                    rs.getString("note"),
                    rs.getString("gos_num"),
                    rs.getString("return_time")));
        }
        objectOutputStream.writeObject(arrayList);
        objectOutputStream.flush();
    }


    public void sendTable(boolean notify) throws IOException {
        objectOutputStream.writeBoolean(notify);
        objectOutputStream.flush();

        ArrayList<String> carList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT reg_num FROM car_list where car_state = 1");
            while (rs.next()) {
                carList.add(rs.getString("reg_num"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println(carList);
        objectOutputStream.writeObject(carList);
        objectOutputStream.flush();

        ArrayList<SummaryTable> arrayList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT*FROM summary where pdo = 'На согласовании'");
            while (rs.next()) {
                arrayList.add(new SummaryTable(
                        rs.getString("id"),
                        rs.getString("fio"),
                        rs.getString("departure_time"),
                        rs.getString("pdo"),
                        rs.getString("note"),
                        rs.getString("gos_num"),
                        rs.getString("return_time")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        objectOutputStream.writeObject(arrayList);
        objectOutputStream.flush();
    }
}