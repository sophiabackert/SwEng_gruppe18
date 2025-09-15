package mm.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller für das Hauptmenü.
 * <p>
 * Steuert die Navigation zu Spiel, Level-Editor, Einstellungen und das Beenden der Anwendung.
 * </p>
 */
public class MainMenuController extends Controller {

    /**
     * Button zum Beenden der Anwendung (wird für FileChooser benötigt).
     */
    @FXML
    private Button exitButton;

    /**
     * Handler für den "Spielen"-Button. Öffnet die Levelauswahl.
     */
    @FXML
    private void handlePlayGame() {
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }

    /**
     * Handler für den "Level-Editor"-Button. Öffnet den Level-Editor.
     */
    @FXML
    private void handleLevelEditor() {
        if (viewManager != null) {
            viewManager.showLevelEditor();
        }
    }

    /**
     * Handler für den "Einstellungen"-Button. Öffnet das Einstellungsmenü.
     */
    @FXML
    private void handleSettings() {
        if (viewManager != null) {
            viewManager.showSettings();
        }
    }

    /**
     * Handler für den "Beenden"-Button. Schließt die Anwendung.
     */
    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }
} 