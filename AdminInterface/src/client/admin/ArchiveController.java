package client.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import msg.ServiceMsg;
import msg.UserInfo;
import table.ArchiveTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ArchiveController implements Initializable {
    @FXML
    public TableView<ArchiveTable> archiveTable;
    @FXML
    public TableColumn<ArchiveTable, String> id;
    @FXML
    public TableColumn<ArchiveTable, String> idSum;
    @FXML
    public TableColumn<ArchiveTable, String> name;
    @FXML
    public TableColumn<ArchiveTable, String> departureTime;
    @FXML
    public TableColumn<ArchiveTable, String> PDO;
    @FXML
    public TableColumn<ArchiveTable, String> note;
    @FXML
    public TableColumn<ArchiveTable, String> gosNum;
    @FXML
    public TableColumn<ArchiveTable, String> arriveTime;

    private Socket clientSocket;
    //private PrintWriter outMessage;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String serverHost;
    private int serverPort;
    private ArrayList<ArchiveTable> arrayList;
    private String username;
    private String password;


    public void initTable() {
        idSum.setCellValueFactory(new PropertyValueFactory<>("id"));
        idSum.setCellValueFactory(new PropertyValueFactory<>("idSum"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        departureTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        PDO.setCellValueFactory(new PropertyValueFactory<>("PDO"));
        note.setCellValueFactory(new PropertyValueFactory<>("note"));
        gosNum.setCellValueFactory(new PropertyValueFactory<>("gosNum"));
        arriveTime.setCellValueFactory(new PropertyValueFactory<>("arriveTime"));
    }


    public void sendMsg(String command) throws IOException {
        ServiceMsg serviceMsg = new ServiceMsg();
        serviceMsg.command = command;
        objectOutputStream.writeObject(serviceMsg);
        objectOutputStream.flush();
    }


    public void getSettings() throws IOException {
        Settings settings = new Settings();
        settings.getSettings();
        serverPort = settings.getServerPort();
        serverHost = settings.getServerHost();
        username = settings.getUsername();
        password = settings.getPassword();
    }

    public void initClient() throws IOException {
        getSettings();
        clientSocket = new Socket(serverHost, serverPort);
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        UserInfo userInfo = new UserInfo(username, password, true);
        objectOutputStream.writeObject(userInfo);
        objectOutputStream.flush();
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        ServiceMsg serviceMsg = new ServiceMsg();
        serviceMsg.command = "##session##end##";
        serviceMsg.parameters.put("status", "#MAINTENANCE");
        sendMsg(serviceMsg);
        objectOutputStream.close();
        objectInputStream.close();
        clientSocket.close();
    }

    public void sendMsg(ServiceMsg serviceMsg) throws IOException {
        objectOutputStream.writeObject(serviceMsg);
        objectOutputStream.flush();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initClient();
            sendMsg("#GETARCHIVE");
            arrayList = (ArrayList<ArchiveTable>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        initTable();
        archiveTable.setItems(FXCollections.observableArrayList(arrayList));
    }
}
