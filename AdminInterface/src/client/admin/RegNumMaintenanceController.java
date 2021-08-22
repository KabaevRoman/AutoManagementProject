package client.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import msg.AdminMsg;
import msg.ServiceMsg;
import msg.UserInfo;
import table.VehicleTable;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class    RegNumMaintenanceController implements Initializable {
    enum btnType {
        SUBMIT,
        DELETE
    }

    @FXML
    public TableView<VehicleTable> vehicleTable;
    @FXML
    public TableColumn<VehicleTable, String> regNum;
    @FXML
    public TableColumn<VehicleTable, String> vehicleState;
    @FXML
    public TableColumn<VehicleTable, String> buttonsCol;
    @FXML
    public TableColumn<VehicleTable, String> delButtonCol;
    @FXML
    public TextField regNumTextField;
    @FXML
    public ComboBox<String> stateBox;
    @FXML
    public Button addVehicleBtn;


    private String regNumCellData;
    private String vehicleStateCellData;


    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String serverHost;
    private int serverPort;
    private ArrayList<VehicleTable> arrayList;
    private final ObservableList<String> optionsList = FXCollections.observableArrayList(
            "Свободна", "Занята", "На ТО");
    private String username;
    private String password;

    public void addVehicle() throws IOException, ClassNotFoundException {
        ServiceMsg serviceMsg = new ServiceMsg();
        serviceMsg.command = "#ADDVEHICLE";
        serviceMsg.parameters.put("gos_num", regNumTextField.getText());
        String state = stateBox.getValue();

        switch (state) {
            case "Свободна":
                serviceMsg.parameters.put("state", "1");
                sendMsg(serviceMsg);
                break;
            case "Занята":
                serviceMsg.parameters.put("state", "0");
                sendMsg(serviceMsg);
                break;
            case "На ТО":
                serviceMsg.parameters.put("state", "2");
                sendMsg(serviceMsg);
                break;
        }
        //sendMsg(state);
        updateTableData();
    }

    public Callback<TableColumn<VehicleTable, String>, TableCell<VehicleTable, String>> formCellFactoryBtn(String btnName, btnType type) {
        return new Callback<>() {
            @Override
            public TableCell<VehicleTable, String> call(final TableColumn<VehicleTable, String> param) {
                final Button btn = new Button(btnName);
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
                switch (type) {
                    case SUBMIT:
                        btn.setOnAction(e -> {
                            int cellIndex = t.getTableRow().getIndex();
                            regNumCellData = regNum.getCellData(cellIndex);
                            vehicleStateCellData = vehicleState.getCellData(cellIndex);
                            //sendMsg("#VEHICLESTATECHANGED");
                            ServiceMsg serviceMsg = new ServiceMsg();
                            serviceMsg.command = "#VEHICLESTATECHANGED";
                            serviceMsg.parameters.put("gos_num", regNumCellData);
                            switch (vehicleStateCellData) {
                                case "Свободна":
                                    serviceMsg.parameters.put("state", "1");
                                    try {
                                        sendMsg(serviceMsg);
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                    break;
                                case "Занята":
                                    serviceMsg.parameters.put("state", "0");
                                    try {
                                        sendMsg(serviceMsg);
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                    break;
                                case "На ТО":
                                    serviceMsg.parameters.put("state", "2");
                                    try {
                                        sendMsg(serviceMsg);
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                    break;
                            }
                            try {
                                updateTableData();
                            } catch (IOException | ClassNotFoundException ioException) {
                                ioException.printStackTrace();
                            }
                        });
                        break;
                    case DELETE:
                        btn.setOnAction(e -> {
                            int cellIndex = t.getTableRow().getIndex();
                            regNumCellData = regNum.getCellData(cellIndex);
                            ServiceMsg serviceMsg = new ServiceMsg();
                            serviceMsg.command = "#DELETEVEHICLE";
                            serviceMsg.parameters.put("gos_num", regNumCellData);
                            try {
                                sendMsg(serviceMsg);
                                updateTableData();
                            } catch (IOException | ClassNotFoundException ioException) {
                                ioException.printStackTrace();
                            }
                        });
                        break;
                }
                return t;
            }
        };
    }

    public void formTable() {
        regNum.setCellValueFactory(new PropertyValueFactory<>("regNum"));
        vehicleState.setCellValueFactory(new PropertyValueFactory<>("vehicleState"));
        vehicleState.setCellFactory(ComboBoxTableCell.forTableColumn(optionsList));
        vehicleState.setOnEditCommit(event -> {
            VehicleTable table = event.getRowValue();
            table.setVehicleState(event.getNewValue());
        });
        stateBox.getItems().setAll(optionsList);
        buttonsCol.setCellFactory(formCellFactoryBtn("Отправить", btnType.SUBMIT));
        delButtonCol.setCellFactory(formCellFactoryBtn("Удалить", btnType.DELETE));
        vehicleTable.setEditable(true);
    }

    public void updateTableData() throws IOException, ClassNotFoundException {
        vehicleTable.getItems().clear();
        sendMsg("#REGNUMMAINTENANCE");
        arrayList = (ArrayList<VehicleTable>) objectInputStream.readObject();
        System.out.println(arrayList);
        vehicleTable.setItems(FXCollections.observableArrayList(arrayList));
    }

    public void updContent() throws IOException, ClassNotFoundException {
        vehicleTable.getItems().clear();
        arrayList = (ArrayList<VehicleTable>) objectInputStream.readObject();
        System.out.println(arrayList);
        vehicleTable.setItems(FXCollections.observableArrayList(arrayList));
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

    public void setSettings() throws IOException {
        Settings settings = new Settings();
        settings.getSettings();
        serverPort = settings.getServerPort();
        serverHost = settings.getServerHost();
        username = settings.getUsername();
        password = settings.getPassword();
    }

    public void initClient() throws IOException {
        clientSocket = new Socket(serverHost, serverPort);
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        UserInfo userInfo = new UserInfo(username, password, true);
        objectOutputStream.writeObject(userInfo);
        objectOutputStream.flush();
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        try {
            sendMsg("#REGNUMMAINTENANCECLOSE");
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "##session##end##";
            serviceMsg.parameters.put("status", "#MAINTENANCE");
            sendMsg(serviceMsg);
            objectOutputStream.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setSettings();
            initClient();
            formTable();
            updateTableData();
        } catch (IOException | NullPointerException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
