package mm.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.Group;
import mm.domain.config.*;
import mm.domain.storage.LevelData;
import mm.domain.storage.LevelStorage;
import mm.domain.editor.PlacedObject;
import mm.service.command.CommandManager;
import mm.service.object.ObjectManager;
import mm.service.selection.SelectionHelper;
import mm.service.overlay.OverlayHelper;
import javafx.scene.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Path;
import java.io.IOException;

/**
 * Controller für den Game-Editor (Spielobjekt-Editor).
 * <p>
 * Ermöglicht das Platzieren, Verschieben, Löschen und Rotieren von Objekten auf dem Spielfeld.
 * Unterstützt Drag & Drop, Undo/Redo, Inventarverwaltung und das Speichern/Laden von Leveln.
 * Kommuniziert mit dem ViewManager für Szenenwechsel und mit Service-Klassen für Logik.
 * </p>
 */
public class GameEditorController extends Controller {

    private static List<PlacedObject> savedPlayerObjects = new ArrayList<>();
    private static List<PlacedObject> savedLevelObjects = new ArrayList<>();
    private static String savedObjective = "Bringe den Ball in die Zielzone";
    private static Map<String, Integer> savedLimits = new HashMap<>();

    /** Container für das Spielfeld und Overlays */
    @FXML 
    private StackPane canvasRoot;

    /** Zeichenfläche für das Spielfeld */
    @FXML
    private Pane editorCanvas;

    /** Container für die Inventar-Items */
    @FXML
    private VBox itemsContainer;

    /** Undo-Button */
    @FXML
    private Button undoButton;

    /** Redo-Button */
    @FXML
    private Button redoButton;

    /** Label für die Aufgabenbeschreibung */
    @FXML
    private Label taskLabel;

    /** Button für Rotation nach links */
    @FXML
    private Button rotateLeftButton;

    /** Button für Rotation nach rechts */
    @FXML
    private Button rotateRightButton;

    /** Aktuell ausgewähltes Node-Objekt */
    private Node selectedNode = null;

    /** Undo/Redo-Manager */
    private CommandManager commandManager;

    /** Objektmanager für platzierte Objekte */
    private ObjectManager objectManager;

    private boolean checkLimitReached(String type) {
        return objectManager.checkLimitReached(type);
    }
    
    private int getCurrentCount(String type) {
        return objectManager.getCurrentCount(type);
    }

    /**
     * Lädt ein Level aus einer Datei und platziert die Objekte.
     * @param levelFileName Dateiname des Levels
     */
    public void loadLevel(String levelFileName) {
        try {
            Path levelPath = Path.of("src/main/resources/levels/" + levelFileName);
            LevelData levelData = LevelStorage.load(levelPath);
            String objective = levelData.getObjective();
            if (objective != null && !objective.trim().isEmpty()) {
                taskLabel.setText(objective);
            } else {
                taskLabel.setText("Aufgabe: Bringe den Ball in die Zielzone");
            }
            
            objectManager = new ObjectManager(levelData.getLimits());
            
            editorCanvas.getChildren().clear();
            objectManager.clear();
            
            for (ObjectConf conf : levelData.getObjects()) {
                double x = conf.getX() * 100;
                double y = conf.getY() * 100;
                String type = conf.getClass().getSimpleName()
                                        .toLowerCase()
                                        .replace("conf", "");
                PlacedObject po = objectManager.createPlacedObject(type, x, y, true);
                if (po != null) {
                    po.getNode().setRotate(Math.toDegrees(conf.getAngle()));
                    
                    objectManager.addPrePlacedObject(po, editorCanvas);
                }
            }
            updateInventoryDisplay();
            
        } catch (IOException ex) {
            OverlayHelper.showWarning(canvasRoot, "Fehler beim Laden: " + ex.getMessage(), 5);

        }
    }

    private Image loadImage(String fileName) {
        try {
            return new Image(getClass().getResource("/assets/entities/" + fileName).toExternalForm());
        } catch (Exception e) {


            return null;
        }
    }

