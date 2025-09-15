package mm.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import mm.domain.json.LevelValidator;
import java.io.File;

/**
 * Controller für die Levelauswahl-Ansicht.
 * <p>
 * Ermöglicht die Auswahl und das Laden von vordefinierten oder eigenen Leveln.
 * Unterstützt das Importieren von benutzerdefinierten Leveldateien und die Navigation zurück zum Hauptmenü.
 * </p>
 */
public class LevelSelectionController extends Controller {

    /**
     * Button zum Beenden oder Verlassen der Ansicht (wird für FileChooser benötigt).
     */
    @FXML
    private Button exitButton;

    /**
     * Handler für den Zurück-Button. Wechselt zurück ins Hauptmenü.
     */
    @FXML
    private void handleBack() {
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }
    
    /**
     * Lädt Level 1 in den Game Editor.
     */
    @FXML
    private void handleLevel1() {
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level1.json");
        }
    }
    
    /**
     * Lädt Level 2 in den Game Editor.
     */
    @FXML
    private void handleLevel2() {
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level2.json");
        }
    }
    
    /**
     * Lädt Level 3 in den Game Editor.
     */
    @FXML
    private void handleLevel3() {
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level3.json");
        }
    }
    
    /**
     * Lädt Level 4 in den Game Editor.
     */
    @FXML
    private void handleLevel4() {
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level4.json");
        }
    }
    
    /**
     * Lädt Level 5 in den Game Editor.
     */
    @FXML
    private void handleLevel5() {
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level5.json");
        }
    }
    
    /**
     * Öffnet einen Dialog zum Importieren eines benutzerdefinierten Levels (JSON-Datei).
     * Validiert die Datei und kopiert sie in das Projektverzeichnis, falls gültig.
     * Zeigt das Level anschließend im Game Editor an.
     */
    @FXML
    private void handleCustomLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level importieren");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Level-Dateien", "*.json")
        );

        File file = fileChooser.showOpenDialog(exitButton.getScene().getWindow());
        if (file != null) {
            try {
                if (!LevelValidator.isValidLevelFile(file)) {
                    showAlert("Ungültiges Level-Format", "Die ausgewählte Datei enthält kein gültiges Level-Format.");
                    return;
                }

                File targetDir = new File("src/main/resources/levels");
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                
                String fileName = "custom_" + file.getName();
                File targetFile = new File(targetDir, fileName);
                java.nio.file.Files.copy(
                    file.toPath(), 
                    targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                if (viewManager != null) {
                    viewManager.showGameEditorWithLevel(fileName);
                }
            } catch (IllegalArgumentException ex) {
                showAlert("Fehler", "Ungültiges Level-Format: " + ex.getMessage());
            } catch (java.io.IOException ex) {
                showAlert("Fehler", "Fehler beim Laden des Levels: " + ex.getMessage());
            }
        }
    }

    /**
     * Zeigt einen Fehlerdialog mit Titel und Inhalt an.
     * @param title Titel des Dialogs
     * @param content Inhalt des Dialogs
     */
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 