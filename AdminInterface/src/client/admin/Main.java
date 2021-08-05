package client.admin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {

    public Popup createPopup(String popupText, double PosX, double PosY) {
        final Popup popup = new Popup();
        Label label = new Label(popupText);
        label.setMinHeight(50);
        label.setMinWidth(15);
        label.setStyle("-fx-background-color: grey;-fx-background-radius: 5px;-fx-text-fill:white");
        popup.getContent().add(label);
        popup.setX(665);
        popup.setY(385);
        return popup;
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../resources/MainWindow.fxml"));
        Parent root = loader.load();
        stage.setTitle("Admin window");
        stage.setScene(new Scene(root, 1158, 700));
        SummaryController controller = loader.getController();
        stage.initStyle(StageStyle.UNDECORATED);
        final double[] mousePressedX = new double[1];
        final double[] mousePressedY = new double[1];

        controller.headerPane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mousePressedX[0] = mouseEvent.getX();
                mousePressedY[0] = mouseEvent.getY();
            }
        });

        controller.closeBtn.setOnMouseClicked((ActionEvent) -> {
            stage.close();
        });

        controller.fullscreenBtn.setOnMouseClicked((ActionEvent) -> {
            stage.setFullScreen(!stage.isFullScreen());
        });

        controller.minimizeBtn.setOnMouseClicked((ActionEvent) -> {
            stage.setIconified(true);
        });

        controller.headerPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double crrX = mouseEvent.getScreenX();
                double crrY = mouseEvent.getScreenY();
                stage.setX(crrX - mousePressedX[0]);
                stage.setY(crrY - mousePressedY[0]);
            }
        });

        stage.setOnCloseRequest(event -> {
            try {
                controller.shutdown();
            } catch (IOException | InterruptedException | NullPointerException e) {
                System.out.println("error in closing");
            }
        });

        controller.settingsBtn.setOnAction((ActionEvent) -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../resources/SettingsWindow.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage prStage = new Stage();
            prStage.initModality(Modality.APPLICATION_MODAL);
            prStage.setTitle("ABC");
            SettingsController settingsController = fxmlLoader.getController();

            Popup popup = createPopup("settings saved", 665, 385);
            settingsController.saveSettings.setOnMouseClicked(event -> {
                settingsController.save();
                popup.show(prStage);
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(popup::hide);
                            }
                        },
                        2000
                );
            });
            prStage.setScene(new Scene(root1));
            prStage.show();
        });

        controller.editDatabaseBtn.setOnAction((ActionEvent) -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../resources/EditingWindow.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditingController editingController = fxmlLoader.getController();
            Stage prStage = new Stage();
            prStage.initModality(Modality.APPLICATION_MODAL);
            prStage.setTitle("Database edit");
            prStage.setOnCloseRequest(event -> {
                try {
                    editingController.shutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            prStage.setScene(new Scene(root1));
            prStage.show();
        });

        controller.editRegNumBtn.setOnAction(ActionEvent -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../resources/RegistrationNumberMaintenance.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            RegNumMaintenanceController regNumMaintenanceController = fxmlLoader.getController();
            Stage prStage = new Stage();
            prStage.initModality(Modality.APPLICATION_MODAL);
            prStage.setTitle("Registration number maintenance");
            Popup popup = createPopup("Vehicle added!", 750, 385);
            regNumMaintenanceController.addVehicleBtn.setOnAction(event -> {
                regNumMaintenanceController.addVehicle();
                popup.show(prStage);
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(popup::hide);
                            }
                        },
                        2000
                );
            });

            prStage.setOnCloseRequest(event -> {
                try {
                    regNumMaintenanceController.shutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            prStage.setScene(new Scene(root1));
            prStage.show();
        });
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("Stopping application");
    }
    //TODO make reconnect working

    public static void main(String[] args) {
        launch(args);
    }
}
