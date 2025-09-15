package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.DragEvent;
import javafx.scene.canvas.Canvas;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;

import mm.objects.GameObject;
import mm.objects.balls.*;
import mm.objects.containers.*;
import mm.inventory.InventoryManager;
import mm.inventory.InventoryItem;
import mm.rules.PlacementRules;
import mm.resources.ResourceManager;
import mm.editor.commands.*;
import mm.engine.GameEngine;
import mm.gui.components.FloatingPauseButton;
import mm.world.World;

import org.jbox2d.common.Vec2;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mm.model.*;
import mm.validation.LevelSchemaValidator;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.stream.Collectors;
import mm.validation.ValidationException;
import mm.utils.ErrorHandler;
import mm.exceptions.LevelLoadException;
import mm.exceptions.LevelSaveException;

/**
 * Controller für den Level-Editor.
 */
public class LevelEditorController extends GuiController {
    
    @FXML
    private BorderPane rootPane;
    
    @FXML
    private Pane editorCanvas;
    
    @FXML
    private VBox objectPalette;
    
    @FXML
    private VBox propertyEditor;
    
    @FXML
    private ListView<InventoryItem> inventoryList;
    
    @FXML
    private Button undoButton;
    
    @FXML
    private Button redoButton;
    
    @FXML
    private Button playButton;
    
    @FXML
    private TextField levelNameField;
    
    @FXML
    private ComboBox<String> difficultyComboBox;
    
    @FXML
    private TextArea taskDescriptionArea;
    
    private GameObject selectedObject;
    private final Map<Node, GameObject> gameObjects = new HashMap<>();
    private final InventoryManager inventoryManager = InventoryManager.getInstance();
    private final CommandHistory commandHistory = new CommandHistory();
    
    private boolean showGrid = true;
    private Rectangle previewRect;
    private GameEngine gameEngine;
    private Canvas gameCanvas;
    private World world;
    private boolean isPlaying;
    
    private static final double GAME_FIELD_WIDTH = 800;
    private static final double GAME_FIELD_HEIGHT = 600;
    private static final double BOUNDARY_PADDING = 10;
    
    private FloatingPauseButton floatingPauseButton;
    
    private Text timerText;
    private AnimationTimer timerUpdateLoop;
    private GameOverOverlay gameOverOverlay;
    
    private final LevelSchemaValidator schemaValidator;
    private File currentFile;
    
    public LevelEditorController() {
        this.schemaValidator = new LevelSchemaValidator();
    }
    