    /**
     * Initialisiert den Editor, das Inventar und die Event-Handler.
     * Wird automatisch von JavaFX nach dem Laden des FXML aufgerufen.
     */
    @FXML
    private void initialize() {
        if (objectManager == null) {
            objectManager = new ObjectManager();
        }
        editorCanvas.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
        commandManager = new CommandManager(undoButton, redoButton);

        itemsContainer.getChildren().clear();

        addInventoryItem("Tennisball", "tennisball.png", "tennisball");
        addInventoryItem("Bowlingball", "bowlingball.png", "bowlingball");
        addInventoryItem("Plank", "plank.png", "plank");
        addInventoryItem("Bucket", "bucket.png", "bucket");
        addInventoryItem("Log", "log.png", "log");
        addInventoryItem("Domino", "domino.png", "domino");
        addInventoryItem("Cratebox", "cratebox.png", "cratebox");
        addInventoryItem("Balloon", "balloon.png", "balloon");
        addInventoryItem("Billiardball", "billiardball.png", "billiardball");

        setupCanvasDragDrop();
        
        taskLabel.setText("Aufgabe: Bringe den Ball in die Zielzone");
        
        updateButtonStates();
    }

    /**
     * Fügt ein Inventar-Item hinzu.
     * @param label Anzeigename
     * @param imageFileName Bilddateiname
     * @param objectType Objekttyp
     */
    private void addInventoryItem(String label, String imageFileName, String objectType) {
        VBox itemBox = new VBox(5);
        itemBox.setPrefSize(140, 140);
        itemBox.getStyleClass().add("inventory-item");
        itemBox.setAlignment(Pos.CENTER);

        Image image = loadImage(imageFileName);
        if (image == null) {

            return;
        }
        ImageView imageView = new ImageView(image);
        
        if (objectType.equals("domino")) {
            imageView.setFitWidth(30);
            imageView.setFitHeight(40);
        } else {
            imageView.setFitWidth(50);
        }
        imageView.setPreserveRatio(true);

        Label text = new Label(label);
        text.setStyle("-fx-font-size: 10px;");

        Label counter = new Label("0/3");
        counter.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        itemBox.getChildren().addAll(imageView, text, counter);

        itemBox.setOnDragDetected(event -> {

            Dragboard db = itemBox.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(objectType);
            db.setContent(content);
           
            db.setDragView(image);
           
            event.consume();
        });

        itemBox.setOnDragDone(event -> {

            event.consume();
        });

        itemsContainer.getChildren().add(itemBox);
    }

    private boolean overlapsExisting(Node n, Node ignore) {
        return objectManager.overlapsExisting(n, ignore, editorCanvas);
    }
    


    /**
     * Setzt Drag & Drop-Handler für das Spielfeld.
     */
    private void setupCanvasDragDrop() {

        canvasRoot.setOnDragOver(event -> {
            if (event.getGestureSource() != null && 
                event.getGestureSource() != canvasRoot &&
                event.getGestureSource() != editorCanvas &&
                event.getDragboard().hasString()) {
    
                javafx.geometry.Point2D canvasPoint = editorCanvas.sceneToLocal(event.getSceneX(), event.getSceneY());
                
                boolean inside = canvasPoint.getX() >= 0 && canvasPoint.getX() <= editorCanvas.getWidth() &&
                               canvasPoint.getY() >= 0 && canvasPoint.getY() <= editorCanvas.getHeight();
    
                if (inside) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.consume();
            }
        });
    
        canvasRoot.setOnDragDropped(event -> {
            if (event.getGestureSource() != null &&
                event.getGestureSource() != canvasRoot &&
                event.getGestureSource() != editorCanvas &&
                event.getDragboard().hasString()) {
        
                boolean success = false;
                Dragboard db = event.getDragboard();
        
                javafx.geometry.Point2D p = editorCanvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        
                                PlacedObject po = objectManager.createPlacedObject(db.getString(), p.getX(), p.getY());
                if (po != null) {
                    Node n = po.getNode();
                    String objectType = db.getString();

                    if (checkLimitReached(objectType)) {
                        int current = getCurrentCount(objectType);
                        int allowed = objectManager.getCurrentLimits().getOrDefault(objectType, 999);
                        OverlayHelper.showWarning(canvasRoot, "Limit erreicht! " + objectType + ": " + current + "/" + allowed, 3);
                        return;
                    }

                    if (objectManager.isWithinBounds(n, editorCanvas) && !overlapsExisting(n, null)) {
                        objectManager.addPlacedObject(po, editorCanvas);
                        addDraggingToNode(po.getNode());
                        setupObjectSelection(po.getNode());
                        push(new CommandManager.AddAction(po, po.getNode(), objectManager.getPlacedObjects(), editorCanvas));
                        updateInventoryDisplay();
                        success = true;

                    } else {
                        if (!objectManager.isWithinBounds(n, editorCanvas)) {

                            OverlayHelper.showWarning(canvasRoot, "Can't drop here – place objects fully inside the field.", 5);
                        } else {

                            OverlayHelper.showWarning(canvasRoot, "Can't drop here – objects must not overlap!", 5);
                        }
                    }
                }
        
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        editorCanvas.setOnMousePressed(event -> {
        });
        
        editorCanvas.setOnMouseDragged(event -> {
        });
        
        editorCanvas.setOnMouseReleased(event -> {
        });
        
        editorCanvas.setOnDragEntered(null);
        editorCanvas.setOnDragOver(null);
        editorCanvas.setOnDragDropped(null);

        editorCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
            }
        });
    }



