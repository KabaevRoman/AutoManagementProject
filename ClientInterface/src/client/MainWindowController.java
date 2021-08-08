package client;

import table.SummaryTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.util.converter.DateTimeStringConverter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

import static java.lang.System.exit;

public class MainWindowController implements Initializable {
    public TableView<SummaryTable> summaryTable;
    public TableColumn<SummaryTable, String> idSum;
    public TableColumn<SummaryTable, String> name;
    public TableColumn<SummaryTable, String> departureTime;
    public TableColumn<SummaryTable, String> PDO;
    public TableColumn<SummaryTable, String> note;
    public TableColumn<SummaryTable, String> gosNum;
    public TableColumn<SummaryTable, String> arriveTime;
    public Label displayNumOfCars;
    public TextField nameTextField;
    public TextField dateTextField;
    public TextField noteTextField;
    public Button sendRequestBtn;
    public Pane headerPane;
    public Pane closeBtn;
    public Pane minimizeBtn;
    public Pane fullscreenBtn;
    public MenuItem settingsBtn;
    public MenuItem reconnectBtn;
    private ArrayList<SummaryTable> arrayList;
    private int numOfCars;
    private boolean running;
    //private static final String SERVER_HOST = "localhost";
    //private static final int SERVER_PORT = 3443;
    private String serverHost;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private Boolean lock = false;

    public void initClient() {
        arrayList = new ArrayList<>();
        try {
            clientSocket = new Socket(serverHost, serverPort);
            outMessage = new PrintWriter(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Connection error!", "Error while connecting " +
                    "to server, check your settings or contact administrator to know about server status");
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                while (running) {
                    lock = objectInputStream.readBoolean();
                    System.out.println(lock);
                    numOfCars = objectInputStream.readInt();
                    arrayList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    System.out.println(arrayList);
                    //formTable();
                    updateTableData();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void sendMsg(String msg) {
        outMessage.println(msg);
        outMessage.flush();
    }

    public void formTable() {
        idSum.setCellValueFactory(new PropertyValueFactory<>("idSum"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        departureTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        PDO.setCellValueFactory(new PropertyValueFactory<>("PDO"));
        note.setCellValueFactory(new PropertyValueFactory<>("note"));
        gosNum.setCellValueFactory(new PropertyValueFactory<>("gosNum"));
        arriveTime.setCellValueFactory(new PropertyValueFactory<>("arriveTime"));
    }

    public void updateTableData() {
        summaryTable.getItems().clear();
        Platform.runLater(() -> displayNumOfCars.setText(String.valueOf(numOfCars)));
        Platform.runLater(() -> summaryTable.setItems(FXCollections.observableArrayList(arrayList)));
        if (lock) {
            Platform.runLater(this::onStartAlert);
            lock = false;
        }
    }

    public void shutdown() throws IOException, InterruptedException {
        Thread.sleep(100);
        outMessage.println("##session##end##");
        outMessage.flush();
        running = false;
        objectInputStream.close();
        outMessage.close();
        clientSocket.close();
        exit(0);//временное решение
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

    public void init() {
        running = true;
        try {
            getSettings();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        initClient();
        if (outMessage != null) {
            sendMsg("#INITTABLE");
        }
        formTable();
        updateTableData();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        dateTextField.setTextFormatter(new TextFormatter<>(new DateTimeStringConverter(format)));
        nameTextField.setPromptText("Name");
        noteTextField.setPromptText("Some note");
        dateTextField.setPromptText("10:00");
        sendRequestBtn.setOnAction(e -> {
            String name = nameTextField.getText();
            String timeStr = dateTextField.getText();
            sendMsg("#INSERT");
            sendMsg(name);
            sendMsg(timeStr);
        });
    }

    public void onStartAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acceptance of return");
        alert.setHeaderText(null);
        alert.setContentText("As soon as the trip is completed, you will need to click on the OK button to lock the " +
                "return time and release the car at the database");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        sendMsg("#FREEAUTO");
        sendMsg(dtf.format(now));
        System.out.println(dtf.format(now));
        try {
            shutdown();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Some error occurred while closing application");
        }
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
        reconnectBtn.setOnAction((ActionEvent) -> {
            try {
                reconnect();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}