package ui;


import client.MainWindowController;
import client.SettingsController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        stage.setTitle("Клиент");
        stage.setScene(new Scene(root, 1156, 700));
        MainWindowController controller = loader.getController();

        stage.setOnCloseRequest(event -> {
            try {
                controller.shutdown(true);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        controller.reconnectBtn.setOnAction(ActionEvent -> {
            try {
                controller.reconnect();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        controller.settingsBtn.setOnAction((ActionEvent) -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SettingsWindow.fxml"));
            Parent root1 = null;
            try {
                root1 = (Parent) fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage prStage = new Stage();
            prStage.initModality(Modality.APPLICATION_MODAL);
            prStage.setTitle("Настройки");
            SettingsController settingsController = fxmlLoader.getController();

            final Popup popup = new Popup();
            Label label = new Label("Настройки сохранены");
            label.setMinHeight(50);
            label.setMinWidth(15);
            label.setStyle("-fx-background-color: grey;-fx-background-radius: 5px;-fx-text-fill:white");
            popup.getContent().add(label);
            popup.setX(665);
            popup.setY(385);

            settingsController.saveSettings.setOnMouseClicked(event -> {
                settingsController.save();
                popup.show(prStage);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
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

        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("Stopping application");
    }

    public static void main(String[] args) {
        launch(args);
    }
}