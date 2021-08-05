package client.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import table.SummaryTable;

import java.io.IOException;
import java.io.ObjectInputStream;
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

    private Socket clientSocket;
    private PrintWriter outMessage;
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
                            updateTable();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        summaryTable.setEditable(true);
        summaryTable.setItems(FXCollections.observableArrayList(arrayList));
    }

    public void sendMsg(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

    public void initClient() throws IOException {
        String[] serverParams = SummaryController.getSettings();
        serverHost = serverParams[0];
        serverPort = Integer.parseInt(serverParams[1]);
        clientSocket = new Socket(serverHost, serverPort);
        outMessage = new PrintWriter(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        outMessage.println("##session##end##");
        outMessage.flush();
        outMessage.close();
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
