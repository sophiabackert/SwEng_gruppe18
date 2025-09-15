package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import javafx.scene.control.ProgressIndicator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.ScaleTransition;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import mm.utils.ResourceManager;
import mm.model.LevelManager;
import mm.model.LevelInfo;
import mm.validation.LevelValidationException;
import mm.validation.LevelValidationException.ErrorType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller für die Level-Auswahl.
 */
public class LevelSelectionController extends GuiController {
    @FXML private GridPane levelGrid;
    @FXML private Pane dropZone;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final LevelManager levelManager;
    private static final int COLUMNS = 3;
    private static final double THUMBNAIL_WIDTH = 200;
    private static final double THUMBNAIL_HEIGHT = 150;
    
    public LevelSelectionController() {
        this.levelManager = new LevelManager();
    }
    
    @FXML
    private void initialize() {
        setupDropZone();
        loadLevels();
    }
    
    /**
     * Lädt und zeigt alle verfügbaren Levels an.
     */
    private void loadLevels() {
        levelGrid.getChildren().clear();
        loadingIndicator.setVisible(true);
        
        try {
            List<LevelInfo> levels = levelManager.loadLevels();
            int row = 0;
            int col = 0;
            
            for (LevelInfo level : levels) {
                VBox levelBox = createLevelBox(level);
                levelGrid.add(levelBox, col, row);
                
                col++;
                if (col >= COLUMNS) {
                    col = 0;
                    row++;
                }
            }
            
        } catch (LevelValidationException e) {
            showError("Fehler beim Laden der Levels", e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
        }
    }
    
    /**
     * Erstellt eine Box für ein Level.
     */
    private VBox createLevelBox(LevelInfo level) {
        VBox box = new VBox(10);
        box.getStyleClass().add("level-box");
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER);
        
        // Thumbnail
        ImageView thumbnail = createThumbnail(level.getFileName());
        
        // Level-Name
        Label nameLabel = new Label(levelManager.formatLevelName(level.getFileName()));
        nameLabel.getStyleClass().add("level-name");
        
        // Schwierigkeitsgrad
        Label difficultyLabel = new Label(level.getDifficulty());
        difficultyLabel.getStyleClass().add("level-difficulty");
        
        // Beschreibung
        Label descriptionLabel = new Label(level.getDescription());
        descriptionLabel.getStyleClass().add("level-description");
        descriptionLabel.setWrapText(true);
        
        // Container für Buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Start-Button
        Button startButton = new Button("Spielen");
        startButton.getStyleClass().add("level-button");
        startButton.setOnAction(e -> startLevel(level));
        
        buttonContainer.getChildren().add(startButton);
        
        // Löschen-Button für importierte Levels
        if (level.isCustomLevel()) {
            Button deleteButton = new Button("Löschen");
            deleteButton.getStyleClass().addAll("level-button", "delete-button");
            deleteButton.setOnAction(e -> deleteLevel(level));
            buttonContainer.getChildren().add(deleteButton);
        }
        
        box.getChildren().addAll(thumbnail, nameLabel, difficultyLabel, descriptionLabel, buttonContainer);
        
        // Hover-Effekt
        setupHoverEffect(box);
        
        return box;
    }
    
    /**
     * Erstellt ein Thumbnail für ein Level.
     */
    private ImageView createThumbnail(String levelName) {
        String thumbnailPath = String.format("/assets/backgrounds/%s.png", 
            levelName.replace(".json", ""));
        
        // Prüfe ob spezifisches Thumbnail existiert
        if (getClass().getResource(thumbnailPath) == null) {
            System.out.println("Thumbnail nicht gefunden: " + thumbnailPath + 
                ", verwende Standard-Thumbnail");
            // Fallback auf level1.png als Standard-Thumbnail
            thumbnailPath = "/assets/backgrounds/level1.png";
            
            // Wenn auch das nicht existiert, erstelle ein leeres Bild
            if (getClass().getResource(thumbnailPath) == null) {
                System.err.println("Kein Standard-Thumbnail gefunden!");
                // Erstelle ein einfaches farbiges Rechteck als Platzhalter
                ImageView placeholder = new ImageView();
                placeholder.setFitWidth(THUMBNAIL_WIDTH);
                placeholder.setFitHeight(THUMBNAIL_HEIGHT);
                placeholder.setStyle("-fx-background-color: #cccccc;");
                return placeholder;
            }
        }
        
        try {
            Image image = new Image(getClass().getResource(thumbnailPath).toExternalForm(), 
                THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true, true);
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(THUMBNAIL_WIDTH);
            imageView.setFitHeight(THUMBNAIL_HEIGHT);
            
            return imageView;
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Thumbnails: " + e.getMessage());
            // Fallback: leerer ImageView
            ImageView placeholder = new ImageView();
            placeholder.setFitWidth(THUMBNAIL_WIDTH);
            placeholder.setFitHeight(THUMBNAIL_HEIGHT);
            placeholder.setStyle("-fx-background-color: #cccccc;");
            return placeholder;
        }
    }
    
