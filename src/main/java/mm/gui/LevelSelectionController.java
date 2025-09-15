package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import mm.core.json.LevelValidator;
import java.io.File;

/**
 * Controller für die Level-Auswahl.
 */
public class LevelSelectionController extends Controller {

    @FXML
    private Button exitButton;

    @FXML
    private void handleBack() {
        System.out.println("Zurück zum Hauptmenü");
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }
    
    @FXML
    private void handleLevel1() {
        System.out.println("Level 1 ausgewählt");
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level1.json");
        }
    }
    
    @FXML
    private void handleLevel2() {
        System.out.println("Level 2 ausgewählt");
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level2.json");
        }
    }
    
    @FXML
    private void handleLevel3() {
        System.out.println("Level 3 ausgewählt");
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level3.json");
        }
    }
    
    @FXML
    private void handleLevel4() {
        System.out.println("Level 4 ausgewählt");
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level4.json");
        }
    }
    
    @FXML
    private void handleLevel5() {
        System.out.println("Level 5 ausgewählt");
        if (viewManager != null) {
            viewManager.showGameEditorWithLevel("level5.json");
        }
    }
    
    @FXML
    private void handleCustomLevel() {
        System.out.println("Custom Level ausgewählt");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level importieren");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Level-Dateien", "*.json")
        );

        File file = fileChooser.showOpenDialog(exitButton.getScene().getWindow());
        if (file != null) {
            try {
                // Validiere die JSON-Datei
                if (!LevelValidator.isValidLevelFile(file)) {
                    showAlert("Ungültiges Level-Format", "Die ausgewählte Datei enthält kein gültiges Level-Format.");
                    return;
                }

                // Kopiere die Datei in den resources/levels Ordner
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

                // Öffne das Level im GameEditor
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

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 