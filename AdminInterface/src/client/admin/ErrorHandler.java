package client.admin;

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
                onStartAlert(Alert.AlertType.ERROR, "Ошибка на стороне сервера!", "Произошла ошибка во время подключения к серверу");
                break;
            case INPUT_ERROR:
                errorAlert(Alert.AlertType.ERROR, "Пользовательская ошибка",
                        "Некорректный ввод, данные не будут использованы, попробуйте ввести корректные данные");
                break;
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