    @FXML
    public void initialize() {
        // 1. Initialisiere die Spielwelt ZUERST
        world = new World();
        
        // 2. UI-Komponenten initialisieren
        initializeObjectPalette();
        setupCanvasListeners();
        setupInventoryList();
        setupPreviewRect();
        
        // 3. Setze die Größe des Spielfelds
        editorCanvas.setPrefSize(GAME_FIELD_WIDTH, GAME_FIELD_HEIGHT);
        
        // 4. Registriere Event-Handler für Drag & Drop
        editorCanvas.setOnDragOver(this::handleDragOver);
        editorCanvas.setOnDragDropped(this::handleDrop);
        
        // 5. Initialisiere das Spiel-Canvas
        setupGameCanvas();
        
        // 6. Initialisiere die GameEngine (mit gültiger World und Canvas)
        if (gameCanvas != null && world != null) {
            gameEngine = new GameEngine(gameCanvas, world);
            gameEngine.setOnGameWon(this::handleGameWon);
            gameEngine.setOnGameOver(this::handleGameOver);
        }
        
        isPlaying = false;
        
        // 7. Weitere UI-Komponenten
        setupFloatingPauseButton();
        setupTimerText();
        setupTimerUpdateLoop();
        setupGameOverOverlay();
        
        // 8. Warte auf die Scene, bevor wir die Tastenkombinationen einrichten
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupUndoRedo();
            }
        });
        
        // 9. Initialisiere UI-Werte
        difficultyComboBox.getItems().addAll("EASY", "MEDIUM", "HARD", "CUSTOM");
        difficultyComboBox.setValue("CUSTOM");
        
        levelNameField.setText("Neues Level");
        taskDescriptionArea.setText("Erreiche das Ziel");
    }
    
    /**
     * Initialisiert die Objektpalette mit verfügbaren Spielobjekten.
     */
    private void initializeObjectPalette() {
        // Erstelle Vorschaubilder für verschiedene Spielobjekte
        createPaletteItem("Ball", createPreviewNode("ball", 30, 30));
        createPaletteItem("Box", createPreviewNode("box", 40, 40));
        createPaletteItem("Plank", createPreviewNode("plank", 60, 20));
        createPaletteItem("Bucket", createPreviewNode("bucket", 50, 50));
    }
    
    /**
     * Erstellt ein Vorschaubild für ein Spielobjekt.
     */
    private Node createPreviewNode(String type, double width, double height) {
        Rectangle preview = new Rectangle(width, height);
        preview.getStyleClass().add("preview-" + type);
        preview.setStyle("-fx-fill: #4a90e2; -fx-stroke: #2171c7; -fx-stroke-width: 2;");
        return preview;
    }
    
    /**
     * Fügt ein Element zur Objektpalette hinzu und richtet Drag-and-Drop ein.
     */
    private void createPaletteItem(String name, Node previewNode) {
        VBox item = new VBox(5);
        item.getStyleClass().add("palette-item");
        item.getChildren().addAll(previewNode, new Label(name));
        
        // Drag-and-Drop-Funktionalität
        item.setOnDragDetected(event -> {
            // Starte Drag-Operation
            var dragboard = item.startDragAndDrop(TransferMode.COPY);
            var content = new ClipboardContent();
            content.putString(name.toLowerCase());
            dragboard.setContent(content);
            
            // Setze ein visuelles Feedback
            var snapshot = item.snapshot(null, null);
            dragboard.setDragView(snapshot);
            
            event.consume();
        });
        
        objectPalette.getChildren().add(item);
    }
    
    /**
     * Richtet die Event-Listener für die Zeichenfläche ein.
     */
    private void setupCanvasListeners() {
        editorCanvas.setOnMouseClicked(this::handleCanvasClick);
        
        editorCanvas.setOnDragOver(event -> {
            if (event.getGestureSource() != editorCanvas &&
                event.getDragboard().hasString()) {
                
                String type = event.getDragboard().getString();
                double x = PlacementRules.snapToGrid(event.getX());
                double y = PlacementRules.snapToGrid(event.getY());
                
                // Aktualisiere die Vorschau
                updatePreviewRect(type, x, y);
                
                // Prüfe, ob das Objekt hier platziert werden kann
                if (PlacementRules.canPlaceObject(x, y, type, gameObjects,
                    editorCanvas.getWidth(), editorCanvas.getHeight())) {
                    event.acceptTransferModes(TransferMode.COPY);
                    previewRect.setStyle("-fx-fill: rgba(74, 144, 226, 0.3); -fx-stroke: #2171c7; -fx-stroke-width: 2; -fx-stroke-dash-array: 5;");
                } else {
                    previewRect.setStyle("-fx-fill: rgba(255, 0, 0, 0.3); -fx-stroke: #ff0000; -fx-stroke-width: 2; -fx-stroke-dash-array: 5;");
                }
            }
            event.consume();
        });
        
        editorCanvas.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString()) {
                String type = dragboard.getString();
                double x = PlacementRules.snapToGrid(event.getX());
                double y = PlacementRules.snapToGrid(event.getY());
                
                // Prüfe, ob das Objekt hier platziert werden kann
                if (PlacementRules.canPlaceObject(x, y, type, gameObjects,
                    editorCanvas.getWidth(), editorCanvas.getHeight())) {
                    createGameObject(type, x, y);
                    success = true;
                }
            }
            
            previewRect.setVisible(false);
            event.setDropCompleted(success);
            event.consume();
        });
        
        editorCanvas.setOnDragExited(event -> {
            previewRect.setVisible(false);
            event.consume();
        });
    }
    
    private void updatePreviewRect(String type, double x, double y) {
        double width = type.equals("plank") ? 60 : 40;
        double height = type.equals("plank") ? 20 : 40;
        if (type.equals("bucket")) {
            width = height = 50;
        }
        
        previewRect.setWidth(width);
        previewRect.setHeight(height);
        previewRect.setX(x - width/2);
        previewRect.setY(y - height/2);
        previewRect.setVisible(true);
    }
    
    /**
     * Richtet die Undo/Redo-Funktionalität ein.
     */
    private void setupUndoRedo() {
        // Tastenkombinationen
        rootPane.getScene().getAccelerators().put(
            new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
            this::onUndo
        );
        rootPane.getScene().getAccelerators().put(
            new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
            this::onRedo
        );
        rootPane.getScene().getAccelerators().put(
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            this::quickSaveLevel
        );
        
        // Button-Zustände aktualisieren
        undoButton.setDisable(!commandHistory.canUndo());
        redoButton.setDisable(!commandHistory.canRedo());
    }
    
    @FXML
    private void onUndo() {
        if (commandHistory.undo()) {
            updateUndoRedoButtons();
        }
    }
    
    @FXML
    private void onRedo() {
        if (commandHistory.redo()) {
            updateUndoRedoButtons();
        }
    }
    
    private void updateUndoRedoButtons() {
        undoButton.setDisable(!commandHistory.canUndo());
        redoButton.setDisable(!commandHistory.canRedo());
    }
    
    /**
     * Erstellt ein GameObject des angegebenen Typs.
     * @param type Der Typ des zu erstellenden GameObjects
     * @param x Die x-Koordinate
     * @param y Die y-Koordinate
     * @return Das erstellte GameObject oder null, wenn der Typ nicht unterstützt wird
     */
    private GameObject createGameObject(String type, double x, double y) {
        // Konvertiere die Koordinaten zu float für die Physik-Engine
        float physX = (float) x;
        float physY = (float) y;
        
        // Erstelle das GameObject basierend auf dem Typ
        GameObject obj = null;
        Vec2 position = new Vec2(physX, physY);
        
        switch (type.toLowerCase()) {
            case "billiardball":
                obj = new BilliardBall(position);
                break;
            case "bowlingball":
                obj = new BowlingBall(position);
                break;
            case "tennisball":
                obj = new TennisBall(position);
                break;
            // Weitere Objekttypen hier hinzufügen
        }
        
        if (obj != null) {
            Node visualNode = createPreviewNode(type, 
                type.equals("plank") ? 60 : 40,  // Spezielle Größe für Planken
                type.equals("plank") ? 20 : 40
            );
            
            visualNode.setLayoutX(x - visualNode.getBoundsInLocal().getWidth() / 2);
            visualNode.setLayoutY(y - visualNode.getBoundsInLocal().getHeight() / 2);
            
            // Speichere das Objekt in der Map
            gameObjects.put(visualNode, obj);
            
            // Mache das Objekt verschiebbar
            setupDraggable(visualNode);
            
            // Füge das Objekt zur Zeichenfläche hinzu
            editorCanvas.getChildren().add(visualNode);
        }
        
        return obj;
    }
    
    /**
     * Macht ein Node-Element auf der Zeichenfläche verschiebbar.
     */
    private void setupDraggable(Node node) {
        final Delta dragDelta = new Delta();
        final double[] startPos = new double[2];
        
        node.setOnMousePressed(event -> {
            // Speichere die Anfangsposition für das Verschieben
            dragDelta.x = node.getLayoutX() - event.getSceneX();
            dragDelta.y = node.getLayoutY() - event.getSceneY();
            startPos[0] = node.getLayoutX();
            startPos[1] = node.getLayoutY();
            
            // Markiere das Objekt als ausgewählt
            node.setStyle(node.getStyle() + "; -fx-effect: dropshadow(gaussian, #2171c7, 10, 0, 0, 0);");
            
            // Aktualisiere das ausgewählte Objekt
            selectedObject = gameObjects.get(node);
            
            event.consume();
        });
        
        node.setOnMouseDragged(event -> {
            double newX = PlacementRules.snapToGrid(event.getSceneX() + dragDelta.x);
            double newY = PlacementRules.snapToGrid(event.getSceneY() + dragDelta.y);
            
            // Prüfe, ob die neue Position gültig ist
            String type = getObjectType(gameObjects.get(node));
            if (type != null) {
                // Erstelle eine temporäre Map ohne das aktuelle Objekt
                Map<Node, GameObject> tempObjects = new HashMap<>(gameObjects);
                tempObjects.remove(node);
                
                if (PlacementRules.canPlaceObject(newX, newY, type, tempObjects,
                    editorCanvas.getWidth(), editorCanvas.getHeight())) {
                    node.setLayoutX(newX);
                    node.setLayoutY(newY);
                    
                    // Aktualisiere die Position des GameObjects
                    GameObject obj = gameObjects.get(node);
                    if (obj != null) {
                        obj.setPosition(
                            (float)(newX + node.getBoundsInLocal().getWidth() / 2),
                            (float)(newY + node.getBoundsInLocal().getHeight() / 2)
                        );
                    }
                }
            }
            
            event.consume();
        });
        
        node.setOnMouseReleased(event -> {
            // Entferne den Auswahleffekt
            node.setStyle(node.getStyle().replace("; -fx-effect: dropshadow(gaussian, #2171c7, 10, 0, 0, 0)", ""));
            
            // Erstelle einen Bewegungsbefehl, wenn sich die Position geändert hat
            double endX = node.getLayoutX();
            double endY = node.getLayoutY();
            if (endX != startPos[0] || endY != startPos[1]) {
                MoveObjectCommand command = new MoveObjectCommand(
                    node, gameObjects.get(node),
                    startPos[0], startPos[1],
                    endX, endY
                );
                commandHistory.executeCommand(command);
                updateUndoRedoButtons();
            }
            
            event.consume();
        });
    }
    
    /**
     * Hilfsklasse zum Speichern der Drag-Position.
     */
    private static class Delta {
        double x, y;
    }
    
    /**
     * Behandelt Klicks auf die Zeichenfläche.
     */
    private void handleCanvasClick(MouseEvent event) {
        // Deselektiere das aktuelle Objekt
        if (selectedObject != null) {
            for (Map.Entry<Node, GameObject> entry : gameObjects.entrySet()) {
                if (entry.getValue() == selectedObject) {
                    entry.getKey().setStyle(entry.getKey().getStyle().replace("-fx-effect: dropshadow(gaussian, #2171c7, 10, 0, 0, 0);", ""));
                    break;
                }
            }
        }
        selectedObject = null;
        propertyEditor.setVisible(false);
    }
    
    private void setupInventoryList() {
        inventoryList.setItems(FXCollections.observableArrayList(inventoryManager.getItems()));
        inventoryList.getStyleClass().add("inventory-list");
        
        // Drag-and-Drop für Inventar-Items
        inventoryList.setOnDragDetected(event -> {
            if (inventoryList.getSelectionModel().getSelectedItem() != null) {
                InventoryItem item = inventoryList.getSelectionModel().getSelectedItem();
                
                // Starte Drag-Operation
                var dragboard = inventoryList.startDragAndDrop(TransferMode.COPY);
                var content = new ClipboardContent();
                content.putString(item.getType());
                dragboard.setContent(content);
                
                // Setze ein visuelles Feedback
                Node previewNode = createPreviewNode(item.getType(), 
                    item.getType().equals("plank") ? 60 : 40,
                    item.getType().equals("plank") ? 20 : 40
                );
                var snapshot = previewNode.snapshot(null, null);
                dragboard.setDragView(snapshot);
                
                event.consume();
            }
        });
    }
    
    @FXML
    private void onAddToInventory() {
        if (selectedObject != null) {
            String type = getObjectType(selectedObject);
            if (type != null) {
                inventoryManager.addItem(type);
                removeSelectedObject();
            }
        }
    }
    
    @FXML
    private void onRemoveFromInventory() {
        InventoryItem selectedItem = inventoryList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            inventoryManager.removeItem(selectedItem.getType());
        }
    }
    
    /**
     * Ermittelt den Typ eines GameObjects.
     */
    private String getObjectType(GameObject obj) {
        if (obj instanceof BilliardBall) return "ball";
        if (obj instanceof Box) return "box";
        if (obj instanceof Plank) return "plank";
        if (obj instanceof Bucket) return "bucket";
        throw new IllegalArgumentException("Unbekannter Objekttyp: " + obj.getClass().getSimpleName());
    }
    
    private void removeSelectedObject() {
        if (selectedObject != null) {
            Node nodeToRemove = null;
            for (Map.Entry<Node, GameObject> entry : gameObjects.entrySet()) {
                if (entry.getValue() == selectedObject) {
                    nodeToRemove = entry.getKey();
                    break;
                }
            }
            
            if (nodeToRemove != null) {
                DeleteObjectCommand command = new DeleteObjectCommand(
                    editorCanvas, nodeToRemove, selectedObject, gameObjects
                );
                commandHistory.executeCommand(command);
                updateUndoRedoButtons();
                selectedObject = null;
            }
        }
    }
    
    @FXML
    private void onNewLevel() {
        clearCanvas();
    }
    
    /**
     * Konfiguriert einen FileChooser für Level-Dateien.
     * @param title Der Titel des Dialogs
     * @return Konfigurierter FileChooser
     */
    private FileChooser configureFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        
        // Setze das Standardverzeichnis auf den Level-Ordner
        File levelsDir = new File(ResourceManager.getResourceDirectory(), "levels");
        if (!levelsDir.exists()) {
            levelsDir.mkdirs();
        }
        fileChooser.setInitialDirectory(levelsDir);
        
        // Füge Dateifilter hinzu
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Level-Dateien (*.json)", "*.json"),
            new FileChooser.ExtensionFilter("Alle Dateien (*.*)", "*.*")
        );
        
        return fileChooser;
    }
    
    @FXML
    private void onLoadLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level laden");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Levels", "*.json")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                loadLevelFromFile(file);
                currentFile = file;
                ErrorHandler.showSuccess("Level geladen", 
                    "Das Level wurde erfolgreich geladen.");
            } catch (LevelLoadException e) {
                ErrorHandler.showError("Fehler beim Laden", 
                    "Level konnte nicht geladen werden", 
                    e.getMessage(), e);
            }
        }
    }

    @FXML
    private void onSaveLevel() {
        if (currentFile == null) {
            onSaveLevelAs();
            return;
        }

        try {
            saveLevelToFile(currentFile);
            ErrorHandler.showSuccess("Level gespeichert", 
                "Das Level wurde erfolgreich gespeichert.");
        } catch (LevelSaveException e) {
            ErrorHandler.showError("Fehler beim Speichern", 
                "Level konnte nicht gespeichert werden", 
                e.getMessage(), e);
        }
    }

    @FXML
    private void onSaveLevelAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level speichern");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Levels", "*.json")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            // Füge .json-Erweiterung hinzu, falls nicht vorhanden
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }

            // Prüfe, ob die Datei bereits existiert
            if (file.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Datei überschreiben");
                alert.setHeaderText("Die Datei existiert bereits");
                alert.setContentText("Möchten Sie die vorhandene Datei überschreiben?");
                
                if (alert.showAndWait().get() != ButtonType.OK) {
                    return;
                }
            }

            try {
                saveLevelToFile(file);
                currentFile = file;
                ErrorHandler.showSuccess("Level gespeichert", 
                    "Das Level wurde erfolgreich gespeichert.");
            } catch (LevelSaveException e) {
                ErrorHandler.showError("Fehler beim Speichern", 
                    "Level konnte nicht gespeichert werden", 
                    e.getMessage(), e);
            }
        }
    }

    private void loadLevelFromFile(File file) throws LevelLoadException {
        try {
            String jsonContent = Files.readString(file.toPath());
            
            // Validate against schema before loading
            try {
                schemaValidator.validate(jsonContent);
            } catch (ValidationException e) {
                throw new LevelLoadException("Das Level ist ungültig:\n" + 
                    e.getAllMessages().stream().collect(Collectors.joining("\n")), e);
            }
            
            // Parse and load level data
            // TODO: Implement actual level loading logic
            
        } catch (IOException e) {
            throw new LevelLoadException("Fehler beim Lesen der Datei: " + e.getMessage(), e);
        }
    }
    
    /**
     * Speichert ein Level in eine Datei.
     * @param file Die Zieldatei
     */
    private void saveLevelToFile(File file) throws LevelSaveException {
        try {
            String jsonContent = createLevelJson();
            Files.writeString(file.toPath(), jsonContent);
        } catch (IOException e) {
            throw new LevelSaveException("Fehler beim Speichern des Levels: " + e.getMessage(), e);
        }
    }
    
    private String generateLevelJson() {
        // TODO: Implement actual JSON generation logic
        return "{}"; // Placeholder
    }
    
    @FXML
    private void onBack() {
        // Wenn das Spiel läuft, beende es
        if (isPlaying) {
            stopGameSimulation();
        }
        
        try {
            ViewManager.getInstance().navigateTo(ViewManager.View.MAIN_MENU);
        } catch (NavigationException e) {
            ErrorHandler.showError("Navigationsfehler", "Konnte nicht zum Hauptmenü zurückkehren", e);
        }
    }
    
    /**
     * Löscht alle Objekte von der Zeichenfläche.
     */
    private void clearCanvas() {
        editorCanvas.getChildren().clear();
        gameObjects.clear();
        selectedObject = null;
        propertyEditor.setVisible(false);
        commandHistory.clear();
        updateUndoRedoButtons();
    }
    
    /**
     * Zeigt eine Fehlermeldung an.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Initialisiert das Vorschau-Rechteck für Drag-and-Drop.
     */
    private void setupPreviewRect() {
        previewRect = new Rectangle(40, 40);
        previewRect.setStyle("-fx-fill: rgba(74, 144, 226, 0.3); -fx-stroke: #2171c7; -fx-stroke-width: 2; -fx-stroke-dash-array: 5;");
        previewRect.setVisible(false);
        editorCanvas.getChildren().add(previewRect);
    }
    
    /**
     * Überprüft, ob ein Objekt innerhalb der Spielfeldgrenzen platziert werden kann.
     */
    private boolean isWithinBoundaries(Node node, double x, double y) {
        double nodeWidth = node.getBoundsInLocal().getWidth();
        double nodeHeight = node.getBoundsInLocal().getHeight();
        
        // Prüfe, ob das Objekt komplett innerhalb der Grenzen liegt
        return x >= BOUNDARY_PADDING &&
               y >= BOUNDARY_PADDING &&
               x + nodeWidth <= GAME_FIELD_WIDTH - BOUNDARY_PADDING &&
               y + nodeHeight <= GAME_FIELD_HEIGHT - BOUNDARY_PADDING;
    }
    
    /**
     * Aktualisiert das visuelle Feedback während des Drag & Drop.
     */
    private void updateDragFeedback(Node node, double x, double y) {
        if (isWithinBoundaries(node, x, y)) {
            node.getStyleClass().remove("invalid-placement");
            node.setOpacity(1.0);
        } else {
            node.getStyleClass().add("invalid-placement");
            node.setOpacity(0.5);
        }
    }
    
    /**
     * Event-Handler für Drag-Over-Events.
     */
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != editorCanvas &&
            event.getDragboard().hasString()) {
            
            Node draggedNode = (Node) event.getGestureSource();
            double x = event.getX();
            double y = event.getY();
            
            updateDragFeedback(draggedNode, x, y);
            
            if (isWithinBoundaries(draggedNode, x, y)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            
            event.consume();
        }
    }
    
    /**
     * Event-Handler für Drop-Events.
     */
    private void handleDrop(DragEvent event) {
        if (event.getGestureSource() != editorCanvas &&
            event.getDragboard().hasString()) {
            
            Node droppedNode = (Node) event.getGestureSource();
            double x = event.getX();
            double y = event.getY();
            
            if (isWithinBoundaries(droppedNode, x, y)) {
                // Objekt platzieren
                droppedNode.setLayoutX(x);
                droppedNode.setLayoutY(y);
                droppedNode.getStyleClass().remove("invalid-placement");
                droppedNode.setOpacity(1.0);
                
                // Command für Undo/Redo erstellen und ausführen
                PlaceObjectCommand command = new PlaceObjectCommand(
                    editorCanvas, droppedNode, gameObjects.get(droppedNode)
                );
                commandHistory.executeCommand(command);
                updateUndoRedoButtons();
            }
            
            event.setDropCompleted(true);
            event.consume();
        }
    }

    /**
     * Initialisiert das Spiel-Canvas und die GameEngine.
     */
    private void setupGameCanvas() {
        gameCanvas = new Canvas(GAME_FIELD_WIDTH, GAME_FIELD_HEIGHT);
        gameCanvas.setVisible(false);
        editorCanvas.getChildren().add(gameCanvas);
        
        gameEngine = new GameEngine(gameCanvas, world);
    }

    /**
     * Initialisiert den schwebenden Pause-Button.
     */
    private void setupFloatingPauseButton() {
        floatingPauseButton = new FloatingPauseButton(editorCanvas);
        floatingPauseButton.setVisible(false);
        floatingPauseButton.setOnAction(e -> onPlay());
        editorCanvas.getChildren().add(floatingPauseButton);
    }

    /**
     * Initialisiert den Timer-Text.
     */
    private void setupTimerText() {
        timerText = new Text("60.0");
        timerText.setStyle("-fx-font-size: 24px; -fx-fill: white; -fx-stroke: black; -fx-stroke-width: 1px;");
        timerText.setVisible(false);
        
        // Positioniere den Text in der oberen linken Ecke
        timerText.setX(10);
        timerText.setY(30);
        
        editorCanvas.getChildren().add(timerText);
    }
    
    /**
     * Initialisiert die Timer-Update-Schleife.
     */
    private void setupTimerUpdateLoop() {
        timerUpdateLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.getGameState() == GameEngine.GameState.RUNNING) {
                    double remainingTime = gameEngine.getRemainingTime();
                    timerText.setText(String.format("%.1f", remainingTime));
                }
            }
        };
    }

    /**
     * Initialisiert das GameOverOverlay.
     */
    private void setupGameOverOverlay() {
        gameOverOverlay = new GameOverOverlay(gameCanvas);
        
        // Setze die Callbacks
        gameOverOverlay.setOnRetry(() -> {
            gameOverOverlay.hide();
            stopSimulation();
            startSimulation(); // Starte das Level neu
        });
        
        gameOverOverlay.setOnMenu(() -> {
            gameOverOverlay.hide();
            stopSimulation();
            // Hier könnte später die Navigation zum Hauptmenü implementiert werden
        });
        
        editorCanvas.getChildren().add(gameOverOverlay);
    }

    /**
     * Wird aufgerufen, wenn das Spiel gewonnen wurde.
     */
    private void handleGameWon() {
        // Stoppe die Timer-Update-Schleife
        timerUpdateLoop.stop();
        
        // Zeige den Gewinn-Bildschirm an
        gameOverOverlay.showWinScreen();
    }
    
    /**
     * Wird aufgerufen, wenn das Spiel verloren wurde.
     */
    private void handleGameOver() {
        // Stoppe die Timer-Update-Schleife
        timerUpdateLoop.stop();
        
        // Zeige den Verlier-Bildschirm an
        gameOverOverlay.showLoseScreen();
    }

    /**
     * Wird aufgerufen, wenn der Play-Button geklickt wird.
     */
    @FXML
    private void onPlay() {
        if (!isPlaying) {
            startSimulation();
        } else {
            stopSimulation();
        }
    }
    
    /**
     * Startet die Spielsimulation.
     */
    private void startSimulation() {
        isPlaying = true;
        playButton.setText("Stop");
        
        // Bereite die Simulation vor
        prepareGameSimulation();
        
        // Deaktiviere Editor-Steuerelemente
        enableEditorControls(false);
        
        // Zeige das Spiel-Canvas und den Timer
        gameCanvas.setVisible(true);
        timerText.setVisible(true);
        
        // Starte die Timer-Update-Schleife
        timerUpdateLoop.start();
        
        // Starte die GameEngine
        gameEngine.start();
    }
    
    /**
     * Stoppt die Spielsimulation und räumt auf.
     */
    private void stopSimulation() {
        if (gameEngine != null) {
            gameEngine.reset();
            gameEngine = null;
        }
        
        // UI-Elemente zurücksetzen
        if (gameCanvas != null) {
            editorCanvas.getChildren().remove(gameCanvas);
            gameCanvas = null;
        }
        
        // Timer aufräumen
        if (timerUpdateLoop != null) {
            timerUpdateLoop.stop();
            timerUpdateLoop = null;
        }
        
        // Pause-Button entfernen
        if (floatingPauseButton != null) {
            editorCanvas.getChildren().remove(floatingPauseButton);
            floatingPauseButton = null;
        }
        
        // Timer-Text entfernen
        if (timerText != null) {
            editorCanvas.getChildren().remove(timerText);
            timerText = null;
        }
        
        isPlaying = false;
        enableEditorControls(true);
    }
    
    /**
     * Bereitet die Spielsimulation vor.
     */
    private void prepareGameSimulation() {
        // Übertrage alle GameObjects in die Spielwelt
        for (GameObject obj : gameObjects.values()) {
            world.addGameObject(obj);
        }
    }
    
    /**
     * Aktiviert oder deaktiviert die Editor-Steuerelemente.
     * @param enabled true um die Steuerelemente zu aktivieren
     */
    private void enableEditorControls(boolean enabled) {
        // Deaktiviere alle Steuerelemente außer den Play-Button
        for (Node node : editorCanvas.getChildren()) {
            if (node != gameCanvas) {
                node.setMouseTransparent(!enabled);
            }
        }
        
        // Deaktiviere Drag & Drop und Selektion
        for (Node node : editorCanvas.getChildren()) {
            if (node != gameCanvas) {
                node.setMouseTransparent(!enabled);
            }
        }
        
        if (enabled) {
            gameCanvas.setVisible(false);
            floatingPauseButton.setVisible(false);
        } else {
            gameCanvas.setVisible(true);
            floatingPauseButton.setVisible(true);
        }
    }

    /**
     * Stoppt die Spielsimulation.
     */
    private void stopGameSimulation() {
        stopSimulation();
    }

    /**
     * Schnellspeichern des aktuellen Levels.
     */
    private void quickSaveLevel() {
        // Prüfe, ob es bereits einen Speicherort gibt
        File levelsDir = new File(ResourceManager.getResourceDirectory(), "levels");
        File quickSaveFile = new File(levelsDir, "quicksave.json");
        
        // Erstelle das Verzeichnis, falls es nicht existiert
        if (!levelsDir.exists() && !levelsDir.mkdirs()) {
            showError("Speichern fehlgeschlagen", "Konnte Verzeichnis nicht erstellen");
            return;
        }
        
        try {
            saveLevelToFile(quickSaveFile);
            showSuccessMessage("Level wurde erfolgreich gespeichert");
        } catch (LevelSaveException e) {
            showError("Speichern fehlgeschlagen", e.getMessage());
        }
    }

    @FXML
    private void handleSaveLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Level");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                String jsonContent = createLevelJson();
                
                // Validate against schema before saving
                try {
                    schemaValidator.validate(jsonContent);
                } catch (ValidationException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Level validation failed");
                    alert.setContentText("The level data is invalid:\n" + e.getAllMessages().stream()
                        .collect(Collectors.joining("\n")));
                    alert.showAndWait();
                    return;
                }
                
                Files.writeString(file.toPath(), jsonContent);
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Level saved successfully");
                alert.setContentText("The level has been saved to " + file.getPath());
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save level");
                alert.setContentText("An error occurred while saving the level: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleLoadLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Level");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                String jsonContent = Files.readString(file.toPath());
                
                // Validate against schema before loading
                try {
                    schemaValidator.validate(jsonContent);
                } catch (ValidationException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Level validation failed");
                    alert.setContentText("The level file is invalid:\n" + e.getAllMessages().stream()
                        .collect(Collectors.joining("\n")));
                    alert.showAndWait();
                    return;
                }
                
                loadLevelFromJson(jsonContent);
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Level loaded successfully");
                alert.setContentText("The level has been loaded from " + file.getPath());
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not load level");
                alert.setContentText("An error occurred while loading the level: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void showValidationError(List<String> errors) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("The level file has validation errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String createLevelJson() {
        JsonObject levelData = new JsonObject();
        levelData.addProperty("name", levelNameField.getText());
        
        JsonArray objects = new JsonArray();
        for (GameObject obj : gameObjects.values()) {
            JsonObject objData = new JsonObject();
            objData.addProperty("type", obj.getClass().getSimpleName());
            
            JsonObject position = new JsonObject();
            position.addProperty("x", obj.getX());
            position.addProperty("y", obj.getY());
            objData.add("position", position);
            
            JsonObject properties = new JsonObject();
            properties.addProperty("density", obj.getDensity());
            properties.addProperty("friction", obj.getFriction());
            properties.addProperty("restitution", obj.getRestitution());
            objData.add("properties", properties);
            
            objects.add(objData);
        }
        levelData.add("objects", objects);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(levelData);
    }

    private void loadLevelFromJson(String jsonContent) {
        Gson gson = new Gson();
        JsonObject levelData = gson.fromJson(jsonContent, JsonObject.class);
        
        // Clear existing objects
        gameObjects.clear();
        
        // Set level name
        levelNameField.setText(levelData.get("name").getAsString());
        
        // Load objects
        JsonArray objects = levelData.getAsJsonArray("objects");
        for (JsonElement element : objects) {
            JsonObject objData = element.getAsJsonObject();
            String type = objData.get("type").getAsString();
            
            JsonObject position = objData.getAsJsonObject("position");
            float x = position.get("x").getAsFloat();
            float y = position.get("y").getAsFloat();
            
            JsonObject properties = objData.getAsJsonObject("properties");
            float density = properties.get("density").getAsFloat();
            float friction = properties.get("friction").getAsFloat();
            float restitution = properties.get("restitution").getAsFloat();
            
            GameObject obj = createGameObject(type, x, y);
            if (obj != null) {
                obj.setDensity(density);
                obj.setFriction(friction);
                obj.setRestitution(restitution);
                gameObjects.put(createPreviewNode(type, 40, 40), obj);
            }
        }
        
        // Refresh the view
        drawLevel();
    }

    private void drawLevel() {
        // Implementation of drawLevel method
    }
} 