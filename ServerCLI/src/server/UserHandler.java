package server;

import msg.ScreenLock;
import msg.UserMsg;
import msg.ServiceMsg;
import table.SummaryTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserHandler implements Runnable {
    private Server server;
    private Connection connection;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private boolean running = true;
    private boolean saveToggled;
    private String username;
    private String gos_num;
    private ScreenLock lock;
    private String id;


    public UserHandler(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Server server,
                       DBConnect dbConnect, boolean saveToggled, String username) {
        try {
            this.username = username;
            this.saveToggled = saveToggled;
            this.server = server;
            this.objectOutputStream = objectOutputStream;
            this.objectInputStream = objectInputStream;
            this.connection = dbConnect.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setLock(ScreenLock lock) {
        this.lock = lock;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGos_num(String gos_num) {
        this.gos_num = gos_num;
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
                            insertRecording(
                                    serviceMsg.parameters.get("name"),
                                    serviceMsg.parameters.get("note"),
                                    serviceMsg.parameters.get("departureTime"),
                                    username
                            );
                            server.sendTableToAllClients(true);
                            break;
                        }
                        case "#INITUSERTABLE":
                            sendTable();
                            break;
                        case "#FREEAUTO": {
                            changeAutoState();
                            saveReturnTime(serviceMsg.parameters.get("returnTime"));
                            if (saveToggled) {
                                saveToArchive();
                            }
                            server.userMetaData.remove(username);
                            server.sendTableToAllClients(false);
                            break;
                        }
                        case "#ARCHIVE":
                            if (saveToggled) {
                                saveToArchive();
                            }
                            server.userMetaData.remove(username);
                            server.sendTableToAllClients(false);
                            break;
                        case "#FORCEQUIT":
                            break;
                        case "##session##end##":
                            this.close();
                            break;
//                        case "#GOSNUM":
//                            sendRegNum();
//                            break;
                    }
                }
            } catch (NullPointerException | SQLException | ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveReturnTime(String date) throws SQLException {
        System.out.println("UPDATE summary SET return_time =" + date + " WHERE id=" + id);
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

    public void saveToArchive() throws SQLException {
        connection.createStatement().executeUpdate(
                "INSERT INTO archive(old_id,fio,departure_time,car_status,return_time,pdo, note,gos_num)" +
                        "SELECT id,fio,departure_time,car_status,return_time,pdo, note,gos_num from summary WHERE id=" + id);
    }

    public void close() throws IOException, SQLException {
        connection.close();
        running = false;
        server.removeClient(username);
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
            rs.next();
            numOfAvailableCars = rs.getInt("count");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
        UserMsg userMsg = new UserMsg(lock, numOfAvailableCars, gos_num, arrayList);
        objectOutputStream.writeObject(userMsg);
        objectOutputStream.flush();
    }
}