package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        stage.setTitle("Client window");
        stage.setScene(new Scene(root, 900, 700));
        SummaryController controller = loader.getController();
        stage.setOnCloseRequest(event -> controller.shutdown());

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