package server;

import javafx.application.Platform;
import msg.UserMsg;
import msg.ServiceMsg;
import table.SummaryTable;
import ui.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserHandler implements Runnable {
    private Server server;
    private Connection connection;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    //private Scanner inMessage;
    private int clients_count = 0;
    private boolean running = true;
    private boolean saveToggled;
    private String username;
    public Controller controller;
    private int lock;
    private String id;


    public UserHandler(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Server server, DBConnect dbConnect, boolean saveToggled, Controller controller, String username) {
        try {
            this.username = username;
            this.controller = controller;
            this.saveToggled = saveToggled;
            this.server = server;
            this.objectOutputStream = objectOutputStream;
            this.objectInputStream = objectInputStream;
            //this.inMessage = new Scanner(socket.getInputStream());
            this.connection = dbConnect.getConnection();
            this.clients_count++;
            Platform.runLater(() -> server.controller.numOfClientsLabel.setText(String.valueOf(clients_count)));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (objectInputStream != null) {
                    ServiceMsg serviceMsg = (ServiceMsg) objectInputStream.readObject();
                    System.out.println(serviceMsg.command);
                    switch (serviceMsg.command) {
                        case "#INSERT": {
//                            String name =
//                            String note =
//                            String time =
                            insertRecording(serviceMsg.parameters.get("name"), serviceMsg.parameters.get("note"), serviceMsg.parameters.get("departureTime"), username);
                            server.sendTableToAllClients(true);
                            //System.out.println(userWaitingApproval());
                            break;
                        }
                        case "#FREEAUTO": {
                            changeAutoState();
                            saveReturnTime(serviceMsg.parameters.get("returnTime"));
                            if (saveToggled) {
                                saveToArchive();
                            }
                            server.lock.remove(username);
                            break;
                        }
                        case "#AUTH":
                            sendTable();
                            break;
                        case "#ARCHIVE":
                            if (saveToggled) {
                                saveToArchive();
                            }
                            server.lock.remove(username);
                            break;
                        case "#FORCEQUIT":
                            //removeClientFromDB();
                            //server.sendTableToAllClients();
                            break;
                        case "##session##end##":
                            this.close();
                            break;
                        case "#GOSNUM":
                            sendRegNum();
                            break;
                    }
                }
            } catch (NullPointerException | IOException | SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    //TODO проверить перезапуск сервера вроде с ним что-то не так
    private void saveReturnTime(String date) throws SQLException {
        System.out.println("UPDATE summary SET return_time =" + date + " WHERE id=" + username);
        try {
            connection.createStatement().executeUpdate(
                    "UPDATE summary SET return_time =" + "'" + date + "'" + " WHERE id=" + id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public String getRegNum() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery(
                "select gos_num from summary where id = " + "'" + id + "'");
        rs.next();
        return rs.getString("gos_num");
    }

    public void sendRegNum() throws SQLException, IOException {
        String gos_num = getRegNum();
        objectOutputStream.writeObject(gos_num);
    }

    public void saveToArchive() throws SQLException {
        connection.createStatement().executeUpdate(
                "INSERT INTO archive(old_id,fio,departure_time,car_status,return_time,pdo, note,gos_num)" +
                        "SELECT id,fio,departure_time,car_status,return_time,pdo, note,gos_num from summary WHERE id=" + id);
    }

    public void close() throws IOException {
        running = false;
        server.removeClient(username);
        clients_count--;
        Platform.runLater(() -> server.controller.numOfClientsLabel.setText(String.valueOf(clients_count)));
        objectInputStream.close();
        objectOutputStream.close();
    }

    public void insertRecording(String name, String note, String time, String username) throws SQLException {
        connection.createStatement().executeUpdate("INSERT INTO summary(fio,departure_time,pdo,note,username) VALUES(" +
                "'" + name + "'" + "," + "'" + time + "'" + "," + "'" + "На согласовании" + "','" + note + "','" + username + "')");
    }

    public void changeAutoState() throws SQLException {
        String gos_num = getRegNum();
        System.out.println(gos_num);
        connection.createStatement().executeUpdate("UPDATE car_list SET car_state = 1 WHERE reg_num='" + gos_num + "'");
    }

    public void sendTable() throws IOException {
        int numOfAvailableCars = 0;
        try {
            ResultSet rs = connection.createStatement().executeQuery("select count(*) from car_list where car_state = 1");
            while (rs.next()) {
                numOfAvailableCars = rs.getInt("count");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        String gos_num = "";
        if (lock == 1) {
            try {
                gos_num = getRegNum();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        UserMsg userMsg = new UserMsg(lock, numOfAvailableCars, gos_num);
        objectOutputStream.writeObject(userMsg);
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