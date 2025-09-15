package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import mm.gui.NavigationException;

/**
 * Controller-Klasse für das Hauptmenü der Anwendung.
 */
public class GuiMainMenuController extends GuiController {
    
    @FXML
    private Button startGameButton;
    
    @FXML
    private Button levelEditorButton;
    
    @FXML
    private Button exitButton;
    
    /**
     * Wird aufgerufen, wenn der "Spiel starten" Button geklickt wird.
     */
    @FXML
    private void onPlayClick() {
        try {
            getViewManager().showLevelSelection();
        } catch (NavigationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Wird aufgerufen, wenn der "Level Editor" Button geklickt wird.
     */
    @FXML
    private void onLevelEditorClick() {
        try {
            getViewManager().showLevelEditor();
        } catch (NavigationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Wird aufgerufen, wenn der "Beenden" Button geklickt wird.
     */
    @FXML
    private void onExitClick() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSettings() {
        try {
            getViewManager().showSettings();
        } catch (NavigationException e) {
            e.printStackTrace();
        }
    }
} 