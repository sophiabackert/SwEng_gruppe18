package mm.gui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.Group;
import javafx.stage.FileChooser;
import mm.domain.config.*;
import mm.domain.storage.LevelStorage;
import mm.domain.storage.LevelData;
import mm.domain.storage.Difficulty;
import mm.domain.json.LevelValidator;
import mm.service.command.CommandManager;
import mm.service.object.ObjectManager;
import mm.service.selection.SelectionHelper;
import mm.service.overlay.OverlayHelper;
import mm.domain.editor.PlacedObject;

import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller für den Level-Editor.
 * <p>
 * Ermöglicht das Platzieren, Verschieben, Löschen und Rotieren von Objekten auf dem Spielfeld,
 * das Festlegen von Objekt-Limits, das Speichern und Laden von Leveln sowie Undo/Redo.
 * Unterstützt Drag & Drop, Inventarverwaltung und die Interaktion mit dem ViewManager.
 * </p>
 */
public class LevelEditorController extends Controller {
    /** Overlay-Container für das Spielfeld und Dialoge */
    @FXML private StackPane canvasRoot;
    /** Zeichenfläche für das Spielfeld */
    @FXML private Pane editorCanvas;
    /** Container für die Inventar-Items */
    @FXML private VBox itemsContainer;
    /** Undo-Button */
    @FXML private Button undoButton;
    /** Redo-Button */
    @FXML private Button redoButton;
    /** Button für Rotation nach links */
    @FXML private Button rotateLeftButton;
    /** Button für Rotation nach rechts */
    @FXML private Button rotateRightButton;
    /** Aktuell ausgewähltes Node-Objekt */
    private Node selectedNode = null;
    /** Undo/Redo-Manager */
    private CommandManager commandManager;
    /** Objektmanager für platzierte Objekte */
    private ObjectManager objectManager;
    /** Level-Metadaten (Name, Schwierigkeit, Ziel, Limits) */
    private LevelData lastMeta = new LevelData(
        "My level",
        Difficulty.EASY,
        "Reach the goal",
        List.of(),
        defaultLimits()
    );

