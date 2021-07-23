package client.admin;

import com.company.SummaryTable;
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
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class SummaryController implements Initializable {
    @FXML
    public TableView<SummaryTable> summaryTable;
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
    public TableColumn<SummaryTable, Integer> gosNum;
    @FXML
    public TableColumn<SummaryTable, String> arriveTime;
    @FXML
    public TableColumn<SummaryTable, String> buttonsCol;
    @FXML
    public Pane headerPane;
    @FXML
    public Pane closeBtn;
    @FXML
    public Pane minimizeBtn;
    @FXML
    public Pane fullscreenBtn;
    @FXML
    public MenuItem settingsBtn;
    @FXML
    public MenuItem reconnectBtn;

    private ArrayList<SummaryTable> pendingApprovalList;
    private ArrayList<Integer> carList;
    private boolean running = true;
    private boolean sqlQueryEmpty;
    private String serverHost;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private String departureTimeCellData;
    private Integer gosNumCellData;
    private String PDOCellData;
    private String idSumCellData;
    private final ObservableList<String> optionsList = FXCollections.observableArrayList("Accepted", "Refused", "On approval");

    public void initClient() throws IOException, InterruptedException {
        carList = new ArrayList<>();
        pendingApprovalList = new ArrayList<>();
        gosNumCellData = 0;
        try {
            clientSocket = new Socket(serverHost, serverPort);
            outMessage = new PrintWriter(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            sqlQueryEmpty = true;
        } catch (IOException e) {
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Connection error!", "Error while connecting " +
                    "to server, check your settings or contact administrator to know about server status");
            return;
        }
        new Thread(() -> {
            try {
                while (running) {
                    try {
                        carList = (ArrayList<Integer>) objectInputStream.readObject();
                        pendingApprovalList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    } catch (SocketException ex) {
                        System.out.println("socket was closed while listening(it's ok)");
                    }
                    System.out.println(pendingApprovalList);
                    if (sqlQueryEmpty) {
                        updateTable();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMsg(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

    public void updateTable() {
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
                            sendMsg(gosNumCellData.toString());
                            sendMsg(departureTimeCellData);
                            sendMsg(PDOCellData);
                            sqlQueryEmpty = true;
                            updateTable();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        summaryTable.setEditable(true);
        Platform.runLater(() -> summaryTable.setItems(FXCollections.observableArrayList(pendingApprovalList)));

    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        outMessage.println("##session##end##");
        outMessage.flush();
        outMessage.close();
        objectInputStream.close();
        clientSocket.close();
    }

    public void getSettings() throws IOException {
        File file = new File("settings.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Scanner sc = new Scanner(file);
        if (sc.hasNext()) {
            serverHost = sc.nextLine();
        }
        if (sc.hasNext()) {
            serverPort = Integer.parseInt(sc.nextLine());
        }
        sc.close();
    }

    public void init() {
        sqlQueryEmpty = true;
        try {
            getSettings();
            initClient();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (outMessage != null) {
            sendMsg("#INITPDOTABLE");
        }
        updateTable();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();

        settingsBtn.setOnAction((ActionEvent) -> {
            init();
        });
    }
}