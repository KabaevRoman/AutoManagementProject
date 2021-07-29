package client;

import javafx.scene.control.Alert;

import static java.lang.System.exit;

public class ErrorHandler {
    enum ErrorCode {
        NO_ERROR,
        SERVERSIDE_ERROR,
        INPUT_ERROR,
    }

    public static void handleError(ErrorCode err) {
        switch (err) {
            case SERVERSIDE_ERROR:
                onStartAlert(Alert.AlertType.ERROR, "Serverside Error!", "Error while connecting to server");
                break;
            case INPUT_ERROR:
                errorAlert(Alert.AlertType.ERROR, "User error!",
                        "Wrong input, data will not be used, retry inputting correct data");
        }
    }

    public static void onStartAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        exit(0);
    }

    public static void errorAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
