package client;

import custom.Error.ErrorHandler;
import javafx.fxml.FXML;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import table.InterfaceData;
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
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

import static java.lang.System.exit;

//TODO при перезапуске тоже сделать чтобы отправлялся форс квит
public class MainWindowController implements Initializable {
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
    public Label displayNumOfCars;
    @FXML
    public TextField nameTextField;
    @FXML
    public TextField dateTextField;
    @FXML
    public TextField noteTextField;
    @FXML
    public Button sendRequestBtn;
    @FXML
    public Pane headerPane;
    @FXML
    public MenuItem settingsBtn;
    @FXML
    public MenuItem reconnectBtn;

    private ArrayList<SummaryTable> arrayList;
    private int numOfCars;
    private boolean running;
    private String serverHost;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private int lock = 0;//1-accepted,2-refused
    private boolean busy;
    private String gos_num;

    public void initClient() {
        arrayList = new ArrayList<>();
        try {
            clientSocket = new Socket(serverHost, serverPort);
            outMessage = new PrintWriter(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                    "Ошибка во время подключения к серверу, проверьте настройки подключения или обратитесь к " +
                            "администратору чтобы узнать статус сервера ");
            return;
        }
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    //lock = objectInputStream.readInt();
                    //System.out.println(lock);
                    //numOfCars = objectInputStream.readInt();
                    InterfaceData interfaceData = (InterfaceData) objectInputStream.readObject();
                    lock = interfaceData.getLock();
                    numOfCars = interfaceData.getNumOfCars();
                    gos_num = interfaceData.getRegNum();
                    arrayList = (ArrayList<SummaryTable>) objectInputStream.readObject();
                    System.out.println(arrayList);
                    updateTableData();
                } catch (IOException | ClassNotFoundException e) {
                    Platform.runLater(() -> ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка подключения!",
                            "Вы были отключены от сервера"));
                    e.printStackTrace();
                    break;
                }
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
        switch (lock) {
            case 1:
                Platform.runLater(() -> onStartAlert(1, "Заявка одобрена!",
                        "Как только вы вернетесь на рабочее место," +
                                " нажмите клавишу ок, это зафиксирует время прибытия\nНомер вашей машины: "));
                break;
            case 2:
                Platform.runLater(() -> onStartAlert(2, "Заявка не одобрена!",
                        "Нажмите OK чтобы закрыть программу"));
        }
        lock = 0;
    }

    public void shutdown(boolean force) throws IOException, InterruptedException {
        Thread.sleep(100);
        try {
            if (force) {
                outMessage.println("#FORCEQUIT");
            }
            outMessage.println("##session##end##");
            outMessage.flush();
            running = false;
            objectInputStream.close();
            outMessage.close();
            clientSocket.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
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
            ex.printStackTrace();
        }
        objectInputStream = null;
        init();
    }

    public void init() {
        busy = false;
        running = true;
        try {
            getSettings();
            initClient();
            sendMsg("#INITTABLE");
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
        formTable();
        updateTableData();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        dateTextField.setTextFormatter(new TextFormatter<>(new DateTimeStringConverter(format)));
        sendRequestBtn.setOnAction(e -> {
            String name = nameTextField.getText();
            String timeStr = dateTextField.getText();
            String note = noteTextField.getText();
            if (!busy) {
                if (name.equals("") || timeStr.equals("")) {
                    ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Вы не ввели имя либо время отправления");
                    busy = false;
                } else {
                    sendMsg("#INSERT");
                    sendMsg(name);
                    sendMsg(note);
                    sendMsg(timeStr);
                    busy = true;
                }
            } else {
                ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Пользовательская ошибка",
                        "вы уже отправили запрос");
            }
        });
    }

    public void onStartAlert(int code, String title, String contentText) {
        new AudioClip(Objects.requireNonNull(MainWindowController.class.getResource("/notification.wav")).toString()).play();
        if (code == 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle(title);
            alert.setContentText(contentText + gos_num);
            alert.initModality(Modality.APPLICATION_MODAL);
            Stage stage = (Stage) summaryTable.getScene().getWindow();
            stage.hide();
            alert.showAndWait();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            sendMsg("#FREEAUTO");
            sendMsg(dtf.format(now));
            System.out.println(dtf.format(now));
            try {
                shutdown(false);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        } else if (code == 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setTitle(title);
            alert.setContentText(contentText);
            alert.initModality(Modality.APPLICATION_MODAL);
            Stage stage = (Stage) summaryTable.getScene().getWindow();
            stage.hide();
            alert.showAndWait();
            sendMsg("#ARCHIVE");
            try {
                shutdown(false);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void getSettings() throws IOException {
        File file = new File("ClientSettings.txt");
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
    }
}