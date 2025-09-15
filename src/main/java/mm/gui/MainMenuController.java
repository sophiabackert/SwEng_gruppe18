package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller für das Hauptmenü.
 */
public class MainMenuController extends Controller {

    @FXML
    private Button exitButton;

    /**
     * Wird aufgerufen, wenn der "Spiel starten" Button geklickt wird.
     */
    @FXML
    private void handlePlayGame() {
        System.out.println("Spiel starten geklickt");
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }

    /**
     * Wird aufgerufen, wenn der "Level Editor" Button geklickt wird.
     */
    @FXML
    private void handleLevelEditor() {
        System.out.println("Level Editor geklickt");
        if (viewManager != null) {
            viewManager.showLevelEditor();
        }
    }

    /**
     * Wird aufgerufen, wenn der "Einstellungen" Button geklickt wird.
     */
    @FXML
    private void handleSettings() {
        System.out.println("Einstellungen geklickt");
        if (viewManager != null) {
            viewManager.showSettings();
        }
    }

    /**
     * Wird aufgerufen, wenn der "Beenden" Button geklickt wird.
     */
    @FXML
    private void handleExit() {
        System.out.println("Beenden geklickt");
        javafx.application.Platform.exit();
    }
} 