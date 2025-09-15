package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import mm.model.LevelInfo;
import mm.objects.GameObject;
import mm.objects.balls.*;
import mm.objects.containers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller für den Game Editor (Level-Setup vor dem Spiel).
 * Hier platziert der Spieler Objekte aus seinem Inventar, bevor das Spiel startet.
 * Ähnliches Design wie der LevelEditor, aber für Spieler-Interaktion optimiert.
 */
public class GameEditorController extends GuiController {
    
    @FXML private BorderPane rootPane;
    @FXML private Pane editorCanvas;
    @FXML private Label taskLabel;
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button playButton;
    @FXML private ScrollPane inventoryScrollPane;
    @FXML private VBox inventoryContainer;
    @FXML private StackPane trashArea;
    @FXML private StackPane exitConfirmationOverlay;
    @FXML private Label unsavedChangesWarning;
    
    private LevelInfo currentLevel;
    private Map<String, Integer> availableObjects;
    private Map<String, Integer> originalObjectCounts;
    private List<PlacedObject> placedObjects;
    private List<StaticLevelObject> staticLevelObjects;
    private List<GameAction> undoStack;
    private List<GameAction> redoStack;
    private boolean hasUnsavedChanges = false;
    
    private final Map<Node, PlacedObject> nodeToObjectMap = new HashMap<>();
    private PlacedObject selectedObject;
    private Rectangle previewRect;
    
    private static final double GAME_FIELD_WIDTH = 800;
    private static final double GAME_FIELD_HEIGHT = 600;
    private static final double BOUNDARY_PADDING = 10;
    
