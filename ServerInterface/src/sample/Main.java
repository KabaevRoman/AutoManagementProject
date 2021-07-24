package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 800, 600));
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((ActionEvent) -> controller.closeProgram());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