    /**
     * Speichert den aktuellen Zustand des Editors (Undo/Redo, Objekte, Limits).
     */
    public void saveCurrentState() {
        // Debug-Ausgabe
        System.out.println("Saving state - Player objects: " + objectManager.getPlacedObjects().size());
        
        savedPlayerObjects = new ArrayList<>();
        for (PlacedObject po : objectManager.getPlacedObjects()) {
            PlacedObject copy = po.copy();
            savedPlayerObjects.add(copy);
            System.out.println("Saved player object: " + copy.getConfigClass().getSimpleName() + 
                             " at (" + copy.getNode().getLayoutX() + ", " + copy.getNode().getLayoutY() + ")");
        }
        
        savedLevelObjects = new ArrayList<>();
        for (PlacedObject po : objectManager.getPrePlacedObjects()) {
            savedLevelObjects.add(po.copy());
        }
        
        savedObjective = taskLabel.getText();
        savedLimits = new HashMap<>(objectManager.getCurrentLimits());
        
        System.out.println("State saved - Player objects: " + savedPlayerObjects.size() + 
                         ", Level objects: " + savedLevelObjects.size());
    }

    /**
     * Stellt den gespeicherten Zustand des Editors wieder her.
     */
    public void restoreState() {
        System.out.println("Restoring state - Saved player objects: " + savedPlayerObjects.size());
        
        editorCanvas.getChildren().clear();
        objectManager.clear();
        
        // Level-Objekte wiederherstellen
        for (PlacedObject po : savedLevelObjects) {
            PlacedObject restored = po.copy();
            objectManager.addPrePlacedObject(restored, editorCanvas);
            objectManager.setupPrePlacedObjectEvents(restored.getNode());
        }
        
        // Spieler-Objekte wiederherstellen
        for (PlacedObject po : savedPlayerObjects) {
            PlacedObject restored = po.copy();
            objectManager.addPlacedObject(restored, editorCanvas);
            
            setupObjectDragging(restored.getNode());
            setupObjectSelection(restored.getNode());
            
            System.out.println("Restored player object: " + restored.getConfigClass().getSimpleName() + 
                             " at (" + restored.getNode().getLayoutX() + ", " + restored.getNode().getLayoutY() + ")");
        }
        
        taskLabel.setText(savedObjective);
        objectManager.setLimits(savedLimits);
        
        updateInventoryDisplay();
        
        System.out.println("State restored - Player objects: " + objectManager.getPlacedObjects().size() + 
                         ", Level objects: " + objectManager.getPrePlacedObjects().size());
    }

    private void setupObjectDragging(Node node) {
        final double[] lastMousePos = new double[2];

        final double[] startPos = new double[2];

        node.setPickOnBounds(true);
        node.setMouseTransparent(false);

        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }

        node.setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;
            
            startPos[0] = node.getLayoutX();
            startPos[1] = node.getLayoutY();
            
            lastMousePos[0] = event.getSceneX();
            lastMousePos[1] = event.getSceneY();
            
            node.setOpacity(0.7);
            node.toFront();