    private static Map<String, Integer> defaultLimits() {
        List<String> types = List.of(
            "tennisball", "bowlingball", "billiardball", "balloon", "log",
            "plank", "domino", "cratebox", "bucket", "gameball", "goalzone", "restrictionzone"
        );
        return types.stream().collect(Collectors.toMap(t -> t, t -> 3));
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
        objectManager = new ObjectManager(defaultLimits()); // <- Initialisierung hinzugefügt
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
        addInventoryItem("Spielball", "gameball.png", "gameball");
        addInventoryItem("Goalzone", "goalzone.png", "goalzone");
        addInventoryItem("Restriction Zone", "restrictionzone.png", "restrictionzone");

        setupCanvasDragDrop();
        updateButtonStates();
        SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
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

        itemBox.getChildren().addAll(imageView, text);


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


                    if (objectManager.isUniqueItemLimitReached(objectType)) {
                        OverlayHelper.showWarning(canvasRoot, objectType + " kann nur einmal platziert werden!", 3);
                        return;
                    }


                    if (objectManager.isWithinBounds(n, editorCanvas) && !overlapsExisting(n, null)) {
                        objectManager.addPlacedObject(po, editorCanvas);
                        addDraggingToNode(po.getNode());
                        setupObjectSelection(po.getNode());
                        push(new CommandManager.AddAction(po, po.getNode(), objectManager.getPlacedObjects(), editorCanvas));
                        success = true;
                        System.out.printf("► platziert (%.1f, %.1f)%n",
                                          p.getX(), p.getY());
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
    }

    /**
     * Fügt Auswahl-Handler zu einem Node hinzu.
     * @param node Das zu bearbeitende Node
     */
    private void setupObjectSelection(Node node) {
        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
                SelectionHelper.highlightSelected(node, editorCanvas);
                selectedNode = node; // <--- Neu: ausgewähltes Objekt setzen
                SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                PlacedObject po = objectManager.findPlacedObjectByNode(node);
        
                if (po != null) {
                    push(new CommandManager.DeleteAction(po, po.getNode(), objectManager.getPlacedObjects(), editorCanvas));
                    objectManager.removePlacedObject(po, editorCanvas);
                    OverlayHelper.showWarning(canvasRoot, "Object deleted", 1);
                }

                event.consume();
            }
        });
    }

    @FXML
    private void handleBack() {
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }

    /**
     * Handler für das Zurücksetzen des Editors.
     */
    @FXML
    private void handleReset() {
        selectedNode = SelectionHelper.clearSelection(editorCanvas, selectedNode);
        editorCanvas.getChildren().clear();
        objectManager.clear();
        commandManager.clear();
    }

    /**
     * Handler für das Speichern eines Levels.
     */
    @FXML
    private void handleSave() {
        showMetaDialog().ifPresent(meta -> {
            try {
                List<ObjectConf> list = objectManager.getPlacedObjects().stream()
                        .map(PlacedObject::toConfig)
                        .collect(Collectors.toList());
                LevelData level = new LevelData(
                        meta.getName(),
                        meta.getDifficulty(),
                        meta.getObjective(),
                        list,
                        lastMeta.getLimits()
                );
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Level exportieren");
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Level-Dateien", "*.json")
                );
                fileChooser.setInitialFileName(level.getName().replaceAll("\\s+", "_").toLowerCase() + ".json");
                File file = fileChooser.showSaveDialog(editorCanvas.getScene().getWindow());
                if (file != null) {
                    if (!file.getName().toLowerCase().endsWith(".json")) {
                        file = new File(file.getParentFile(), file.getName() + ".json");
                    }
                    LevelStorage.save(level, file.toPath());
                    lastMeta = level;
                    OverlayHelper.showWarning(canvasRoot, "Level gespeichert: " + file.getName(), 3);
                }
            } catch (IOException ex) {
                OverlayHelper.showWarning(canvasRoot, "Speichern fehlgeschlagen: " + ex.getMessage(), 5);
            }
        });
    }

    /**
     * Handler für das Laden eines Levels.
     */
    @FXML
    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Level importieren");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Level-Dateien", "*.json")
        );
        File file = fileChooser.showOpenDialog(editorCanvas.getScene().getWindow());
        if (file != null) {
            try {
                if (!LevelValidator.isValidLevelFile(file)) {
                    OverlayHelper.showWarning(canvasRoot, "Ungültiges Level-Format", 5);
                    return;
                }
                LevelData level = LevelValidator.loadValidatedLevel(file);
                lastMeta = level;
                handleReset();
                for (ObjectConf conf : level.getObjects()) {
                    double x = conf.getX() * 100;
                    double y = conf.getY() * 100;
                    String type = conf.getClass().getSimpleName().toLowerCase().replace("conf", "");
                    PlacedObject po = objectManager.createPlacedObject(type, x, y);
                    if (po != null) {
                        po.getNode().setRotate(Math.toDegrees(conf.getAngle()));
                        objectManager.addPlacedObject(po, editorCanvas);
                        addDraggingToNode(po.getNode());
                        setupObjectSelection(po.getNode());
                    }
                }
                OverlayHelper.showWarning(canvasRoot, "Level geladen – " + level.getName(), 3);
            } catch (IllegalArgumentException ex) {
                OverlayHelper.showWarning(canvasRoot, "Ungültiges Level-Format: " + ex.getMessage(), 5);
            } catch (IOException ex) {
                OverlayHelper.showWarning(canvasRoot, "Fehler beim Laden: " + ex.getMessage(), 5);
            }
        }
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
     * Öffnet einen Dialog zur Eingabe der Level-Metadaten (Name, Schwierigkeit, Ziel).
     * @return Optional mit den eingegebenen Leveldaten
     */
    private Optional<LevelData> showMetaDialog() {
        Dialog<LevelData> dialog = new Dialog<>();
        dialog.setTitle("Level-Metadaten");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(lastMeta.getName());
        nameField.setPromptText("Name (max 20)");
        nameField.setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().length() <= 20 ? change : null));

        ComboBox<Difficulty> diffBox = new ComboBox<>();
        diffBox.getItems().setAll(Difficulty.values());
        diffBox.getSelectionModel().select(lastMeta.getDifficulty());

        TextField objField = new TextField(lastMeta.getObjective());
        objField.setPromptText("Objective (max 50)");
        objField.setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().length() <= 50 ? change : null));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Name:"),       nameField);
        grid.addRow(1, new Label("Difficulty:"), diffBox);
        grid.addRow(2, new Label("Objective:"),  objField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new LevelData(
                        nameField.getText().trim(),
                        diffBox.getValue(),
                        objField.getText().trim(),
                        null,
                        defaultLimits()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Öffnet einen Dialog zur Festlegung der Objekt-Limits.
     * @return Optional mit den eingegebenen Limits
     */
    private Optional<Map<String, Integer>> showLimitsDialog() {
        Dialog<Map<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Objekt-Limits festlegen");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new javafx.geometry.Insets(15, 25, 10, 25));

        Map<String, Spinner<Integer>> spinners = new HashMap<>();

        int row = 0;
        for (String type : defaultLimits().keySet()) {
            Label lbl = new Label(type + ":");
            Spinner<Integer> spin = new Spinner<>(0, 99, 
                    lastMeta.getLimits().getOrDefault(type, 3));
            spin.setEditable(true);
            spin.setPrefWidth(70);

            spinners.put(type, spin);
            grid.addRow(row++, lbl, spin);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return spinners.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().getValue()
                        ));
            }
            return null;
        });
        return dialog.showAndWait();
    }

    /**
     * Handler für das Festlegen der Objekt-Limits über einen Dialog.
     */
    @FXML
    private void handleObjectLimits() {
        showLimitsDialog().ifPresent(limits -> {
            lastMeta = new LevelData(
                    lastMeta.getName(),
                    lastMeta.getDifficulty(),
                    lastMeta.getObjective(),
                    lastMeta.getObjects(),
                    limits
            );
            OverlayHelper.showWarning(canvasRoot, "Objekt-Limits aktualisiert", 2);
        });
    }

    /**
     * Handler für das Starten des Spiels aus dem Editor (optional implementiert).
     */
    @FXML
    private void handlePlay() {

    }

    /**
     * Handler für Rotation nach links.
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
     * Handler für Rotation nach rechts.
     */
    @FXML
    private void handleRotateRight() {
        if (selectedNode != null) {
            double oldRotation = selectedNode.getRotate();
            double newRotation = oldRotation + 10.0; // 10 Grad nach rechts
            selectedNode.setRotate(newRotation);
            push(new CommandManager.RotateAction(selectedNode, oldRotation, newRotation));
            SelectionHelper.updateRotationButtons(rotateLeftButton, rotateRightButton, selectedNode);
        }
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
        // Button states are now managed by CommandManager
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
            newLayoutX = ObjectManager.clamp(newLayoutX, minLX, maxRX);
            newLayoutY = ObjectManager.clamp(newLayoutY, minLY, maxDY);
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
}