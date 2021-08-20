package server;

import msg.AdminMsg;
import table.ArchiveTable;
import msg.ServiceMsg;
import table.SummaryTable;
import table.VehicleTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AdminHandler implements Runnable {
    private final Server server;
    private final Connection connection;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;

    private static int clients_count = 0;
    private boolean running = true;
    private String username;

    public AdminHandler(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Server server, DBConnect dbConnect, String username) throws IOException, SQLException {
        clients_count++;
        this.username = username;
        this.server = server;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.connection = dbConnect.getConnection();
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (objectInputStream != null) {
                    ServiceMsg serviceMsg = (ServiceMsg) objectInputStream.readObject();
                    System.out.println(serviceMsg.command);
                    switch (serviceMsg.command) {
                        case "#UPDATE": {
//                            String id = inMessage.nextLine();
//                            String gos_num = inMessage.nextLine();
//                            String departure_time = inMessage.nextLine();
//                            String pdo = inMessage.nextLine();
                            updateRecording(
                                    serviceMsg.parameters.get("id"),
                                    serviceMsg.parameters.get("gos_num"),
                                    serviceMsg.parameters.get("departure_time"),
                                    serviceMsg.parameters.get("pdo")
                            );
                            String username = getUsername(serviceMsg.parameters.get("id"));
                            if (serviceMsg.parameters.get("pdo").equals("Одобрено")) {
                                server.userClients.get(username).setLock(1);
                                server.lock.put(username, 1);
                            } else if (serviceMsg.parameters.get("pdo").equals("Отказ")) {
                                server.userClients.get(username).setLock(2);
                                server.lock.put(username, 2);
                            } else {
                                server.userClients.get(username).setLock(0);
                                server.lock.put(username, 0);
                            }
                            server.userClients.get(username).setId(serviceMsg.parameters.get("id"));
                            server.sendTableToAllClients(false);
//                            server.sendMsgToClientServer(pdo);
//                            server.sendMsgToClientServer(id);
                            break;
                        }
                        case "#INITPDOTABLE":
                            sendTable(false);
                            break;
                        case "#TRUNCATE": {
                            server.resetData();
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "#RESETVEHSTATE": {
                            resetVehicleState();
                            server.sendTableToAllClients(false);
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
                            updateRegNum(serviceMsg.parameters.get("gos_num"),
                                    serviceMsg.parameters.get("state")
                            );
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "#ADDVEHICLE": {
                            addVehicle(serviceMsg.parameters.get("gos_num"), serviceMsg.parameters.get("state"));
                            //server.sendTableToAllPDOClients(false); ломает взаимодействие с редактором
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "#DELETEVEHICLE": {
                            deleteVehicle(serviceMsg.parameters.get("gos_num"));
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "#REGNUMMAINTENANCECLOSE": {
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "##session##end##":
                            this.close();
                            break;
                        case "#EDIT":
                            emergencyUpdate(serviceMsg.parameters.get("id"), serviceMsg.parameters.get("departure_time"),
                                    serviceMsg.parameters.get("arrive_time"), serviceMsg.parameters.get("pdo"),
                                    serviceMsg.parameters.get("pdo"), serviceMsg.parameters.get("gos_num"));
                            server.sendTableToAllClients(false);
                    }
                }
            } catch (NullPointerException | IOException | SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void emergencyUpdate(String id, String departure_time, String return_time, String pdo, String note, String gos_num) throws SQLException {
        System.out.println(departure_time);
        System.out.println(return_time);
        if (return_time.equals("null") || return_time.equals("")) {
            connection.createStatement().executeUpdate("UPDATE summary SET departure_time = '" + departure_time +
                    "',gos_num='" + gos_num +
                    "',note='" + note +
                    "',return_time=NULL" +
                    ",pdo='" + pdo +
                    "' WHERE id='" + id + "'"
            );
        } else {
            connection.createStatement().executeUpdate("UPDATE summary SET departure_time = '" + departure_time +
                    "',gos_num='" + gos_num +
                    "',note='" + note +
                    "',return_time='" + return_time +
                    "',pdo='" + pdo +
                    "' WHERE id='" + id + "'"
            );
        }
    }


    public String getUsername(String id) throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT username from summary where id='" + id + "'");
        rs.next();
        return rs.getString("username");
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
        objectInputStream.close();
        objectOutputStream.close();
        server.removeClient(username);
        clients_count--;
    }


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
        AdminMsg adminmsg = new AdminMsg(notify, carList, arrayList);
        objectOutputStream.writeObject(adminmsg);
        objectOutputStream.flush();
    }
}