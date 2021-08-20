package client;

import custom.Error.ErrorHandler;
import javafx.fxml.FXML;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import msg.UserMsg;
import msg.ServiceMsg;
import table.SummaryTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.util.converter.DateTimeStringConverter;
import msg.UserInfo;

import java.io.*;
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
    //private PrintWriter outMessage;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private int lock = 0;//1-accepted,2-refused
    private boolean busy;
    private String gos_num;
    private String username;
    private String password;

    public void initClient() {
        arrayList = new ArrayList<>();
        try {
            clientSocket = new Socket(serverHost, serverPort);
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            UserInfo userInfo = new UserInfo(username, password, false);
            objectOutputStream.writeObject(userInfo);
            objectOutputStream.flush();
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
                    UserMsg userMsg = (UserMsg) objectInputStream.readObject();
                    System.out.println(userMsg);
                    lock = userMsg.getLock();
                    numOfCars = userMsg.getNumOfCars();
                    gos_num = userMsg.getRegNum();
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

    public void sendMsg(ServiceMsg serviceMsg) throws IOException {
        objectOutputStream.writeObject(serviceMsg);
        objectOutputStream.flush();
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
                Platform.runLater(() -> {
                    try {
                        onStartAlert(1, "Заявка одобрена!",
                                "Как только вы вернетесь на рабочее место," +
                                        " нажмите клавишу ок, это зафиксирует время прибытия\nНомер вашей машины: ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case 2:
                Platform.runLater(() -> {
                    try {
                        onStartAlert(2, "Заявка не одобрена!",
                                "Нажмите OK чтобы закрыть программу");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

    public void shutdown(boolean force) throws IOException, InterruptedException {
        Thread.sleep(100);
        running = false;
        try {
            if (force) {
                ServiceMsg serviceMsg = new ServiceMsg();
                serviceMsg.command = "#FORCEQUIT";
                sendMsg(serviceMsg);
//                outMessage.println("#FORCEQUIT");
            }
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "##session##end##";
            sendMsg(serviceMsg);
            objectInputStream.close();
            objectOutputStream.close();
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
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "##session##end##";
//            outMessage.println("##session##end##");
//            outMessage.flush();
            objectOutputStream.close();
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
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "#AUTH";
            sendMsg(serviceMsg);
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
                    ServiceMsg serviceMsg = new ServiceMsg();
                    serviceMsg.command = "#INSERT";
                    serviceMsg.parameters.put("name", name);
                    serviceMsg.parameters.put("note", note);
                    serviceMsg.parameters.put("departureTime", timeStr);
                    try {
                        sendMsg(serviceMsg);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    busy = true;
                }
            } else {
                ErrorHandler.errorAlert(Alert.AlertType.ERROR, "Пользовательская ошибка",
                        "вы уже отправили запрос");
            }
        });
    }

    public void onStartAlert(int code, String title, String contentText) throws IOException {
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
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "#FREEAUTO";
            serviceMsg.parameters.put("returnTime", dtf.format(now));
            sendMsg(serviceMsg);
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
            ServiceMsg serviceMsg = new ServiceMsg();
            serviceMsg.command = "#ARCHIVE";
            sendMsg(serviceMsg);
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
        if (sc.hasNext()) {
            username = sc.nextLine();
            password = sc.nextLine();
        }
        sc.close();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }
}