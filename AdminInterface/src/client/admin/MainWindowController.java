package client.admin;

import javafx.application.Platform;
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
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

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
    private final ObservableList<String> optionsList = FXCollections.observableArrayList("Accepted", "Refused", "On approval");

    public void initClient() throws IOException, InterruptedException {
        String[] serverParams = getSettings();
        serverHost = serverParams[0];
        serverPort = Integer.parseInt(serverParams[1]);

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
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Connection error!", "Error while connecting " +
                    "to server, check your settings or contact administrator to know about server status");
            return;
        }
        new Thread(() -> {
            try {
                while (running) {
                    try {
                        carList = (ArrayList<String>) objectInputStream.readObject();
                        pendingApprovalList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    } catch (SocketException | EOFException ex) {
                        System.out.println("socket was closed while listening(it's ok)");
                        //return;
                    }
                    if (sqlQueryEmpty) {
                        setTableData();
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
        alert.setTitle("Clear database data");
        alert.setHeaderText("All data from database will be COMPLETELY DELETED!");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            sendMsg("#TRUNCATE");
        }
    }

    public void resetVehicleState() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset vehicle states");
        alert.setHeaderText("All vehicle states WILL BE SET AS FREE");
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
        gosNum.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableList(carList)));
        note.setCellFactory(TextFieldTableCell.forTableColumn());
        arriveTime.setCellFactory(TextFieldTableCell.forTableColumn());
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
                        final Button btn = new Button("Submit");
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
                            setTableData();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        pendingApprovalTable.setEditable(true);
    }

    public void setTableData() {
        Platform.runLater(() -> pendingApprovalTable.getItems().clear());
        Platform.runLater(() ->
                pendingApprovalTable.setItems(FXCollections.observableArrayList(pendingApprovalList)));
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        outMessage.println("##session##end##");
        outMessage.flush();
        outMessage.close();
        objectInputStream.close();
        clientSocket.close();
        exit(0);
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
            System.out.println("NIGGER");
        }
        objectInputStream = null;
        init();
    }

    public static String[] getSettings() throws IOException {
        File file = new File("settings.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        String[] serverParams = new String[2];
        Scanner sc = new Scanner(file);
        if (sc.hasNext()) {
            serverParams[0] = sc.nextLine();
        }
        if (sc.hasNext()) {
            serverParams[1] = sc.nextLine();
        }
        sc.close();
        return serverParams;
    }

    public void init() {
        sqlQueryEmpty = true;
        running = true;
        try {
            initClient();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (outMessage != null) {
            sendMsg("#INITPDOTABLE");
        }
        initTable();
        setTableData();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }
}