            event.consume();
        });

        node.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;

            double deltaX = event.getSceneX() - lastMousePos[0];
            double deltaY = event.getSceneY() - lastMousePos[1];

            double newLayoutX = node.getLayoutX() + deltaX;
            double newLayoutY = node.getLayoutY() + deltaY;

            javafx.geometry.Bounds localBounds = node.getBoundsInLocal();
            double minLX = -localBounds.getMinX();
            double minLY = -localBounds.getMinY();
            double maxRX = editorCanvas.getWidth() - localBounds.getMaxX();
            double maxDY = editorCanvas.getHeight() - localBounds.getMaxY();

            newLayoutX = clamp(newLayoutX, minLX, maxRX);
            newLayoutY = clamp(newLayoutY, minLY, maxDY);

            double oldX = node.getLayoutX();
            double oldY = node.getLayoutY();
            node.setLayoutX(newLayoutX);
            node.setLayoutY(newLayoutY);

            if (overlapsExisting(node, node)) {
                node.setLayoutX(oldX);
                node.setLayoutY(oldY);
            } else {
                lastMousePos[0] = event.getSceneX();
                lastMousePos[1] = event.getSceneY();
            }

            event.consume();
        });

        node.setOnMouseReleased(event -> { 
            node.setOpacity(1); 

            double endX = node.getLayoutX();
            double endY = node.getLayoutY();
            if (endX != startPos[0] || endY != startPos[1]) {
                push(new CommandManager.MoveAction(node, startPos[0], startPos[1], endX, endY));
            }

            event.consume(); 
        });
        
        node.setOnMouseEntered(event -> { 
            node.setOpacity(0.8); 
            editorCanvas.setCursor(Cursor.HAND); 

        });
        
        node.setOnMouseExited(event -> { 
            node.setOpacity(1); 
            editorCanvas.setCursor(Cursor.DEFAULT); 

        });

        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                PlacedObject po = objectManager.findPlacedObjectByNode(node);
        
                if (po != null) {
                    push(new CommandManager.DeleteAction(po, po.getNode(), objectManager.getPlacedObjects(), editorCanvas));
        
                    objectManager.removePlacedObject(po, editorCanvas);
                    updateInventoryDisplay();
        
                    if (selectedNode == node) {
                        selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                        SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
                    }
        
                    OverlayHelper.showWarning(canvasRoot, "Object deleted", 1);

                }
        
                event.consume();
            }
        });
    }

    /**
     * Fügt eine Aktion zum Undo/Redo-Stack hinzu.
     * @param a Die auszuführende Aktion
     */
    private void push(CommandManager.Action a) {
        commandManager.push(a);
    }

    /**
     * Aktualisiert den Status der Rotationsbuttons basierend auf der aktuellen Auswahl.
     */
    private void updateButtonStates() {
        SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
    }

    /**
     * Aktualisiert die Anzeige der Inventar-Items (Zähler, Aktivierung).
     */
    private void updateInventoryDisplay() {
        for (Node child : itemsContainer.getChildren()) {
            if (child instanceof VBox) {
                VBox itemBox = (VBox) child;
                if (itemBox.getChildren().size() >= 3) {
                    Label nameLabel = (Label) itemBox.getChildren().get(1);
                    Label counter = (Label) itemBox.getChildren().get(2);
                    String objectType = nameLabel.getText().toLowerCase().replace(" ", "");
                    int current = getCurrentCount(objectType);
                    int max = objectManager.getCurrentLimits().getOrDefault(objectType, 999);
                    if (max < 999) {
                        counter.setText(current + "/" + max);           
                        if (current >= max) {
                            counter.setStyle("-fx-font-size: 10px; -fx-text-fill: red; -fx-font-weight: bold;");
                            itemBox.setOpacity(0.5);
                            itemBox.setDisable(true);
                        } else {
                            counter.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                            itemBox.setOpacity(1.0);
                            itemBox.setDisable(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Fügt Dragging-Handler zu einem Node hinzu (für das Verschieben von Objekten).
     * @param node Das zu bearbeitende Node
     */
    private void addDraggingToNode(Node node) {
        final double[] lastMousePos = new double[2];
        final double[] startPos = new double[2];
        node.setPickOnBounds(true);
        node.setMouseTransparent(false);
        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }
        node.setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;
            startPos[0] = node.getLayoutX();
            startPos[1] = node.getLayoutY();
            lastMousePos[0] = event.getSceneX();
            lastMousePos[1] = event.getSceneY();
            node.setOpacity(0.7);
            node.toFront();
            event.consume();
        });
        node.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;
            double deltaX = event.getSceneX() - lastMousePos[0];
            double deltaY = event.getSceneY() - lastMousePos[1];
            double newLayoutX = node.getLayoutX() + deltaX;
            double newLayoutY = node.getLayoutY() + deltaY;
            javafx.geometry.Bounds localBounds = node.getBoundsInLocal();
            double minLX = -localBounds.getMinX();
            double minLY = -localBounds.getMinY();
            double maxRX = editorCanvas.getWidth() - localBounds.getMaxX();
            double maxDY = editorCanvas.getHeight() - localBounds.getMaxY();
            newLayoutX = clamp(newLayoutX, minLX, maxRX);
            newLayoutY = clamp(newLayoutY, minLY, maxDY);
            double oldX = node.getLayoutX();
            double oldY = node.getLayoutY();
            node.setLayoutX(newLayoutX);
            node.setLayoutY(newLayoutY);
            if (overlapsExisting(node, node)) {
                node.setLayoutX(oldX);
                node.setLayoutY(oldY);
            } else {
                lastMousePos[0] = event.getSceneX();
                lastMousePos[1] = event.getSceneY();
            }
            event.consume();
        });
        node.setOnMouseReleased(event -> { 
            node.setOpacity(1); 
            double endX = node.getLayoutX();
            double endY = node.getLayoutY();
            if (endX != startPos[0] || endY != startPos[1]) {
                push(new CommandManager.MoveAction(node, startPos[0], startPos[1], endX, endY));
            }
        });
        node.setOnMouseEntered(event -> { 
            node.setOpacity(0.8); 
            editorCanvas.setCursor(Cursor.HAND); 
        });
        node.setOnMouseExited(event -> { 
            node.setOpacity(1); 
            editorCanvas.setCursor(Cursor.DEFAULT); 
        });
    }

    /**
     * Klemmt einen Wert zwischen zwei Grenzen ein.
     * @param v Wert
     * @param lo Untere Grenze
     * @param hi Obere Grenze
     * @return Eingeklemmter Wert
     */
    private static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : (v > hi) ? hi : v;
    }

    @FXML
    private void handlePlay() {
        saveCurrentState();
        
        if (viewManager != null) {
            List<PlacedObject> playerObjects = new ArrayList<>(objectManager.getPlacedObjects());
            List<PlacedObject> levelObjects = new ArrayList<>(objectManager.getPrePlacedObjects());
            String objective = taskLabel.getText();
            
            viewManager.showGame();
            GameController gameController = (GameController) viewManager.getLastController();
            if (gameController != null) {
                gameController.initializeGame(playerObjects, levelObjects, objective);
            }
        }
    }

    /**
     * Handler für den Zurück-Button. Wechselt zurück zur Levelauswahl.
     */
    @FXML
    private void handleBack() {
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }

    /**
     * Setzt den Editor zurück: Entfernt alle platzierten Objekte, leert Undo/Redo und Auswahl.
     */
    @FXML
    private void handleReset() {
        for (PlacedObject po : objectManager.getPlacedObjects()) {
            editorCanvas.getChildren().remove(po.getNode());
        }
        objectManager.getPlacedObjects().clear();
        
        commandManager.clear();
        
        selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
        SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
        
        updateInventoryDisplay();
    }

    /**
     * Undo-Handler.
     */
    @FXML
    private void handleUndo() {
        commandManager.undo();
    }

    /**
     * Redo-Handler.
     */
    @FXML
    private void handleRedo() {
        commandManager.redo();
    }

    /**
     * Handler für die Rotation nach links (gegen den Uhrzeigersinn) des ausgewählten Objekts.
     */
    @FXML
    private void handleRotateLeft() {
        if (selectedNode != null) {
            double oldRotation = selectedNode.getRotate();
            double newRotation = oldRotation - 10.0;
            selectedNode.setRotate(newRotation);
            push(new CommandManager.RotateAction(selectedNode, oldRotation, newRotation));
            SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
        }
    }

    /**
     * Handler für die Rotation nach rechts (im Uhrzeigersinn) des ausgewählten Objekts.
     */
    @FXML
    private void handleRotateRight() {
        if (selectedNode != null) {
            double oldRotation = selectedNode.getRotate();
            double newRotation = oldRotation + 10.0;
            selectedNode.setRotate(newRotation);
            push(new CommandManager.RotateAction(selectedNode, oldRotation, newRotation));
            SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
        }
    }

    /**
     * Fügt Auswahl-Handler zu einem Node hinzu (für das Selektieren und Löschen von Objekten).
     * @param node Das zu bearbeitende Node
     */
    private void setupObjectSelection(Node node) {
        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                SelectionHelper.highlightSelected(node, editorCanvas);
                selectedNode = node; // Auswahl setzen
                SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                PlacedObject po = objectManager.findPlacedObjectByNode(node);
                if (po != null) {
                    push(new CommandManager.DeleteAction(po, po.getNode(), objectManager.getPlacedObjects(), editorCanvas));
                    objectManager.removePlacedObject(po, editorCanvas);
                    updateInventoryDisplay();
                    if (selectedNode == node) {
                        selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                        SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
                    }
                    OverlayHelper.showWarning(canvasRoot, "Object deleted", 1);
                }
                event.consume();
            }
        });
    }
}   