    /**
     * Initialisiert den Game Editor mit einem Level.
     */
    public void initializeEditor(LevelInfo level) {
        this.currentLevel = level;
        this.availableObjects = new HashMap<>();
        this.originalObjectCounts = new HashMap<>();
        this.placedObjects = new ArrayList<>();
        this.staticLevelObjects = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        this.redoStack = new ArrayList<>();
        this.hasUnsavedChanges = false;
        
        // Aufgabe anzeigen
        taskLabel.setText(level.getDescription());
        
        // Canvas-Größe setzen
        editorCanvas.setPrefSize(GAME_FIELD_WIDTH, GAME_FIELD_HEIGHT);
        
        // Level-Daten laden
        loadLevelData();
        
        // UI-Komponenten initialisieren
        setupCanvas();
        setupInventory();
        setupEventHandlers();
        setupPreviewRect();
        setupTrashArea();
        
        // Tastenkombinationen einrichten
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupUndoRedo();
            }
        });
        
        updateUI();
        drawLevel();
    }
    
    @FXML
    public void initialize() {
        // Basis-Initialisierung - spezifische Initialisierung erfolgt in initializeEditor
    }
    
    /**
     * Lädt die Level-Daten aus der JSON-Datei.
     */
    private void loadLevelData() {
        // Hier würde normalerweise die JSON-Datei gelesen werden
        // Für jetzt verwenden wir Dummy-Daten
        
        // Beispiel: Level definiert verfügbare Objekte
        availableObjects.put("ball", 1);
        availableObjects.put("platform", 3);
        availableObjects.put("spring", 2);
        availableObjects.put("domino", 5);
        
        // Kopie für Reset-Funktion
        originalObjectCounts.putAll(availableObjects);
        
        // Level-spezifische statische Objekte laden
        loadStaticLevelObjects();
    }
    
    /**
     * Lädt die statischen Level-Objekte (nicht bewegbar).
     */
    private void loadStaticLevelObjects() {
        // Beispiel für statische Objekte
        staticLevelObjects.add(new StaticLevelObject("wall", 100, 100, 200, 20));
        staticLevelObjects.add(new StaticLevelObject("goal_zone", 600, 500, 100, 80));
    }
    
    /**
     * Richtet das Canvas ein.
     */
    private void setupCanvas() {
        editorCanvas.setOnMouseClicked(this::handleCanvasClick);
        editorCanvas.setOnDragOver(this::handleDragOver);
        editorCanvas.setOnDragDropped(this::handleDrop);
    }
    
    /**
     * Baut das Inventar auf.
     */
    private void setupInventory() {
        inventoryContainer.getChildren().clear();
        
        for (Map.Entry<String, Integer> entry : availableObjects.entrySet()) {
            String objectType = entry.getKey();
            int count = entry.getValue();
            
            if (count > 0) {
                InventoryItem item = new InventoryItem(objectType, count);
                inventoryContainer.getChildren().add(item.getNode());
                setupInventoryItemDrag(item);
            }
        }
    }
    
    /**
     * Richtet Drag & Drop für Inventar-Items ein.
     */
    private void setupInventoryItemDrag(InventoryItem item) {
        VBox itemNode = item.getNode();
        
        itemNode.setOnDragDetected(event -> {
            if (item.getCount() > 0) {
                Dragboard db = itemNode.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(item.getType());
                db.setContent(content);
                event.consume();
            }
        });
    }
    
    /**
     * Richtet Event-Handler ein.
     */
    private void setupEventHandlers() {
        // Canvas-Events sind bereits in setupCanvas() konfiguriert
    }
    
    /**
     * Richtet den Vorschau-Rechteck ein.
     */
    private void setupPreviewRect() {
        previewRect = new Rectangle();
        previewRect.setFill(Color.TRANSPARENT);
        previewRect.setStroke(Color.BLUE);
        previewRect.setStrokeWidth(2);
        previewRect.setVisible(false);
        editorCanvas.getChildren().add(previewRect);
    }
    
    /**
     * Richtet den Mülleimer ein.
     */
    private void setupTrashArea() {
        trashArea.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        trashArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                // Objekt vom Spielfeld entfernen und zum Inventar zurückgeben
                handleTrashDrop(event.getGestureSource());
                event.setDropCompleted(true);
            }
            event.consume();
        });
    }
    
    /**
     * Behandelt das Ablegen im Mülleimer.
     */
    private void handleTrashDrop(Object gestureSource) {
        if (gestureSource instanceof Node) {
            Node node = (Node) gestureSource;
            PlacedObject obj = nodeToObjectMap.get(node);
            if (obj != null) {
                // Objekt zum Inventar zurückgeben
                returnObjectToInventory(obj.getType());
                
                // Objekt vom Canvas entfernen
                removePlacedObject(obj);
                
                // Undo-Aktion hinzufügen
                addUndoAction(new RemoveObjectAction(obj));
                
                hasUnsavedChanges = true;
                updateUI();
                drawLevel();
            }
        }
    }
    
    /**
     * Behandelt Canvas-Klicks.
     */
    private void handleCanvasClick(MouseEvent event) {
        // Objekt-Auswahl
        Node clickedNode = (Node) event.getTarget();
        PlacedObject clickedObject = nodeToObjectMap.get(clickedNode);
        
        if (clickedObject != null && clickedObject != selectedObject) {
            selectObject(clickedObject);
        } else if (clickedObject == null) {
            deselectObject();
        }
    }
    
    /**
     * Behandelt Drag-Over-Events.
     */
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY);
            
            // Vorschau anzeigen
            String objectType = event.getDragboard().getString();
            updatePreviewRect(objectType, event.getX(), event.getY());
        }
        event.consume();
    }
    
    /**
     * Behandelt Drop-Events.
     */
    private void handleDrop(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasString()) {
            String objectType = db.getString();
            double x = event.getX();
            double y = event.getY();
            
            if (canPlaceObject(objectType, x, y)) {
                PlaceObjectAction action = new PlaceObjectAction(objectType, x, y);
                action.execute();
                addUndoAction(action);
                
                success = true;
                hasUnsavedChanges = true;
            }
        }
        
        previewRect.setVisible(false);
        event.setDropCompleted(success);
        event.consume();
        
        updateUI();
        drawLevel();
    }
    
    /**
     * Aktualisiert den Vorschau-Rechteck.
     */
    private void updatePreviewRect(String type, double x, double y) {
        double[] size = getObjectSize(type);
        double width = size[0];
        double height = size[1];
        
        previewRect.setX(x - width / 2);
        previewRect.setY(y - height / 2);
        previewRect.setWidth(width);
        previewRect.setHeight(height);
        previewRect.setVisible(true);
        
        // Farbe basierend auf Platzierbarkeit
        boolean canPlace = canPlaceObject(type, x, y);
        previewRect.setStroke(canPlace ? Color.GREEN : Color.RED);
    }
    
    /**
     * Prüft, ob ein Objekt platziert werden kann.
     */
    private boolean canPlaceObject(String type, double x, double y) {
        // Inventar-Check
        if (!availableObjects.containsKey(type) || availableObjects.get(type) <= 0) {
            return false;
        }
        
        // Grenzen-Check
        double[] size = getObjectSize(type);
        double width = size[0];
        double height = size[1];
        
        if (x - width/2 < BOUNDARY_PADDING || 
            x + width/2 > GAME_FIELD_WIDTH - BOUNDARY_PADDING ||
            y - height/2 < BOUNDARY_PADDING || 
            y + height/2 > GAME_FIELD_HEIGHT - BOUNDARY_PADDING) {
            return false;
        }
        
        // Überlappungs-Check
        Rectangle newObjectBounds = new Rectangle(x - width/2, y - height/2, width, height);
        
        // Mit platzierten Objekten prüfen
        for (PlacedObject obj : placedObjects) {
            if (objectsOverlap(newObjectBounds, obj.getBounds())) {
                return false;
            }
        }
        
        // Mit statischen Objekten prüfen
        for (StaticLevelObject obj : staticLevelObjects) {
            if (objectsOverlap(newObjectBounds, obj.getBounds())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Prüft, ob zwei Objekte überlappen.
     */
    private boolean objectsOverlap(Rectangle bounds1, Rectangle bounds2) {
        return bounds1.getBoundsInParent().intersects(bounds2.getBoundsInParent());
    }
    
    /**
     * Gibt die Standardgröße für einen Objekttyp zurück.
     */
    private double[] getObjectSize(String type) {
        switch (type) {
            case "ball": return new double[]{30, 30};
            case "platform": return new double[]{80, 20};
            case "spring": return new double[]{40, 40};
            case "domino": return new double[]{15, 40};
            default: return new double[]{30, 30};
        }
    }
    
    /**
     * Richtet Undo/Redo-Tastenkombinationen ein.
     */
    private void setupUndoRedo() {
        rootPane.getScene().getAccelerators().put(
            new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
            this::onUndo
        );
        rootPane.getScene().getAccelerators().put(
            new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
            this::onRedo
        );
    }
    
    /**
     * Aktualisiert die Benutzeroberfläche.
     */
    private void updateUI() {
        // Undo/Redo Buttons
        undoButton.setDisable(undoStack.isEmpty());
        redoButton.setDisable(redoStack.isEmpty());
        
        // Play Button (nur aktiviert wenn keine Überlappungen)
        playButton.setDisable(hasOverlappingObjects());
        
        // Exit-Warnung
        unsavedChangesWarning.setVisible(hasUnsavedChanges);
        
        // Inventar aktualisieren
        setupInventory();
    }
    
    /**
     * Prüft, ob sich Objekte überlappen.
     */
    private boolean hasOverlappingObjects() {
        for (int i = 0; i < placedObjects.size(); i++) {
            for (int j = i + 1; j < placedObjects.size(); j++) {
                if (objectsOverlap(placedObjects.get(i).getBounds(), 
                                 placedObjects.get(j).getBounds())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Zeichnet das Level neu.
     */
    private void drawLevel() {
        // Canvas leeren (außer Preview-Rechteck)
        editorCanvas.getChildren().removeIf(node -> node != previewRect);
        nodeToObjectMap.clear();
        
        // Statische Level-Objekte zeichnen
        for (StaticLevelObject obj : staticLevelObjects) {
            Node node = obj.createVisualNode();
            editorCanvas.getChildren().add(node);
        }
        
        // Platzierte Objekte zeichnen
        for (PlacedObject obj : placedObjects) {
            Node node = obj.createVisualNode();
            editorCanvas.getChildren().add(node);
            nodeToObjectMap.put(node, obj);
            setupDraggableObject(node, obj);
        }
    }
    
    /**
     * Macht ein Objekt verschiebbar.
     */
    private void setupDraggableObject(Node node, PlacedObject obj) {
        final Delta dragDelta = new Delta();
        
        node.setOnMousePressed(event -> {
            dragDelta.x = event.getSceneX() - node.getTranslateX();
            dragDelta.y = event.getSceneY() - node.getTranslateY();
            selectObject(obj);
        });
        
        node.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - dragDelta.x;
            double newY = event.getSceneY() - dragDelta.y;
            
            // Temporär verschieben für Kollisionsprüfung
            double oldX = obj.getX();
            double oldY = obj.getY();
            obj.setPosition(newX, newY);
            
            if (canMoveObject(obj, newX, newY)) {
                node.setTranslateX(newX);
                node.setTranslateY(newY);
            } else {
                // Zurücksetzen
                obj.setPosition(oldX, oldY);
            }
        });
        
        node.setOnMouseReleased(event -> {
            // Position finalisieren
            addUndoAction(new MoveObjectAction(obj, obj.getX(), obj.getY()));
            hasUnsavedChanges = true;
            updateUI();
        });
        
        // Drag-to-Trash aktivieren
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(obj.getType());
            db.setContent(content);
            event.consume();
        });
    }
    
    /**
     * Prüft, ob ein Objekt verschoben werden kann.
     */
    private boolean canMoveObject(PlacedObject movingObject, double newX, double newY) {
        // Grenzen-Check
        double[] size = getObjectSize(movingObject.getType());
        double width = size[0];
        double height = size[1];
        
        if (newX - width/2 < BOUNDARY_PADDING || 
            newX + width/2 > GAME_FIELD_WIDTH - BOUNDARY_PADDING ||
            newY - height/2 < BOUNDARY_PADDING || 
            newY + height/2 > GAME_FIELD_HEIGHT - BOUNDARY_PADDING) {
            return false;
        }
        
        // Überlappungs-Check mit anderen Objekten
        Rectangle newBounds = new Rectangle(newX - width/2, newY - height/2, width, height);
        
        for (PlacedObject obj : placedObjects) {
            if (obj != movingObject && objectsOverlap(newBounds, obj.getBounds())) {
                return false;
            }
        }
        
        for (StaticLevelObject obj : staticLevelObjects) {
            if (objectsOverlap(newBounds, obj.getBounds())) {
                return false;
            }
        }
        
        return true;
    }
    
    // Event-Handler
    
    @FXML
    private void onExit() {
        if (hasUnsavedChanges || !placedObjects.isEmpty()) {
            exitConfirmationOverlay.setVisible(true);
        } else {
            exitToLevelSelection();
        }
    }
    
    @FXML
    private void onConfirmExit() {
        exitConfirmationOverlay.setVisible(false);
        exitToLevelSelection();
    }
    
    @FXML
    private void onCancelExit() {
        exitConfirmationOverlay.setVisible(false);
    }
    
    private void exitToLevelSelection() {
        try {
            getViewManager().showLevelSelection();
        } catch (NavigationException e) {
            showError("Navigation fehlgeschlagen", e.getMessage());
        }
    }
    
    @FXML
    private void onReset() {
        // Alle platzierten Objekte entfernen
        placedObjects.clear();
        
        // Inventar zurücksetzen
        availableObjects.clear();
        availableObjects.putAll(originalObjectCounts);
        
        // Undo/Redo zurücksetzen
        undoStack.clear();
        redoStack.clear();
        
        hasUnsavedChanges = false;
        updateUI();
        drawLevel();
    }
    
    @FXML
    private void onPlay() {
        if (hasOverlappingObjects()) {
            showError("Überlappende Objekte", "Bitte entfernen Sie alle überlappenden Objekte, bevor Sie das Spiel starten.");
            return;
        }
        
        try {
            // Zum Game Controller wechseln
            GameController gameController = (GameController) getViewManager().showGame();
            
            // Spiel mit aktueller Level-Konfiguration und platzierten Objekten initialisieren
            gameController.initializeGame(currentLevel);
            
        } catch (NavigationException e) {
            showError("Fehler beim Spielstart", e.getMessage());
        }
    }
    
    @FXML
    private void onUndo() {
        if (!undoStack.isEmpty()) {
            GameAction action = undoStack.remove(undoStack.size() - 1);
            action.undo();
            redoStack.add(action);
            hasUnsavedChanges = true;
            updateUI();
            drawLevel();
        }
    }
    
    @FXML
    private void onRedo() {
        if (!redoStack.isEmpty()) {
            GameAction action = redoStack.remove(redoStack.size() - 1);
            action.redo();
            undoStack.add(action);
            hasUnsavedChanges = true;
            updateUI();
            drawLevel();
        }
    }
    
    // Hilfsmethoden und -klassen
    
    private void selectObject(PlacedObject obj) {
        selectedObject = obj;
        // Visuelles Feedback für Auswahl
    }
    
    private void deselectObject() {
        selectedObject = null;
    }
    
    private void addUndoAction(GameAction action) {
        undoStack.add(action);
        redoStack.clear(); // Redo-Stack leeren bei neuer Aktion
        
        // Undo-Stack begrenzen
        if (undoStack.size() > 20) {
            undoStack.remove(0);
        }
    }
    
    private void returnObjectToInventory(String type) {
        availableObjects.put(type, availableObjects.get(type) + 1);
    }
    
    private void removePlacedObject(PlacedObject obj) {
        placedObjects.remove(obj);
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
    
    // Hilfsklassen
    
    /**
     * Hilfklasse für Drag-Deltas.
     */
    private static class Delta {
        double x, y;
    }
    
    /**
     * Repräsentiert ein platziertes Objekt auf dem Spielfeld.
     */
    private class PlacedObject {
        private String type;
        private double x, y;
        
        public PlacedObject(String type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
        
        public Node createVisualNode() {
            double[] size = getObjectSize(type);
            Rectangle rect = new Rectangle(size[0], size[1]);
            rect.setFill(getColorForType(type));
            rect.setStroke(Color.BLACK);
            rect.setX(x - size[0]/2);
            rect.setY(y - size[1]/2);
            return rect;
        }
        
        public Rectangle getBounds() {
            double[] size = getObjectSize(type);
            return new Rectangle(x - size[0]/2, y - size[1]/2, size[0], size[1]);
        }
        
        private Color getColorForType(String type) {
            switch (type) {
                case "ball": return Color.RED;
                case "platform": return Color.BROWN;
                case "spring": return Color.GREEN;
                case "domino": return Color.GRAY;
                default: return Color.BLUE;
            }
        }
        
        // Getters and Setters
        public String getType() { return type; }
        public double getX() { return x; }
        public double getY() { return y; }
        public void setPosition(double x, double y) { this.x = x; this.y = y; }
    }
    
    /**
     * Repräsentiert ein statisches Level-Objekt.
     */
    private class StaticLevelObject {
        private String type;
        private double x, y, width, height;
        
        public StaticLevelObject(String type, double x, double y, double width, double height) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public Node createVisualNode() {
            Rectangle rect = new Rectangle(width, height);
            rect.setFill(getColorForType(type));
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(2);
            rect.setX(x);
            rect.setY(y);
            return rect;
        }
        
        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
        
        private Color getColorForType(String type) {
            switch (type) {
                case "wall": return Color.DARKGRAY;
                case "goal_zone": return Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.5);
                default: return Color.LIGHTBLUE;
            }
        }
    }
    
    /**
     * Repräsentiert ein Item im Inventar.
     */
    private class InventoryItem {
        private String type;
        private int count;
        private VBox node;
        
        public InventoryItem(String type, int count) {
            this.type = type;
            this.count = count;
            this.node = createNode();
        }
        
        private VBox createNode() {
            VBox item = new VBox(5);
            item.getStyleClass().add("inventory-item");
            item.setPadding(new Insets(10));
            item.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
            
            // Objekt-Icon (Platzhalter)
            Label icon = new Label(getIconForType(type));
            icon.getStyleClass().add("inventory-icon");
            icon.setStyle("-fx-font-size: 24px;");
            
            // Name
            Label nameLabel = new Label(getNameForType(type));
            nameLabel.getStyleClass().add("inventory-name");
            
            // Anzahl
            Label countLabel = new Label(String.valueOf(count));
            countLabel.getStyleClass().add("inventory-count");
            countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
            
            item.getChildren().addAll(icon, nameLabel, countLabel);
            return item;
        }
        
        private String getIconForType(String type) {
            switch (type) {
                case "ball": return "⚽";
                case "platform": return "▬";
                case "spring": return "🌀";
                case "domino": return "⬛";
                default: return "?";
            }
        }
        
        private String getNameForType(String type) {
            switch (type) {
                case "ball": return "Ball";
                case "platform": return "Plattform";
                case "spring": return "Feder";
                case "domino": return "Domino";
                default: return type;
            }
        }
        
        public VBox getNode() { return node; }
        public String getType() { return type; }
        public int getCount() { return count; }
    }
    
    /**
     * Repräsentiert eine rückgängig machbare Aktion.
     */
    private abstract class GameAction {
        public abstract void execute();
        public abstract void undo();
        public abstract void redo();
    }
    
    /**
     * Aktion für das Platzieren eines Objekts.
     */
    private class PlaceObjectAction extends GameAction {
        private String type;
        private double x, y;
        private PlacedObject placedObject;
        
        public PlaceObjectAction(String type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
        
        @Override
        public void execute() {
            placedObject = new PlacedObject(type, x, y);
            placedObjects.add(placedObject);
            
            // Inventar reduzieren
            availableObjects.put(type, availableObjects.get(type) - 1);
        }
        
        @Override
        public void undo() {
            placedObjects.remove(placedObject);
            returnObjectToInventory(type);
        }
        
        @Override
        public void redo() {
            execute();
        }
    }
    
    /**
     * Aktion für das Entfernen eines Objekts.
     */
    private class RemoveObjectAction extends GameAction {
        private PlacedObject object;
        
        public RemoveObjectAction(PlacedObject object) {
            this.object = object;
        }
        
        @Override
        public void execute() {
            placedObjects.remove(object);
            returnObjectToInventory(object.getType());
        }
        
        @Override
        public void undo() {
            placedObjects.add(object);
            availableObjects.put(object.getType(), availableObjects.get(object.getType()) - 1);
        }
        
        @Override
        public void redo() {
            execute();
        }
    }
    
    /**
     * Aktion für das Verschieben eines Objekts.
     */
    private class MoveObjectAction extends GameAction {
        private PlacedObject object;
        private double oldX, oldY, newX, newY;
        
        public MoveObjectAction(PlacedObject object, double newX, double newY) {
            this.object = object;
            this.oldX = object.getX();
            this.oldY = object.getY();
            this.newX = newX;
            this.newY = newY;
        }
        
        @Override
        public void execute() {
            object.setPosition(newX, newY);
        }
        
        @Override
        public void undo() {
            object.setPosition(oldX, oldY);
        }
        
        @Override
        public void redo() {
            object.setPosition(newX, newY);
        }
    }
} 