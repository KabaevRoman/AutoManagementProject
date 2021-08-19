package server.client;

import javafx.application.Platform;
import server.DBConnect;
import table.InterfaceData;
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
    private boolean saveToggled;
    private String username;
    private int lock;


    public ClientHandler(Socket socket, Server server, DBConnect dbConnect, boolean saveToggled) {
        try {
            this.saveToggled = saveToggled;
            clients_count++;
            Platform.runLater(() -> server.controller.numOfClientsLabel.setText(String.valueOf(clients_count)));
            this.server = server;
            this.clientSocket = socket;
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
            this.connection = dbConnect.getConnection();
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (inMessage != null) {
                    if (inMessage.hasNext()) {
                        String clientMessage = inMessage.nextLine();
                        System.out.println(clientMessage);
                        switch (clientMessage) {
                            case "#INSERT": {
                                String name = inMessage.nextLine();
                                String note = inMessage.nextLine();
                                String time = inMessage.nextLine();
                                insertRecording(name, note, time, username);
                                server.sendTableToAllClients();
                                server.sendMsgToPDOServer("#INSERT");
                                //System.out.println(userWaitingApproval());
                                break;
                            }
                            case "#FREEAUTO": {
                                changeAutoState();
                                String date = inMessage.nextLine();
                                saveReturnTime(date);
                                if (saveToggled)
                                    saveToArchive();
                                break;
                            }
                            case "#AUTH":
                                username = inMessage.nextLine();
                                String password = inMessage.nextLine();
                                test();
                                if (userValidation(username, password)) {
                                    if(userWaitingApproval()){
                                        sendTable();
                                    }
                                    sendTable();
                                }else{
                                }
                                break;
                            case "##session##end##":
                                this.close();
                                break;
                            case "#ARCHIVE":
                                if (saveToggled) {
                                    saveToArchive();
                                }
                                break;
                            case "#FORCEQUIT":
                                removeClientFromDB();
                                server.sendTableToAllClients();
                                server.sendMsgToPDOServer("#UPDATECONTENT");
                                break;
                            case "#GOSNUM":
                                sendRegNum();
                                break;
                        }
                    }
                }
            } catch (NullPointerException | IOException | SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean userValidation(String username, String password) throws SQLException {
        //System.out.println(username + " " + password);
        boolean valid = false;
        ResultSet rs = connection.createStatement().executeQuery("select exists(select 1 from " +
                "users where username='" + username + "'and password='" + password + "')");
        while (rs.next()) {
            valid = rs.getBoolean("exists");
        }
        System.out.println(valid);
        return valid;
    }

    public int userStatus(){
        ResultSet rs = connection.createStatement()
    }

    public boolean userWaitingApproval() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("select exists(select 1 from " +
                "summary where username='" + username + "'and pdo='На согласовании')");
        rs.next();
        return rs.getBoolean("exists");
    }

    private void removeClientFromDB() throws SQLException {
        connection.createStatement().executeUpdate("DELETE FROM summary WHERE username=" + username);
    }

    //TODO проверить перезапуск сервера вроде с ним что-то не так
    private void saveReturnTime(String date) throws SQLException {
        System.out.println("UPDATE summary SET return_time =" + date + " WHERE id=" + username);
        try {
            connection.createStatement().executeUpdate(
                    "UPDATE summary SET return_time =" + "'" + date + "'" + " WHERE username=" + username);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void test() throws SQLException {
        connection.createStatement().executeUpdate(
                "UPDATE summary SET return_time =" + "'" + "22:22" + "'" + " WHERE username='" + username + "'");
    }

    public String getRegNum() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery(
                "select gos_num from summary where username = " + "'" + username + "'");
        String gos_num = null;
        while (rs.next()) {
            gos_num = rs.getString("gos_num");
        }
        return gos_num;
    }

    public void sendRegNum() throws SQLException, IOException {
        String gos_num = getRegNum();
        objectOutputStream.writeObject(gos_num);
    }

    public void saveToArchive() throws SQLException {
        connection.createStatement().executeUpdate(
                "INSERT INTO archive(old_id,fio,departure_time,car_status,return_time,pdo, note,gos_num)" +
                        "SELECT id,fio,departure_time,car_status,return_time,pdo, note,gos_num from summary WHERE username=" + username);
    }

    public void close() throws IOException {
        running = false;
        clientSocket.close();
        server.removeClient(username);
        clients_count--;
        Platform.runLater(() -> server.controller.numOfClientsLabel.setText(String.valueOf(clients_count)));
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
        try {
            gos_num = getRegNum();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        InterfaceData interfaceData = new InterfaceData(lock, numOfAvailableCars, gos_num);
        objectOutputStream.writeObject(interfaceData);
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