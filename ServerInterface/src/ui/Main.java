package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Сервер");
        primaryStage.setScene(new Scene(root, 800, 600));
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((ActionEvent) -> {
            try {
                controller.closeProgram();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        controller.saveBtn.setOnMouseClicked(mouseEvent -> {
            final Popup popup = new Popup();
            Label label = new Label("Настройки сохранены");
            label.setMinHeight(50);
            label.setMinWidth(15);
            popup.getContent().add(label);
            popup.setX(500);
            popup.setY(385);
            controller.setSettings();
            popup.show(primaryStage);
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
        controller.saveBtn.setOnMouseClicked((ActionEvent) -> controller.setSettings());
        controller.startServerBtn.setOnAction(ActionEvent-> {
            try {
                controller.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        controller.toggleSaveOn.setOnAction(ActionEvent -> controller.serverUser.saveToggledMode(controller.toggleSaveOn.isSelected()));
        controller.toggleSaveOff.setOnAction(ActionEvent -> controller.serverUser.saveToggledMode(controller.toggleSaveOn.isSelected()));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
