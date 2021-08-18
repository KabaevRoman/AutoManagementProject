package client.admin;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.media.AudioClip;
import table.SummaryTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.*;

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
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private String departureTimeCellData;
    private String gosNumCellData;
    private String PDOCellData;
    private String idSumCellData;
    private Boolean connected;
    private final ObservableList<String> optionsList = FXCollections.observableArrayList("Одобрено", "Отказ", "На согласовании");

    public void initClient() throws IOException, InterruptedException {
        carList = new ArrayList<>();
        pendingApprovalList = new ArrayList<>();
        gosNumCellData = "";
        try {
            clientSocket = new Socket(serverHost, serverPort);
            outMessage = new PrintWriter(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            sqlQueryEmpty = true;
            connected = true;
        } catch (IOException e) {
            connected = false;
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                    "Ошибка во время подключения к серверу, проверьте настройки подключения или обратитесь к " +
                            "администратору чтобы узнать статус сервера ");
            return;
        }
        new Thread(() -> {
            try {
                while (running) {
                    try {
                        if (objectInputStream.readBoolean()) {
                            new AudioClip(Objects.requireNonNull(MainWindowController.class.getResource("/notification.wav")).toString()).play();
                        }
                        carList = (ArrayList<String>) objectInputStream.readObject();
                        pendingApprovalList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    } catch (SocketException | EOFException ex) {
                        Platform.runLater(() -> ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                                "Вы были отключены от сервера"));
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

    public void sendMsg(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

    public void truncateDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Очистить базу данных");
        alert.setHeaderText("Все данные из таблицы с запросами будут ПОЛНОСТЬЮ УДАЛЕНЫ");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            sendMsg("#TRUNCATE");
        }
    }

    public void resetVehicleState() {
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
                            sendMsg("#UPDATE");
                            sendMsg(idSumCellData);
                            sendMsg(gosNumCellData);
                            sendMsg(departureTimeCellData);
                            sendMsg(PDOCellData);
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
        Platform.runLater(() -> gosNum.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableList(carList))));
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        try {
            outMessage.println("##session##end##");
            outMessage.flush();
            outMessage.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        exit(0);
    }

    public void setSettings() throws IOException {
        Settings settings = new Settings();
        settings.getSettings();
        serverPort = settings.getServerPort();
        serverHost = settings.getServerHost();
    }

    public void reconnect() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        try {
            outMessage.println("##session##end##");
            outMessage.flush();
            outMessage.close();
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
            setSettings();
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