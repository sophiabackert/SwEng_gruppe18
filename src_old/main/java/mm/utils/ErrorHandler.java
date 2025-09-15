package mm.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zentrale Fehlerbehandlungsklasse für die Anwendung.
 */
public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    /**
     * Zeigt einen Fehlerdialog an und loggt den Fehler.
     *
     * @param title Der Titel des Dialogs
     * @param header Die Überschrift des Dialogs
     * @param content Der Inhalt des Dialogs
     * @param exception Die Exception, die den Fehler verursacht hat
     */
    public static void showError(String title, String header, String content, Throwable exception) {
        // Log the error
        LOGGER.log(Level.SEVERE, content, exception);

        // Create and configure the alert
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception details
        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);
        }

        alert.showAndWait();
    }

    /**
     * Zeigt einen Fehlerdialog an und loggt den Fehler.
     *
     * @param title Der Titel des Dialogs
     * @param content Der Inhalt des Dialogs
     * @param exception Die Exception, die den Fehler verursacht hat
     */
    public static void showError(String title, String content, Throwable exception) {
        showError(title, null, content, exception);
    }

    /**
     * Zeigt einen Fehlerdialog an.
     *
     * @param title Der Titel des Dialogs
     * @param content Der Inhalt des Dialogs
     */
    public static void showError(String title, String content) {
        showError(title, null, content, null);
    }

    /**
     * Zeigt einen Fehlerdialog ohne Exception-Details an.
     *
     * @param title Der Titel des Dialogs
     * @param header Die Überschrift des Dialogs
     * @param content Der Inhalt des Dialogs
     */
    public static void showError(String title, String header, String content) {
        showError(title, header, content, null);
    }

    /**
     * Zeigt eine Erfolgsmeldung an.
     *
     * @param title Der Titel des Dialogs
     * @param content Der Inhalt des Dialogs
     */
    public static void showSuccess(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Zeigt eine Warnmeldung an.
     *
     * @param title Der Titel des Dialogs
     * @param header Die Überschrift des Dialogs
     * @param content Der Inhalt des Dialogs
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 