package client.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import msg.ServiceMsg;
import table.SummaryTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EditingController implements Initializable {
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
    public TableColumn<SummaryTable, String> gosNum;
    @FXML
    public TableColumn<SummaryTable, String> arriveTime;
    @FXML
    public TableColumn<SummaryTable, String> buttonsCol;

    private String departureTimeCellData;
    private String gosNumCellData;
    private String PDOCellData;
    private String idSumCellData;
    private String noteCellData;
    private String arriveTimeCellData;

    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String serverHost;
    private int serverPort;
    private ArrayList<SummaryTable> arrayList;


    public void updateTable() {
        idSum.setCellValueFactory(new PropertyValueFactory<>("idSum"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        departureTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        PDO.setCellValueFactory(new PropertyValueFactory<>("PDO"));
        note.setCellValueFactory(new PropertyValueFactory<>("note"));
        gosNum.setCellValueFactory(new PropertyValueFactory<>("gosNum"));
        arriveTime.setCellValueFactory(new PropertyValueFactory<>("arriveTime"));

        PDO.setCellFactory(TextFieldTableCell.forTableColumn());
        gosNum.setCellFactory(TextFieldTableCell.forTableColumn());
        note.setCellFactory(TextFieldTableCell.forTableColumn());
        arriveTime.setCellFactory(TextFieldTableCell.forTableColumn());
        departureTime.setCellFactory(TextFieldTableCell.forTableColumn());

        gosNum.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setGosNum(event.getNewValue());
        });

        departureTime.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setDepartureTime(event.getNewValue());
        });

        PDO.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setPDO(event.getNewValue());
        });
        arriveTime.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setArriveTime(event.getNewValue());
        });
        note.setOnEditCommit(event -> {
            SummaryTable table = event.getRowValue();
            table.setNote(event.getNewValue());
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
                            departureTimeCellData = departureTime.getCellData(cellIndex);
                            noteCellData = note.getCellData(cellIndex);
                            gosNumCellData = gosNum.getCellData(cellIndex);
                            arriveTimeCellData = arriveTime.getCellData(cellIndex);
                            PDOCellData = PDO.getCellData(cellIndex);
                            System.out.println(departureTimeCellData + gosNumCellData + PDOCellData);
                            ServiceMsg serviceMsg = new ServiceMsg();
                            serviceMsg.command = "#EDIT";
                            serviceMsg.parameters.put("id", idSumCellData);
                            serviceMsg.parameters.put("departure_time", departureTimeCellData);
                            serviceMsg.parameters.put("arrive_time", arriveTimeCellData);
                            serviceMsg.parameters.put("pdo", PDOCellData);
                            serviceMsg.parameters.put("note", noteCellData);
                            serviceMsg.parameters.put("gos_num", gosNumCellData);
                            try {
                                sendMsg(serviceMsg);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                            updateTable();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        summaryTable.setEditable(true);
        summaryTable.setItems(FXCollections.observableArrayList(arrayList));
    }

    public void getSettings() throws IOException {
        Settings settings = new Settings();
        settings.getSettings();
        serverPort = settings.getServerPort();
        serverHost = settings.getServerHost();
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

    public void initClient() throws IOException {
        getSettings();
        clientSocket = new Socket(serverHost, serverPort);
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        sendMsg("##session##end##");
        objectOutputStream.close();
        objectInputStream.close();
        clientSocket.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initClient();
            sendMsg("#DBMAINTENANCE");
            arrayList = (ArrayList<SummaryTable>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        updateTable();
    }
}


//    public void startTimer() throws ParseException {// прикольный но ненужный кусок кода
//        System.out.println("TimerStarted");
//        Timer timer = new Timer();
//        int latestRequestIndex = pendingApprovalList.size() - 1;
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date();
//        String expireTime = pendingApprovalTable.getItems().get(latestRequestIndex).getDepartureTime();
//        String id = pendingApprovalTable.getItems().get(latestRequestIndex).getIdSum();
//        String currentDate = formatter.format(date);
//        Date d = dateFormat.parse(currentDate + " " + expireTime);
//        System.out.println(d);
//
//        // продумать кейсы когда люди выходят досрочно
//        // подумать если пользователь жмет реконнект считать это форс квитом
//        // подумать как работать с отказами в плане отменять оповещение
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("run started");
//                new AudioClip(Objects.requireNonNull(MainWindowController.class.getResource("/notification.wav")).toString()).play();
//                Platform.runLater(() -> ErrorHandler.errorAlert(Alert.AlertType.INFORMATION, "Оповещение", "Время отправления пользователя с id:" + id));
//            }
//        }, d);
//    }
