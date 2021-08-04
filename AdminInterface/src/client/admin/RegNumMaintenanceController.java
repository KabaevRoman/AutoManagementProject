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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import table.SummaryTable;
import table.VehicleTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class RegNumMaintenanceController implements Initializable {
    @FXML
    public TableView<VehicleTable> vehicleTable;
    @FXML
    public TableColumn<VehicleTable, String> regNum;
    @FXML
    public TableColumn<VehicleTable, String> vehicleState;
    @FXML
    public TableColumn<VehicleTable, String> buttonsCol;

    private String regNumCellData;
    private String vehicleStateCellData;


    private Socket clientSocket;
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private String serverHost;
    private int serverPort;
    private ArrayList<VehicleTable> arrayList;
    private final ObservableList<String> optionsList = FXCollections.observableArrayList("Free", "Busy", "On maintenance");

    public void updateTable() {
        regNum.setCellValueFactory(new PropertyValueFactory<>("regNum"));
        vehicleState.setCellValueFactory(new PropertyValueFactory<>("vehicleState"));

        vehicleState.setCellFactory(ComboBoxTableCell.forTableColumn(optionsList));

        vehicleState.setOnEditCommit(event -> {
            VehicleTable table = event.getRowValue();
            table.setVehicleState(event.getNewValue());
        });

        Callback<TableColumn<VehicleTable, String>, TableCell<VehicleTable, String>> cellFactoryBtn =
                new Callback<>() {
                    @Override
                    public TableCell<VehicleTable, String> call(final TableColumn<VehicleTable, String> param) {
                        final Button btn = new Button("Submit");
                        TableCell<VehicleTable, String> t = new TableCell<>() {
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
                            regNumCellData = regNum.getCellData(cellIndex);
                            vehicleStateCellData = vehicleState.getCellData(cellIndex);
                            sendMsg("#VEHICLESTATECHANGED");
                            sendMsg(regNumCellData);
                            switch (vehicleStateCellData) {
                                case "Free" -> sendMsg("1");
                                case "Busy" -> sendMsg("0");
                                case "On maintenance" -> sendMsg("2");
                            }
                            updateTable();
                        });
                        return t;
                    }
                };
        buttonsCol.setCellFactory(cellFactoryBtn);
        vehicleTable.setEditable(true);
        vehicleTable.setItems(FXCollections.observableArrayList(arrayList));
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
            sendMsg("#REGNUMMAINTENANCE");
            arrayList = (ArrayList<VehicleTable>) objectInputStream.readObject();
            System.out.println(arrayList);
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        updateTable();
    }
}