    /**
     * Richtet die Drag & Drop-Zone ein.
     */
    private void setupDropZone() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDrop);
        dropZone.setOnDragEntered(e -> dropZone.getStyleClass().add("drag-over"));
        dropZone.setOnDragExited(e -> dropZone.getStyleClass().remove("drag-over"));
    }
    
    /**
     * Behandelt das Drag-Over-Event.
     */
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }
    
    /**
     * Behandelt das Drop-Event.
     */
    private void handleDrop(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            if (file.getName().endsWith(".json")) {
                importLevel(file);
                success = true;
            } else {
                showError("Ungültiges Format", 
                    "Bitte nur JSON-Dateien importieren.");
            }
        }
        
        event.setDropCompleted(success);
        event.consume();
    }
    
    /**
     * Importiert ein Level.
     */
    private void importLevel(File file) {
        try {
            levelManager.importLevel(file);
            loadLevels(); // GUI aktualisieren
            showSuccess("Level importiert", 
                "Das Level wurde erfolgreich importiert.");
        } catch (LevelValidationException e) {
            String message;
            switch (e.getErrorType()) {
                case INVALID_JSON:
                    message = "Die Datei enthält kein gültiges JSON-Format.";
                    break;
                case MISSING_REQUIRED_FIELD:
                    message = "Dem Level fehlen erforderliche Felder.";
                    break;
                case INVALID_FIELD_VALUE:
                    message = "Das Level enthält ungültige Werte.";
                    break;
                case INVALID_LEVEL_STRUCTURE:
                    message = "Die Level-Struktur ist ungültig.";
                    break;
                case INVALID_THUMBNAIL:
                    message = "Das Thumbnail konnte nicht verarbeitet werden.";
                    break;
                case FILE_ACCESS_ERROR:
                    message = "Fehler beim Dateizugriff.";
                    break;
                default:
                    message = e.getMessage();
                    break;
            }
            showError("Fehler beim Import", message);
        } catch (IOException e) {
            showError("Fehler beim Import", 
                "Die Level-Datei konnte nicht kopiert werden: " + e.getMessage());
        }
    }
    
    /**
     * Löscht ein Level.
     */
    private void deleteLevel(LevelInfo level) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Level löschen");
        alert.setHeaderText("Level wirklich löschen?");
        alert.setContentText("Möchten Sie das Level '" + 
            levelManager.formatLevelName(level.getFileName()) + "' wirklich löschen?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                levelManager.deleteLevel(level.getFileName(), true);
                loadLevels(); // GUI aktualisieren
                showSuccess("Level gelöscht", 
                    "Das Level wurde erfolgreich gelöscht.");
            } catch (LevelValidationException e) {
                showError("Fehler beim Löschen", e.getMessage());
            } catch (IOException e) {
                showError("Fehler beim Löschen", 
                    "Die Level-Datei konnte nicht gelöscht werden: " + e.getMessage());
            }
        }
    }
    
    /**
     * Startet ein Level.
     */
    private void startLevel(LevelInfo level) {
        try {
            // GameEditorController über ViewManager laden (nicht direkt zum Spiel)
            GameEditorController gameEditorController = (GameEditorController) getViewManager().showGameEditor();
            
            // Game Editor mit ausgewähltem Level initialisieren
            gameEditorController.initializeEditor(level);
        } catch (NavigationException e) {
            showError("Fehler beim Spielstart", e.getMessage());
        } catch (Exception e) {
            showError("Fehler beim Spielstart", "Unerwarteter Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Richtet den Hover-Effekt für eine Level-Box ein.
     */
    private void setupHoverEffect(VBox box) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), box);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), box);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        box.setOnMouseEntered(e -> scaleUp.playFromStart());
        box.setOnMouseExited(e -> scaleDown.playFromStart());
    }
    
    /**
     * Zeigt eine Erfolgsmeldung an.
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Zeigt eine Fehlermeldung an.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Behandelt den Klick auf den Level-Import-Button.
     */
    @FXML
    private void onImportLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level importieren");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(levelGrid.getScene().getWindow());
        if (file != null) {
            importLevel(file);
        }
    }

    /**
     * Behandelt den Klick auf den Zurück-Button.
     */
    @FXML
    private void onBackClick() {
        try {
            getViewManager().showMainMenu();
        } catch (NavigationException e) {
            showError("Navigation fehlgeschlagen", e.getMessage());
        }
    }
} 