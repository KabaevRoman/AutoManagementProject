package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 800, 600));
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((ActionEvent) -> controller.closeProgram());
        controller.saveBtn.setOnMouseClicked(mouseEvent -> {
            final Popup popup = new Popup();
            Label label = new Label("Settings saved");
            label.setMinHeight(50);
            label.setMinWidth(15);
            label.setStyle("-fx-background-color: grey;-fx-background-radius: 5px;-fx-text-fill:white");
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
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
