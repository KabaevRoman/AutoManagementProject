package client.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.util.Callback;
import msg.AdminMsg;
import msg.ServiceMsg;
import msg.UserInfo;
import table.SummaryTable;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.lang.System.exit;

public class MainWindowController implements Initializable {
    @FXML
    public TableView<SummaryTable> pendingApprovalTable;
    @FXML
    public TableColumn<SummaryTable, String> idSum;
    @FXML
    public TableColumn<SummaryTable, String> name;
    @FXML
    public TableColumn<SummaryTable, String> departureTime;
    @FXML
    public TableColumn<SummaryTable, String> PDO;
    @FXML
    public TableColumn<SummaryTable, String> note;
    @FXML
    public TableColumn<SummaryTable, String> gosNum;
    @FXML
    public TableColumn<SummaryTable, String> arriveTime;
    @FXML
    public TableColumn<SummaryTable, String> buttonsCol;
    @FXML
    public Pane headerPane;
    @FXML
    public MenuItem settingsBtn;
    @FXML
    public MenuItem reconnectBtn;
    @FXML
    public MenuItem resetDatabaseBtn;
    @FXML
    public MenuItem editDatabaseBtn;
    @FXML
    public MenuItem editRegNumBtn;
    @FXML
    public MenuItem resetVehicleStateBtn;
    @FXML
    public MenuItem archiveBtn;
    @FXML
    public MenuItem updateBtn;


    private ArrayList<SummaryTable> pendingApprovalList;
    private ArrayList<String> carList;
    private boolean running;
    private boolean sqlQueryEmpty;
    private String serverHost;
    private int serverPort;
    private Socket clientSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String departureTimeCellData;
    private String gosNumCellData;
    private String PDOCellData;
    private String idSumCellData;
    private String username;
    private String password;
    private final ObservableList<String> optionsList = FXCollections.observableArrayList("Одобрено", "Отказ", "На согласовании");

    public void initClient() throws IOException, InterruptedException {
        carList = new ArrayList<>();
        pendingApprovalList = new ArrayList<>();
        gosNumCellData = "";
        try {
            clientSocket = new Socket(serverHost, serverPort);
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            UserInfo userInfo = new UserInfo(username, password, false);
            objectOutputStream.writeObject(userInfo);
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            sqlQueryEmpty = true;
        } catch (IOException e) {
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                    "Ошибка во время подключения к серверу, проверьте настройки подключения или обратитесь к " +
                            "администратору чтобы узнать статус сервера");
            return;
        }
        //TODO кнопка переподключения сломалась к хуям
        new Thread(() -> {
            try {
                while (running) {
                    try {
                        AdminMsg adminMsg = (AdminMsg) objectInputStream.readObject();
                        if (adminMsg.notify) {
                            new AudioClip(Objects.requireNonNull(MainWindowController.class.getResource("/notification.wav")).toString()).play();
                        }
                        carList = adminMsg.carList;
                        pendingApprovalList = adminMsg.arrayList;
                    } catch (SocketException | EOFException ex) {
                        Platform.runLater(() -> ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                                "Отключены от сервера"));
                        System.out.println("socket was closed while listening(it's ok)");
                        break;
                    }
                    if (sqlQueryEmpty) {
                        updateTableData();
                    }
                }
                System.out.println("Thread stopped");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMsg(ServiceMsg serviceMsg) throws IOException {
        objectOutputStream.writeObject(serviceMsg);
        objectOutputStream.flush();
    }

    public void sendMsg(String command) throws IOException {
        ServiceMsg serviceMsg = new ServiceMsg();
        serviceMsg.command = command;
        objectOutputStream.writeObject(serviceMsg);
        objectOutputStream.flush();
    }

    public void truncateDatabase() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Очистить базу данных");
        alert.setHeaderText("Все данные из таблицы с запросами будут ПОЛНОСТЬЮ УДАЛЕНЫ");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            sendMsg("#TRUNCATE");
        }
    }

    public void resetVehicleState() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Освободить статусы авто");
        alert.setHeaderText("Статус всех машин будет установлен на свободный");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            sendMsg("#RESETVEHSTATE");
        }
    }

    public void initTable() {
        idSum.setCellValueFactory(new PropertyValueFactory<>("idSum"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        departureTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        PDO.setCellValueFactory(new PropertyValueFactory<>("PDO"));
        note.setCellValueFactory(new PropertyValueFactory<>("note"));
        gosNum.setCellValueFactory(new PropertyValueFactory<>("gosNum"));
        arriveTime.setCellValueFactory(new PropertyValueFactory<>("arriveTime"));
        PDO.setCellFactory(ComboBoxTableCell.forTableColumn(optionsList));
        note.setCellFactory(TextFieldTableCell.forTableColumn());
        departureTime.setCellFactory(TextFieldTableCell.forTableColumn());

        gosNum.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setGosNum(event.getNewValue());
            sqlQueryEmpty = false;
        });

        departureTime.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setDepartureTime(event.getNewValue());
            sqlQueryEmpty = false;
        });

        PDO.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setPDO(event.getNewValue());
            sqlQueryEmpty = false;
        });

        Callback<TableColumn<SummaryTable, String>, TableCell<SummaryTable, String>> cellFactoryBtn =
                new Callback<>() {
                    @Override
                    public TableCell<SummaryTable, String> call(final TableColumn<SummaryTable, String> param) {
                        final Button btn = new Button("Отправить");
                        TableCell<SummaryTable, String> t = new TableCell<>() {
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(btn);
                                }
                                setText(null);
                            }
                        };
                        btn.setOnAction(e -> {
                            int cellIndex = t.getTableRow().getIndex();
                            idSumCellData = idSum.getCellData(cellIndex);
                            gosNumCellData = gosNum.getCellData(cellIndex);
                            departureTimeCellData = departureTime.getCellData(cellIndex);
                            PDOCellData = PDO.getCellData(cellIndex);
                            ServiceMsg serviceMsg = new ServiceMsg();
                            serviceMsg.command = "#UPDATE";
                            serviceMsg.parameters.put("id", idSumCellData);
                            serviceMsg.parameters.put("gos_num", gosNumCellData);
                            serviceMsg.parameters.put("departure_time", departureTimeCellData);
                            serviceMsg.parameters.put("pdo", PDOCellData);
                            try {
                                sendMsg(serviceMsg);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                            sqlQueryEmpty = true;
                            updateTableData();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        pendingApprovalTable.setEditable(true);
    }

    public void updateTableData() {
        Platform.runLater(() -> pendingApprovalTable.getItems().clear());
        Platform.runLater(() ->
                pendingApprovalTable.setItems(FXCollections.observableArrayList(pendingApprovalList)));
        Platform.runLater(() ->
                gosNum.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableList(carList))));
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        try {
            sendMsg("##session##end##");
            objectOutputStream.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        exit(0);
    }

    public void getSettings() throws IOException {
        Settings settings = new Settings();
        settings.getSettings();
        serverPort = settings.getServerPort();
        serverHost = settings.getServerHost();
        username = settings.getUsername();
        password = settings.getPassword();
    }

    public void reconnect() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        try {
            sendMsg("##session##end##");
            objectOutputStream.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            System.out.println("null pointer in reconnect");
        }
        objectInputStream = null;
        init();
    }

    public void init() {
        sqlQueryEmpty = true;
        running = true;
        try {
            getSettings();
            initClient();
            sendMsg("#INITPDOTABLE");
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
        initTable();
        updateTableData();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }
}