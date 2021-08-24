package custom.Error;

import javafx.scene.control.Alert;

import static java.lang.System.exit;

public class ErrorHandler {
    public static void errorAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
