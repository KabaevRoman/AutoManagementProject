package client;

import com.company.SummaryTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.util.converter.DateTimeStringConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.lang.System.exit;

public class SummaryController implements Initializable {
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

    private ArrayList<SummaryTable> arrayList;
    private int numOfCars;
    private boolean running = true;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3443;
    private Socket clientSocket;
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private Boolean lock = false;

    public void initClient() throws IOException {
        arrayList = new ArrayList<>();
        clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
        outMessage = new PrintWriter(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        new Thread(() -> {
            try {
                while (running) {
                    lock = objectInputStream.readBoolean();
                    System.out.println();
                    numOfCars = objectInputStream.readInt();
                    arrayList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    System.out.println(arrayList);
                    updateTable();
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
        Platform.runLater(() -> displayNumOfCars.setText(String.valueOf(numOfCars)));
        summaryTable.setItems(FXCollections.observableArrayList(arrayList));
        if (lock) {
            Platform.runLater(this::onStartAlert);
            lock = false;
        }
    }

    public void shutdown() {
        try {
            Thread.sleep(100);
            running = false;
            outMessage.println("##session##end##");
            outMessage.flush();
            outMessage.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (IOException | InterruptedException | NullPointerException ex) {
            System.out.println("Some kind of error while closing");
        }
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initClient();
            sendMsg("#INITTABLE");
            updateTable();
        } catch (IOException e) {
            System.out.println("Error while connecting to server");
            //e.printStackTrace();
        }

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

    //TODO таймстемп о возвращении
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
        shutdown();
    }
}