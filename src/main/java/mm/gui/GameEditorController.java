package mm.gui;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import mm.core.config.*;
import mm.core.editor.PlacedObject;
import mm.core.storage.LevelData;
import mm.core.storage.LevelStorage;
import mm.core.storage.Difficulty;
import javafx.scene.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.io.IOException;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import mm.core.config.DriveChainConf;

/**
 * Controller für den Game-Editor.
 */
public class GameEditorController extends Controller {

    // Statische Variablen zur Speicherung des letzten Zustands
    private static List<PlacedObject> savedPlayerObjects = new ArrayList<>();
    private static List<PlacedObject> savedLevelObjects = new ArrayList<>();
    private static String savedObjective = "Bringe den Ball in die Zielzone";
    private static Map<String, Integer> savedLimits = new HashMap<>();

    /** Verfügbare Objekttypen für Limits */
    private static final List<String> ITEM_TYPES = List.of(
            "tennisball", "bowlingball", "billiardball", "balloon", "log",
            "plank", "domino", "cratebox", "bucket", "smallgear", "largegear"
    );

    @FXML 
    private StackPane canvasRoot;

    @FXML
    private Pane editorCanvas;

    @FXML
    private VBox itemsContainer;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    @FXML
    private Label taskLabel;

    @FXML
    private Button rotateLeftButton;

    @FXML
    private Button rotateRightButton;

    private final List<PlacedObject> placedObjects = new ArrayList<>();
    private final List<PlacedObject> prePlacedObjects = new ArrayList<>();  // Objekte aus der Level-Datei
    
    /** Aktuell ausgewähltes Objekt für Rotation */
    private Node selectedNode = null;

    /** Stack für undo/redo */
    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 20;

    /** Aktuelle Limits für Objekte (aus dem geladenen Level) */
    private Map<String, Integer> currentLimits = new HashMap<>();

    /** Prüft, ob das Limit für einen Objekttyp erreicht wurde */
    private boolean checkLimitReached(String type) {
        long already = placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
        int allowed = currentLimits.getOrDefault(type, 999); // 999 = unbegrenzt falls nicht definiert
        return already >= allowed;
    }

    /** Gibt die aktuelle Anzahl eines Objekttyps zurück (nur vom Spieler platzierte) */
    private int getCurrentCount(String type) {
        return (int) placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
    }

    /** Lädt ein Level und setzt die Limits */
    public void loadLevel(String levelFileName) {
        try {
            Path levelPath = Path.of("src/main/resources/levels/" + levelFileName);
            LevelData levelData = LevelStorage.load(levelPath);
            
            // Objective aus dem Level laden und anzeigen
            String objective = levelData.getObjective();
            if (objective != null && !objective.trim().isEmpty()) {
                taskLabel.setText(objective);
            } else {
                taskLabel.setText("Aufgabe: Bringe den Ball in die Zielzone");
            }
            
            // Limits aus dem Level übernehmen
            currentLimits = new HashMap<>(levelData.getLimits());
            
            // Komplettes Reset (auch vorgeladene Objekte entfernen)
            editorCanvas.getChildren().clear();
            placedObjects.clear();
            prePlacedObjects.clear();
            
            // Level-Objekte laden (die bereits platzierten Objekte)
            for (ObjectConf conf : levelData.getObjects()) {
                double x = conf.getX() * 100; // Config ist in Meter, Canvas in Pixel
                double y = conf.getY() * 100;
                // Verwende skinId statt Klassenname für korrekte Objekttyp-Erkennung
                String type = conf.getSkinId();
                PlacedObject po = createPlacedObject(type, x, y, true); // Als vorgeladen markieren
                if (po != null) {
                    // Rotationswinkel aus der Konfiguration anwenden (von Radiant in Grad umrechnen)
                    po.getNode().setRotate(Math.toDegrees(conf.getAngle()));
                    
                    editorCanvas.getChildren().add(po.getNode());
                    prePlacedObjects.add(po); // In separate Liste speichern
                }
            }
            updateInventoryDisplay();
            
        } catch (IOException ex) {
            showDropWarning("Fehler beim Laden: " + ex.getMessage(), 5);
            ex.printStackTrace();
        }
    }

    private Image loadImage(String fileName) {
        try {
            return new Image(getClass().getResource("/assets/entities/" + fileName).toExternalForm());
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Bildes: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void initialize() {
        // Items zur Sidebar hinzufügen
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
        addInventoryItem("Kleines Zahnrad", "smallgear.png", "smallgear");
        addInventoryItem("Großes Zahnrad", "largegear.png", "largegear");
        
        // Antriebsstrang-Item hinzufügen (kein Drag&Drop, nur Klick)
        addDriveChainItem();

        setupCanvasDragDrop();
        updateButtonStates();
    }
    
    /**
     * Fügt das Antriebsstrang-Item zur Sidebar hinzu
     */
    private void addDriveChainItem() {
        VBox itemBox = new VBox(5);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
        
        // Icon (kann später durch echtes Bild ersetzt werden)
        Label iconLabel = new Label("⚙️⚡");
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        Label nameLabel = new Label("Antriebsstrang");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-alignment: center;");
        
        itemBox.getChildren().addAll(iconLabel, nameLabel);
        
        // Klick-Handler für Antriebsstrang-Modus
        itemBox.setOnMouseClicked(event -> {
            if (!driveChainMode) {
                enterDriveChainMode();
            } else {
                exitDriveChainMode();
            }
        });
        
        // Hover-Effekte
        itemBox.setOnMouseEntered(e -> itemBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #999; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;"));
        itemBox.setOnMouseExited(e -> {
            if (!driveChainMode) {
                itemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
            }
        });
        
        itemsContainer.getChildren().add(itemBox);
        driveChainItemBox = itemBox; // Referenz speichern
    }
    
    // Antriebsstrang-Modus Variablen
    private boolean driveChainMode = false;
    private VBox driveChainItemBox;
    private List<Node> driveChainConnections = new ArrayList<>(); // Gespeicherte Verbindungen
    
    // Bessere Zuordnung von Verbindungen zu Zahnrädern
    private static class DriveChainConnection {
        Line line;
        Node gearA;
        Node gearB;
        
        DriveChainConnection(Line line, Node gearA, Node gearB) {
            this.line = line;
            this.gearA = gearA;
            this.gearB = gearB;
        }
    }
    
    private List<DriveChainConnection> driveChainMappings = new ArrayList<>();
    
    /**
     * Aktiviert den Antriebsstrang-Modus
     */
    private void enterDriveChainMode() {
        driveChainMode = true;
        selectedGears.clear();
        
        // Visuelles Feedback
        driveChainItemBox.setStyle("-fx-background-color: #90EE90; -fx-border-color: #32CD32; -fx-border-width: 2; -fx-padding: 10; -fx-cursor: hand;");
        showHint("Antriebsstrang-Modus aktiviert\nKlicken Sie auf zwei Zahnräder");
    }
    
    /**
     * Deaktiviert den Antriebsstrang-Modus
     */
    private void exitDriveChainMode() {
        driveChainMode = false;
        
        // Auswahl zurücksetzen
        for (Node gear : selectedGears) {
            gear.setEffect(null);
        }
        selectedGears.clear();
        
        // Visuelles Feedback
        driveChainItemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
        showHint("Antriebsstrang-Modus deaktiviert");
    }
    
    /**
     * Erstellt eine Antriebsstrang-Verbindung zwischen zwei Zahnrädern
     */
    private void createDriveChainConnection(Node gearA, Node gearB) {
        // Linie zwischen den Zahnrad-Zentren erstellen
        Line connection = new Line();
        updateConnectionLine(connection, gearA, gearB);
        
        connection.setStroke(Color.DARKGRAY);
        connection.setStrokeWidth(3);
        connection.getStrokeDashArray().addAll(5d, 5d);
        
        // Verbindungs-Objekt erstellen
        DriveChainConnection mapping = new DriveChainConnection(connection, gearA, gearB);
        driveChainMappings.add(mapping);
        
        // Rechtsklick zum Löschen
        connection.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                editorCanvas.getChildren().remove(connection);
                driveChainConnections.remove(connection);
                driveChainMappings.removeIf(m -> m.line == connection);
                showHint("Antriebsstrang entfernt");
                event.consume();
            }
        });
        
        // Zur Canvas hinzufügen (hinter andere Objekte)
        editorCanvas.getChildren().add(0, connection);
        driveChainConnections.add(connection);
        
        showHint("Antriebsstrang erstellt! Rechtsklick zum Löschen");
    }
    
    /**
     * Aktualisiert die Position einer Verbindungslinie
     */
    private void updateConnectionLine(Line line, Node gearA, Node gearB) {
        double centerAX = gearA.getLayoutX();
        double centerAY = gearA.getLayoutY();
        double centerBX = gearB.getLayoutX();
        double centerBY = gearB.getLayoutY();
        
        line.setStartX(centerAX);
        line.setStartY(centerAY);
        line.setEndX(centerBX);
        line.setEndY(centerBY);
    }
    
    /**
     * Aktualisiert alle Verbindungslinien wenn Zahnräder bewegt werden
     */
    private void updateAllConnections() {
        for (DriveChainConnection mapping : driveChainMappings) {
            updateConnectionLine(mapping.line, mapping.gearA, mapping.gearB);
        }
    }
    
    /**
     * Hilfsmethode um Verbindungen für ein bewegtes Zahnrad zu aktualisieren
     */
    private void updateConnectionsForGear(Line line) {
        // Diese Methode ist nicht mehr nötig, da wir updateAllConnections() verwenden
    }

    private void addInventoryItem(String label, String imageFileName, String objectType) {
        VBox itemBox = new VBox(5);
        itemBox.setPrefSize(120, 80);
        itemBox.setStyle("-fx-border-color: black; -fx-background-color: white;");
        itemBox.setAlignment(Pos.CENTER);

        Image image = loadImage(imageFileName);
        if (image == null) {
            System.err.println("Konnte Bild nicht laden: " + imageFileName);
            return;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(true);

        Label text = new Label(label);
        text.setStyle("-fx-font-size: 10px;");

        // Counter Label hinzufügen
        Label counter = new Label("0/3");  // Standardwert, wird durch updateInventoryDisplay aktualisiert
        counter.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        itemBox.getChildren().addAll(imageView, text, counter);

        // Drag starten
        itemBox.setOnDragDetected(event -> {
            System.out.println("Starting drag for: " + objectType);
            Dragboard db = itemBox.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(objectType);
            db.setContent(content);
           
            // Setze ein visuelles Feedback für den Drag-Vorgang
            db.setDragView(image);
           
            event.consume();
        });

        // Füge zusätzliche Drag-Event-Handler hinzu
        itemBox.setOnDragDone(event -> {
            System.out.println("Drag done for: " + objectType);
            event.consume();
        });

        itemsContainer.getChildren().add(itemBox);
    }

    /** Prüft, ob das neue Node mit bestehenden platzierten Objekten überlappt (außer sich selbst). */
    private boolean overlapsExisting(Node n, Node ignore) {
        // Prüfung gegen vom Spieler platzierte Objekte
        for (PlacedObject po : placedObjects) {
            Node other = po.getNode();
            if (other == n || other == ignore) continue;
            if (checkObjectCollision(n, other)) {
                return true;
            }
        }
        
        // Prüfung gegen vorgeladene Objekte aus Level-Datei
        for (PlacedObject po : prePlacedObjects) {
            Node other = po.getNode();
            if (other == n || other == ignore) continue;
            if (checkObjectCollision(n, other)) {
                return true;
            }
        }
        
        return false;
    }
    
    /** 
     * Präzise Kollisionserkennung basierend auf tatsächlichen Objektformen.
     * Berücksichtigt Kreise, Rechtecke und spezielle Formen wie Buckets.
     * Goalzones sind transparent - Objekte können darin platziert werden.
     */
    private boolean checkObjectCollision(Node node1, Node node2) {
        // Goalzones sind transparent - keine Kollision
        if (isGoalzone(node1) || isGoalzone(node2)) {
            return false;
        }
        
        ObjectInfo obj1 = getObjectInfo(node1);
        ObjectInfo obj2 = getObjectInfo(node2);
        
        if (obj1 == null || obj2 == null) return false;
        
        // Bucket-speziale Behandlung: Objekte können IN Buckets platziert werden
        if (obj1.type == ObjectType.BUCKET) {
            return !isInsideBucket(obj2, obj1, node1) && checkGeneralCollision(obj1, obj2);
        }
        if (obj2.type == ObjectType.BUCKET) {
            return !isInsideBucket(obj1, obj2, node2) && checkGeneralCollision(obj1, obj2);
        }
        
        // Normale Kollisionsprüfung
        return checkGeneralCollision(obj1, obj2);
    }
    
    /** Hilfsmethode: Prüft ob ein Node eine Goalzone ist */
    private boolean isGoalzone(Node node) {
        // Prüfe sowohl platzierte als auch vorgeladene Objekte
        for (PlacedObject po : placedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == mm.core.config.GoalZoneConf.class;
            }
        }
        for (PlacedObject po : prePlacedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == mm.core.config.GoalZoneConf.class;
            }
        }
        return false;
    }
    
    /** Allgemeine Kollisionsprüfung zwischen zwei Objekten */
    private boolean checkGeneralCollision(ObjectInfo obj1, ObjectInfo obj2) {
        // Spezielle Zahnrad-zu-Zahnrad Kollision
        if (obj1.type == ObjectType.GEAR && obj2.type == ObjectType.GEAR) {
            return checkGearGearCollision(obj1, obj2);
        } else if (obj1.type == ObjectType.GEAR) {
            // Zahnrad zu normalem Objekt - verwende äußeren Radius
            return checkCircleToOtherCollision(obj1, obj2);
        } else if (obj2.type == ObjectType.GEAR) {
            // Normales Objekt zu Zahnrad - verwende äußeren Radius
            return checkCircleToOtherCollision(obj2, obj1);
        } else if (obj1.type == ObjectType.CIRCLE && obj2.type == ObjectType.CIRCLE) {
            return checkCircleCircleCollision(obj1, obj2);
        } else if (obj1.type == ObjectType.CIRCLE && obj2.type == ObjectType.RECTANGLE) {
            return checkCircleRectangleCollision(obj1, obj2);
        } else if (obj1.type == ObjectType.RECTANGLE && obj2.type == ObjectType.CIRCLE) {
            return checkCircleRectangleCollision(obj2, obj1);
        } else if (obj1.type == ObjectType.RECTANGLE && obj2.type == ObjectType.RECTANGLE) {
            return checkRectangleRectangleCollision(obj1, obj2);
        }
        
        // Fallback für unbekannte Kombinationen
        return obj1.bounds.intersects(obj2.bounds);
    }
    
    /** Spezielle Zahnrad-zu-Zahnrad Kollision */
    private boolean checkGearGearCollision(ObjectInfo gear1, ObjectInfo gear2) {
        double dx = gear1.centerX - gear2.centerX;
        double dy = gear1.centerY - gear2.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Exakte innere Radien basierend auf GearConf-Werten berechnen
        // SMALL: inner=0.365, outer=0.455 → ratio=0.365/0.455=0.8022
        // LARGE: inner=0.645, outer=0.75  → ratio=0.645/0.75=0.86
        double innerRadius1 = getInnerRadiusFromOuter(gear1.radius);
        double innerRadius2 = getInnerRadiusFromOuter(gear2.radius);
        
        // Zahnräder kollidieren nur wenn die inneren Kreise sich überlappen würden
        // Das erlaubt, dass die äußeren Ringe sich berühren (wie im Bild 2)
        double minDistance = innerRadius1 + innerRadius2;
        return distance < minDistance;
    }
    
    /** Berechnet den inneren Radius basierend auf dem äußeren Radius */
    private double getInnerRadiusFromOuter(double outerRadius) {
        // Bei 100px = 1m Skalierung (55% kleiner):
        // SMALL: outer=20.5px, inner=16.4px → middle=18.45px
        // LARGE: outer=33.8px, inner=29.0px → middle=31.4px
        
        if (Math.abs(outerRadius - 20.5) < 1) { // SMALL gear (±1px Toleranz)
            double innerRadius = 16.4;
            return (innerRadius + outerRadius) / 2; // Mitte zwischen inner und outer
        } else if (Math.abs(outerRadius - 33.8) < 1) { // LARGE gear  
            double innerRadius = 29.0;
            return (innerRadius + outerRadius) / 2; // Mitte zwischen inner und outer
        } else {
            // Fallback für unbekannte Größen
            return outerRadius * 0.9; // Etwas näher am äußeren Rand
        }
    }
    
    /** Zahnrad zu normalem Objekt Kollision */
    private boolean checkCircleToOtherCollision(ObjectInfo gear, ObjectInfo other) {
        if (other.type == ObjectType.CIRCLE) {
            double dx = gear.centerX - other.centerX;
            double dy = gear.centerY - other.centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double minDistance = gear.radius + other.radius + 1; // Äußerer Radius + Puffer
            return distance < minDistance;
        } else if (other.type == ObjectType.RECTANGLE) {
            return checkCircleRectangleCollision(gear, other);
        }
        return false;
    }
    
    /** Prüft ob ein Objekt IN einem Bucket platziert ist (erlaubt) - gegen tatsächliche Linien */
    private boolean isInsideBucket(ObjectInfo obj, ObjectInfo bucket, Node bucketNode) {
        if (bucket.type != ObjectType.BUCKET || !(bucketNode instanceof Group)) return false;
        
        Group bucketGroup = (Group) bucketNode;
        
        // Sammle alle Line-Objekte im Bucket
        java.util.List<javafx.scene.shape.Line> bucketLines = new java.util.ArrayList<>();
        for (Node child : bucketGroup.getChildren()) {
            if (child instanceof javafx.scene.shape.Line) {
                bucketLines.add((javafx.scene.shape.Line) child);
            }
        }
        
        if (bucketLines.isEmpty()) return false;
        
        // Prüfe ob das Objekt im Bucket-Innenraum ist (nicht mit Linien kollidiert)
        for (javafx.scene.shape.Line line : bucketLines) {
            if (checkObjectLineCollision(obj, line, bucketNode.getLayoutX(), bucketNode.getLayoutY())) {
                return false; // Kollision mit Bucket-Linie
            }
        }
        
        // Zusätzlich prüfen: Ist das Objekt grob im Bucket-Bereich?
        double objCenterX = obj.centerX;
        double objCenterY = obj.centerY;
        double bucketCenterX = bucket.centerX;
        double bucketCenterY = bucket.centerY;
        double bucketWidth = bucket.width;
        
        // Grobe Bereichsprüfung: Objekt sollte horizontal im Bucket-Bereich sein
        boolean roughlyInside = Math.abs(objCenterX - bucketCenterX) < bucketWidth / 2 + 10;
        
        return roughlyInside; // Im Bucket wenn keine Kollision und grob im Bereich
    }
    
    /** Prüft Kollision zwischen Objekt und einer Bucket-Linie */
    private boolean checkObjectLineCollision(ObjectInfo obj, javafx.scene.shape.Line line, double bucketX, double bucketY) {
        // Line-Koordinaten relativ zum Bucket + absolute Bucket-Position
        double x1 = line.getStartX() + bucketX;
        double y1 = line.getStartY() + bucketY;
        double x2 = line.getEndX() + bucketX;
        double y2 = line.getEndY() + bucketY;
        
        if (obj.type == ObjectType.CIRCLE) {
            return checkCircleLineCollision(obj.centerX, obj.centerY, obj.radius, x1, y1, x2, y2);
        } else {
            // Für Rechtecke: prüfe alle 4 Ecken gegen die Linie
            double[][] corners = getRectangleCorners(obj);
            for (double[] corner : corners) {
                if (checkPointToLineDistance(corner[0], corner[1], x1, y1, x2, y2) < 1) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /** Prüft Kollision zwischen Kreis und Linie */
    private boolean checkCircleLineCollision(double cx, double cy, double radius, double x1, double y1, double x2, double y2) {
        double distance = checkPointToLineDistance(cx, cy, x1, y1, x2, y2);
        return distance < radius + 1; // 1 Pixel Puffer
    }
    
    /** Berechnet kürzeste Distanz von Punkt zu Linie */
    private double checkPointToLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = -1;
        
        if (lenSq != 0) {
            param = dot / lenSq;
        }
        
        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        
        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /** Kreis-zu-Kreis Kollision */
    private boolean checkCircleCircleCollision(ObjectInfo circle1, ObjectInfo circle2) {
        double dx = circle1.centerX - circle2.centerX;
        double dy = circle1.centerY - circle2.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = circle1.radius + circle2.radius + 1; // 1 Pixel Puffer
        return distance < minDistance;
    }
    
    /** Kreis-zu-Rechteck Kollision */
    private boolean checkCircleRectangleCollision(ObjectInfo circle, ObjectInfo rect) {
        if (Math.abs(rect.rotation) > 0.1) { // Rotiertes Rechteck
            return checkCircleToRotatedRectangle(circle, rect);
        }
        
        // Nicht rotiertes Rechteck: Nächster Punkt zum Kreismittelpunkt
        double closestX = Math.max(rect.centerX - rect.width/2, 
                         Math.min(circle.centerX, rect.centerX + rect.width/2));
        double closestY = Math.max(rect.centerY - rect.height/2, 
                         Math.min(circle.centerY, rect.centerY + rect.height/2));
        
        double dx = circle.centerX - closestX;
        double dy = circle.centerY - closestY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance < (circle.radius + 1); // 1 Pixel Puffer
    }
    
    /** Rechteck-zu-Rechteck Kollision */
    private boolean checkRectangleRectangleCollision(ObjectInfo rect1, ObjectInfo rect2) {
        if (Math.abs(rect1.rotation) > 0.1 || Math.abs(rect2.rotation) > 0.1) {
            return checkRotatedRectangles(rect1, rect2);
        }
        
        // Beide nicht rotiert: AABB (Axis-Aligned Bounding Box) Kollision
        double puffer = 1; // 1 Pixel Abstand
        
        return !(rect1.centerX + rect1.width/2 + puffer <= rect2.centerX - rect2.width/2 ||
                 rect1.centerX - rect1.width/2 - puffer >= rect2.centerX + rect2.width/2 ||
                 rect1.centerY + rect1.height/2 + puffer <= rect2.centerY - rect2.height/2 ||
                 rect1.centerY - rect1.height/2 - puffer >= rect2.centerY + rect2.height/2);
    }
    
    /** Präzise Kollision zwischen Kreis und rotiertem Rechteck */
    private boolean checkCircleToRotatedRectangle(ObjectInfo circle, ObjectInfo rect) {
        // Kreis-Mittelpunkt in lokale Rechteck-Koordinaten transformieren
        double dx = circle.centerX - rect.centerX;
        double dy = circle.centerY - rect.centerY;
        
        // Inverse Rotation anwenden
        double cos = Math.cos(-rect.rotation);
        double sin = Math.sin(-rect.rotation);
        double localX = dx * cos - dy * sin;
        double localY = dx * sin + dy * cos;
        
        // Jetzt ist das Rechteck wieder achsenparallel - normale Kollisionsprüfung
        double closestX = Math.max(-rect.width/2, Math.min(localX, rect.width/2));
        double closestY = Math.max(-rect.height/2, Math.min(localY, rect.height/2));
        
        double distanceX = localX - closestX;
        double distanceY = localY - closestY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        return distance < (circle.radius + 1); // 1 Pixel Puffer
    }
    
    /** Präzise Kollision zwischen zwei rotierten Rechtecken */
    private boolean checkRotatedRectangles(ObjectInfo rect1, ObjectInfo rect2) {
        // Für rotierte Rechtecke: Separating Axis Theorem (SAT)
        // Vereinfachte Implementierung - prüfe alle 4 Achsen beider Rechtecke
        
        // Hole die 4 Ecken beider Rechtecke
        double[][] corners1 = getRectangleCorners(rect1);
        double[][] corners2 = getRectangleCorners(rect2);
        
        // Prüfe Separating Axes für beide Rechtecke
        double[][] axes = {
            {Math.cos(rect1.rotation), Math.sin(rect1.rotation)},      // Achse 1 von rect1
            {-Math.sin(rect1.rotation), Math.cos(rect1.rotation)},     // Achse 2 von rect1  
            {Math.cos(rect2.rotation), Math.sin(rect2.rotation)},      // Achse 1 von rect2
            {-Math.sin(rect2.rotation), Math.cos(rect2.rotation)}      // Achse 2 von rect2
        };
        
        for (double[] axis : axes) {
            if (isSeparatingAxis(corners1, corners2, axis)) {
                return false; // Separating axis gefunden - keine Kollision
            }
        }
        
        return true; // Keine separating axis gefunden - Kollision
    }
    
    /** Berechnet die 4 Ecken eines rotierten Rechtecks */
    private double[][] getRectangleCorners(ObjectInfo rect) {
        double halfWidth = rect.width / 2;
        double halfHeight = rect.height / 2;
        double cos = Math.cos(rect.rotation);
        double sin = Math.sin(rect.rotation);
        
        // Lokale Ecken (vor Rotation)
        double[][] localCorners = {
            {-halfWidth, -halfHeight}, {halfWidth, -halfHeight},
            {halfWidth, halfHeight}, {-halfWidth, halfHeight}
        };
        
        // Rotierte Ecken (um Zentrum rotiert)
        double[][] corners = new double[4][2];
        for (int i = 0; i < 4; i++) {
            double x = localCorners[i][0];
            double y = localCorners[i][1];
            corners[i][0] = rect.centerX + (x * cos - y * sin);
            corners[i][1] = rect.centerY + (x * sin + y * cos);
        }
        
        return corners;
    }
    
    /** Prüft ob eine Achse die beiden Polygone trennt */
    private boolean isSeparatingAxis(double[][] corners1, double[][] corners2, double[] axis) {
        // Projiziere alle Ecken auf die Achse
        double min1 = Double.MAX_VALUE, max1 = -Double.MAX_VALUE;
        double min2 = Double.MAX_VALUE, max2 = -Double.MAX_VALUE;
        
        for (double[] corner : corners1) {
            double projection = corner[0] * axis[0] + corner[1] * axis[1];
            min1 = Math.min(min1, projection);
            max1 = Math.max(max1, projection);
        }
        
        for (double[] corner : corners2) {
            double projection = corner[0] * axis[0] + corner[1] * axis[1];
            min2 = Math.min(min2, projection);
            max2 = Math.max(max2, projection);
        }
        
        // Prüfe Überlappung mit 1 Pixel Puffer
        return max1 + 1 < min2 || max2 + 1 < min1;
    }
    
    /** Hilfsmethode: Extrahiert Objektinformationen aus einem JavaFX Node */
    private ObjectInfo getObjectInfo(Node node) {
        double centerX, centerY, width = 0, height = 0, radius = 0;
        ObjectType type;
        javafx.geometry.Bounds bounds = node.localToParent(node.getBoundsInLocal());
        double rotation = Math.toRadians(node.getRotate()); // Node-Rotation in Radiant
        
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            centerX = node.getLayoutX() + circle.getCenterX();
            centerY = node.getLayoutY() + circle.getCenterY();
            radius = circle.getRadius();
            type = ObjectType.CIRCLE;
        } else if (node instanceof javafx.scene.shape.Rectangle) {
            javafx.scene.shape.Rectangle rect = (javafx.scene.shape.Rectangle) node;
            centerX = node.getLayoutX() + rect.getWidth() / 2;
            centerY = node.getLayoutY() + rect.getHeight() / 2;
            width = rect.getWidth();
            height = rect.getHeight();
            type = ObjectType.RECTANGLE;
        } else if (node instanceof Group) {
            Group group = (Group) node;
            centerX = node.getLayoutX();
            centerY = node.getLayoutY();
            
            // Spezielle Behandlung für Zahnräder
            if (isGear(node)) {
                // Zahnräder haben zwei Kreise - verwende den äußeren für Kollision
                double outerRadius = 0;
                for (Node child : group.getChildren()) {
                    if (child instanceof javafx.scene.shape.Circle) {
                        javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) child;
                        outerRadius = Math.max(outerRadius, circle.getRadius());
                    }
                }
                radius = outerRadius;
                type = ObjectType.GEAR;
            } else if (!group.getChildren().isEmpty() && group.getChildren().get(0) instanceof javafx.scene.shape.Line) {
                // Bucket-Behandlung
                javafx.scene.shape.Line bottomLine = (javafx.scene.shape.Line) group.getChildren().get(0);
                width = Math.abs(bottomLine.getEndX() - bottomLine.getStartX());
                height = 50; // Geschätzte Bucket-Höhe
                type = ObjectType.BUCKET;
            } else {
                // Fallback: verwende Bounds
                width = bounds.getWidth();
                height = bounds.getHeight();
                type = ObjectType.RECTANGLE;
            }
        } else {
            // Unbekannter Typ: verwende Bounds
            centerX = bounds.getCenterX();
            centerY = bounds.getCenterY();
            width = bounds.getWidth();
            height = bounds.getHeight();
            type = ObjectType.RECTANGLE;
        }
        
        return new ObjectInfo(type, centerX, centerY, width, height, radius, rotation, bounds);
    }
    
    /** Hilfsmethode: Prüft ob ein Node ein Zahnrad ist */
    private boolean isGear(Node node) {
        // Prüfe sowohl platzierte als auch vorgeladene Objekte
        for (PlacedObject po : placedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == mm.core.config.GearConf.class;
            }
        }
        for (PlacedObject po : prePlacedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == mm.core.config.GearConf.class;
            }
        }
        return false;
    }
    
    /** Objekttypen für Kollisionserkennung */
    private enum ObjectType {
        CIRCLE, RECTANGLE, BUCKET, GEAR
    }
    
    /** Objektinformationen für Kollisionserkennung */
    private static class ObjectInfo {
        final ObjectType type;
        final double centerX, centerY;
        final double width, height, radius, rotation;
        final javafx.geometry.Bounds bounds;
        
        ObjectInfo(ObjectType type, double centerX, double centerY, double width, double height, double radius, double rotation, javafx.geometry.Bounds bounds) {
            this.type = type;
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.rotation = rotation;
            this.bounds = bounds;
        }
    }

    private void setupCanvasDragDrop() {

        /* -------- DragOver -------- */
        canvasRoot.setOnDragOver(event -> {
            // NUR bei aktivem Drag&Drop aus der Sidebar reagieren
            if (event.getGestureSource() != null && 
                event.getGestureSource() != canvasRoot &&
                event.getGestureSource() != editorCanvas &&
                event.getDragboard().hasString()) {
    
                // Szene->Canvas-Koordinaten umrechnen (WICHTIG!)
                javafx.geometry.Point2D canvasPoint = editorCanvas.sceneToLocal(event.getSceneX(), event.getSceneY());
                
                System.out.println("DragOver - Scene: " + event.getSceneX() + ", " + event.getSceneY() + 
                                 " -> Canvas: " + canvasPoint.getX() + ", " + canvasPoint.getY() +
                                 " (Canvas size: " + editorCanvas.getWidth() + " x " + editorCanvas.getHeight() + ")");
    
                // Liegt der Punkt im Canvas? (0,0 bis width,height)
                boolean inside = canvasPoint.getX() >= 0 && canvasPoint.getX() <= editorCanvas.getWidth() &&
                               canvasPoint.getY() >= 0 && canvasPoint.getY() <= editorCanvas.getHeight();
    
                if (inside) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.consume(); // NUR bei echtem Drag&Drop aus Sidebar konsumieren
            }
        });
    
        /* -------- Drop -------- */
        canvasRoot.setOnDragDropped(event -> {
            if (event.getGestureSource() != null &&
                event.getGestureSource() != canvasRoot &&
                event.getGestureSource() != editorCanvas &&
                event.getDragboard().hasString()) {
        
                boolean success = false;
                Dragboard db = event.getDragboard();
        
                // Szene->Canvas-Koordinaten
                javafx.geometry.Point2D p = editorCanvas.sceneToLocal(event.getSceneX(), event.getSceneY());
        
                /* 1) Objekt vorläufig erstellen (setzt schon Layout-X/Y) */
                PlacedObject po = createPlacedObject(db.getString(), p.getX(), p.getY());
                if (po != null) {
                    Node n = po.getNode();
                    String objectType = db.getString();
        
                    /* 2) Limit-Check vor Platzierung */
                    if (checkLimitReached(objectType)) {
                        int current = getCurrentCount(objectType);
                        int allowed = currentLimits.getOrDefault(objectType, 999);
                        showDropWarning("Limit erreicht! " + objectType + ": " + current + "/" + allowed, 3);
                        return;
                    }
        
                    /* 3) Globale Bounds des Nodes berechnen                */
                    //  min/max im Canvas = Layout-Versatz + BoundsInLocal
                    javafx.geometry.Bounds bl = n.getBoundsInLocal();
                    double minX = n.getLayoutX() + bl.getMinX();
                    double minY = n.getLayoutY() + bl.getMinY();
                    double maxX = n.getLayoutX() + bl.getMaxX();
                    double maxY = n.getLayoutY() + bl.getMaxY();
        
                    boolean inside = minX >= 0 && minY >= 0 &&
                                     maxX <= editorCanvas.getWidth() &&
                                     maxY <= editorCanvas.getHeight();
        
                    // NEU: Überlappung prüfen
                    if (inside && !overlapsExisting(n, null)) {
                        editorCanvas.getChildren().add(n);
                        placedObjects.add(po);
                        push(new AddAction(po));
                        updateInventoryDisplay();
                        success = true;
                        System.out.printf("► platziert (%.1f, %.1f)%n",
                                          p.getX(), p.getY());
                    } else {
                        if (!inside) {
                            System.out.println("► Ablegen abgelehnt – ragt über den Rand");
                            showDropWarning("Can't drop here – place objects fully inside the field.", 5);
                        } else {
                            System.out.println("► Ablegen abgelehnt – würde überlappen");
                            showDropWarning("Can't drop here – objects must not overlap!", 5);
                        }
                    }
                }
        
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        // TEST: Canvas Mouse Events um zu prüfen ob Events überhaupt ankommen
        editorCanvas.setOnMousePressed(event -> {
            System.out.println("CANVAS Mouse PRESSED at: " + event.getX() + ", " + event.getY());
            // Event NICHT konsumieren, damit es an Kinder weitergegeben wird
        });
        
        editorCanvas.setOnMouseDragged(event -> {
            System.out.println("CANVAS Mouse DRAGGED at: " + event.getX() + ", " + event.getY());
            // Event NICHT konsumieren, damit es an Kinder weitergegeben wird
        });
        
        editorCanvas.setOnMouseReleased(event -> {
            System.out.println("CANVAS Mouse RELEASED at: " + event.getX() + ", " + event.getY());
            // Event NICHT konsumieren, damit es an Kinder weitergegeben wird
        });
        
        // WICHTIG: Alle Drag&Drop Event-Handler entfernen (aber NICHT Mouse-Events!)
        editorCanvas.setOnDragEntered(null);
        editorCanvas.setOnDragOver(null);
        editorCanvas.setOnDragDropped(null);

        // Canvas-Klick für Selektion entfernen
        editorCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Klick in leeren Bereich: Selektion entfernen
                clearSelection();
            }
        });
    }

    /** Erzeugt das passende GUI-Node + PlacedObject für den Drag-&-Drop-Typ. */
    private PlacedObject createPlacedObject(String type, double x, double y) {
        return createPlacedObject(type, x, y, false);
    }

    private PlacedObject createPlacedObject(String type, double x, double y, boolean isPrePlaced) {

        Node node;                       // JavaFX-Node, das im Canvas angezeigt wird
        Class<? extends ObjectConf> cfg; // zugehörige *Conf-Klasse
        
        // Skalierungsfaktor: Config-Werte (in Meter) → Pixel
        final double SCALE = 100.0; // 1 Meter = 100 Pixel
        
        // ------------------------------
        // Erzeuge das passende Node + *Conf-Objekt basierend auf Config-Werten
        // ------------------------------

        switch (type) {

            // ---------- Ball-Familie (erbt von BallConf) ----------
            case "tennisball": {
                TennisballConf tempConf = new TennisballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);  // Mittelpunkt im eigenen Koordinatensystem
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.YELLOW.darker() : Color.YELLOW);
                circle.setStroke(Color.BLACK);
                // Positionierung über Layout
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = TennisballConf.class;
                break;
            }

            case "bowlingball": {
                BowlingballConf tempConf = new BowlingballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.BLACK.brighter() : Color.BLACK);
                circle.setStroke(Color.DARKGRAY);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = BowlingballConf.class;
                break;
            }

            case "billiardball": {
                BilliardballConf tempConf = new BilliardballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.RED.darker() : Color.RED);
                circle.setStroke(Color.DARKRED);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = BilliardballConf.class;
                break;
            }

            case "balloon": {
                BalloonConf tempConf = new BalloonConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.LIGHTBLUE.darker() : Color.LIGHTBLUE);
                circle.setStroke(Color.BLUE);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = BalloonConf.class;
                break;
            }

            case "log": {                    // Log ist auch ein BallConf (runder Stamm)
                LogConf tempConf = new LogConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.SADDLEBROWN.darker() : Color.SADDLEBROWN);
                circle.setStroke(Color.DARKGOLDENROD);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = LogConf.class;
                break;
            }

            // ---------- Box-Familie (erbt von BoxConf) ----------
            case "plank": {                  // horizontale Planke (statisch)
                PlankConf tempConf = new PlankConf(0, 0, 0, true);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height, isPrePlaced ? Color.SADDLEBROWN.darker() : Color.SADDLEBROWN);
                rect.setStroke(Color.DARKGOLDENROD);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = PlankConf.class;
                break;
            }

            case "domino": {                 // schmale, stehende Box
                DominoConf tempConf = new DominoConf(0, 0, 0, false);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height, isPrePlaced ? Color.LIGHTGRAY.darker() : Color.LIGHTGRAY);
                rect.setStroke(Color.BLACK);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = DominoConf.class;
                break;
            }

            case "cratebox": {               // quadratische Kiste
                CrateboxConf tempConf = new CrateboxConf(0, 0, 0, false);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height, isPrePlaced ? Color.BURLYWOOD.darker() : Color.BURLYWOOD);
                rect.setStroke(Color.SADDLEBROWN);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = CrateboxConf.class;
                break;
            }

            // ---------- Spezialform ----------
            case "bucket": {                 // Bucket aus drei Linien (Boden + zwei schräge Seitenwände)
                BucketConf tempConf = new BucketConf(0, 0, 0, true); // Standard-Werte verwenden
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                double thickness = tempConf.getThickness() * SCALE;
                double wallAngle = tempConf.getWallAngle(); // 85° in Radiant
                
                // Gruppe für die drei Linien
                Group bucketGroup = new Group();
                
                Color bucketColor = isPrePlaced ? Color.BLUE.darker() : Color.BLUE;
                
                // Bodenlinie (horizontal, zentriert)
                Line bottomLine = new Line(-width/2, 0, width/2, 0);
                bottomLine.setStroke(bucketColor);
                bottomLine.setStrokeWidth(thickness);
                
                // Linke Seitenwand (schräg nach außen, 85°)
                double sideOffsetX = height * Math.cos(wallAngle);
                double sideOffsetY = height * Math.sin(wallAngle);
                Line leftWall = new Line(-width/2, 0, -width/2 - sideOffsetX, -sideOffsetY);
                leftWall.setStroke(bucketColor);
                leftWall.setStrokeWidth(thickness);
                
                // Rechte Seitenwand (schräg nach außen, 85°)
                Line rightWall = new Line(width/2, 0, width/2 + sideOffsetX, -sideOffsetY);
                rightWall.setStroke(bucketColor);
                rightWall.setStrokeWidth(thickness);
                
                bucketGroup.getChildren().addAll(bottomLine, leftWall, rightWall);
                bucketGroup.setLayoutX(x);
                bucketGroup.setLayoutY(y);
                node = bucketGroup;
                cfg = BucketConf.class;
                break;
            }

            // ---------- Spezielle Level-Objekte (nur in Level-Dateien) ----------
            case "gameball": {               // Spielball - ähnlich dem Tennisball
                GameBallConf tempConf = new GameBallConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(isPrePlaced ? Color.ORANGE.darker() : Color.ORANGE);
                circle.setStroke(Color.DARKORANGE);
                circle.setStrokeWidth(2);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = GameBallConf.class;
                break;
            }

            case "goalzone": {               // Goalzone - durchsichtige rechteckige Zone
                GoalZoneConf tempConf = new GoalZoneConf(0, 0, 0, true);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                // Durchsichtige grüne Zone mit gestricheltem Rand
                rect.setFill(Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.3)); // 30% Transparenz
                rect.setStroke(Color.GREEN);
                rect.setStrokeWidth(2);
                rect.getStrokeDashArray().addAll(5d, 5d); // Gestrichelter Rand
                
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = GoalZoneConf.class;
                break;
            }

            case "smallgear": {              // Kleines Zahnrad
                GearConf tempConf = new GearConf(0, 0, 0, false, GearConf.GearSize.SMALL);
                double innerRadius = tempConf.getInnerRadius() * SCALE;
                double outerRadius = tempConf.getOuterRadius() * SCALE;
                
                Group gearGroup = createGearVisualization(innerRadius, outerRadius, 
                                                        12, 
                                                        isPrePlaced ? Color.SILVER.darker() : Color.SILVER);
                gearGroup.setLayoutX(x);
                gearGroup.setLayoutY(y);
                node = gearGroup;
                cfg = GearConf.class;
                return new PlacedObject(node, cfg, "smallgear");
            }

            case "largegear": {              // Großes Zahnrad
                GearConf tempConf = new GearConf(0, 0, 0, false, GearConf.GearSize.LARGE);
                double innerRadius = tempConf.getInnerRadius() * SCALE;
                double outerRadius = tempConf.getOuterRadius() * SCALE;
                
                Group gearGroup = createGearVisualization(innerRadius, outerRadius, 
                                                        18, 
                                                        isPrePlaced ? Color.DARKGRAY.darker() : Color.DARKGRAY);
                gearGroup.setLayoutX(x);
                gearGroup.setLayoutY(y);
                node = gearGroup;
                cfg = GearConf.class;
                return new PlacedObject(node, cfg, "largegear");
            }

            case "drivechain": {             // Antriebsstrang
                // Prüfen ob zwei Zahnräder ausgewählt sind
                if (selectedGears.size() != 2) {
                    showHint("Bitte wählen Sie zuerst zwei Zahnräder aus\n(Strg+Klick für Mehrfachauswahl)");
                    return null;
                }
                
                // Zahnräder identifizieren
                Node gearA = selectedGears.get(0);
                Node gearB = selectedGears.get(1);
                
                // Antriebsstrang erstellen
                Group chainGroup = createDriveChainVisualization(
                    gearA.getLayoutX(), gearA.getLayoutY(),
                    gearB.getLayoutX(), gearB.getLayoutY()
                );
                chainGroup.setLayoutX(x);
                chainGroup.setLayoutY(y);
                node = chainGroup;
                cfg = DriveChainConf.class;
                break;
            }

            default:
                // Unbekannter String → null zurück = Platzierung verweigern
                return null;
        }

        // ------------------------------
        // Drag-Funktionalität für platzierte Objekte hinzufügen  
        // (nur wenn es kein vorgeladenes Objekt ist)
        // ------------------------------
        if (!isPrePlaced) {
            setupObjectDragging(node);
        } else {
            // Für vorgeladene Objekte: nur visuelles Feedback, kein Dragging/Löschen
            setupPrePlacedObjectEvents(node);
        }

        return new PlacedObject(node, cfg); // → PlacedObject-Objekt erzeugen
    }
    
    /**
     * Erstellt eine einfache JavaFX-Visualisierung für ein Zahnrad
     */
    private Group createGearVisualization(double innerRadius, double outerRadius, int numTeeth, Color color) {
        Group gearGroup = new Group();
        
        // Äußerer Kreis
        javafx.scene.shape.Circle outerCircle = new javafx.scene.shape.Circle(outerRadius);
        outerCircle.setFill(color);
        outerCircle.setStroke(color.darker());
        outerCircle.setStrokeWidth(2);
        
        // Innerer Kreis (heller)
        javafx.scene.shape.Circle innerCircle = new javafx.scene.shape.Circle(innerRadius);
        innerCircle.setFill(color.brighter());
        innerCircle.setStroke(color.darker());
        innerCircle.setStrokeWidth(1);
        
        gearGroup.getChildren().addAll(outerCircle, innerCircle);
        
        return gearGroup;
    }

    /**
     * Speichert den aktuellen Zustand für späteren Restore
     */
    public void saveCurrentState() {
        savedPlayerObjects = new ArrayList<>();
        for (PlacedObject po : placedObjects) {
            savedPlayerObjects.add(po.copy());
        }
        
        savedLevelObjects = new ArrayList<>();
        for (PlacedObject po : prePlacedObjects) {
            savedLevelObjects.add(po.copy());
        }
        
        savedObjective = taskLabel.getText();
        savedLimits = new HashMap<>(currentLimits);
    }

    /**
     * Stellt den gespeicherten Zustand wieder her
     */
    public void restoreState() {
        // Lösche aktuellen Inhalt
        editorCanvas.getChildren().clear();
        placedObjects.clear();
        prePlacedObjects.clear();
        
        // Stelle Level-Objekte wieder her (diese sollen nicht bewegbar/löschbar sein)
        for (PlacedObject po : savedLevelObjects) {
            PlacedObject restored = po.copy();
            editorCanvas.getChildren().add(restored.getNode());
            // Für Level-Objekte: nur visuelles Feedback, kein Dragging/Löschen
            setupPrePlacedObjectEvents(restored.getNode());
            prePlacedObjects.add(restored);
        }
        
        // Stelle Player-Objekte wieder her (diese sollen bewegbar/löschbar sein)
        for (PlacedObject po : savedPlayerObjects) {
            PlacedObject restored = po.copy();
            editorCanvas.getChildren().add(restored.getNode());
            
            // Füge vollständige Drag & Drop Funktionalität hinzu
            setupObjectDragging(restored.getNode());
            
            placedObjects.add(restored);
        }
        
        // Stelle UI-Zustand wieder her
        taskLabel.setText(savedObjective);
        currentLimits = new HashMap<>(savedLimits);
        
        updateInventoryDisplay();
    }

    /**
     * Setzt Ereignisse für vorgeladene Objekte (nur visuelles Feedback, kein Dragging/Löschen)
     */
    private void setupPrePlacedObjectEvents(Node node) {
        node.setPickOnBounds(true);
        node.setMouseTransparent(false);

        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }

        // Hover-Effekte
        node.setOnMouseEntered(event -> { 
            node.setOpacity(0.8); 
            editorCanvas.setCursor(Cursor.DEFAULT);
        });
        
        node.setOnMouseExited(event -> { 
            node.setOpacity(1); 
            editorCanvas.setCursor(Cursor.DEFAULT); 
        });

        // Andere Mouse-Events blockieren (kein Dragging für vorgeladene Objekte)
        node.setOnMousePressed(event -> event.consume());
        node.setOnMouseDragged(event -> event.consume());
        node.setOnMouseReleased(event -> event.consume());
    }

    /**
     * Fügt Drag-Funktionalität zu einem platzierten Objekt hinzu
     */
    private void setupObjectDragging(Node node) {
        final double[] lastMousePos = new double[2];

        /* Startposition für History */
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

        /* ---------- Press ---------- */
        node.setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;
            
            /* Start-Layout merken für UNDO/REDO */
            startPos[0] = node.getLayoutX();
            startPos[1] = node.getLayoutY();
            
            // Scene-Koordinaten der Maus merken
            lastMousePos[0] = event.getSceneX();
            lastMousePos[1] = event.getSceneY();
            
            node.setOpacity(0.7);
            node.toFront();
            System.out.println("Mouse PRESSED - Scene: " + event.getSceneX() + ", " + event.getSceneY() + 
                             " - Layout: " + node.getLayoutX() + ", " + node.getLayoutY());
            event.consume();
        });

        /* ---------- Click (für Zahnrad-Auswahl) ---------- */
        node.setOnMouseClicked(event -> {
            if (driveChainMode && isGear(node)) {
                // Antriebsstrang-Modus: Zahnräder auswählen
                if (selectedGears.contains(node)) {
                    selectedGears.remove(node);
                    node.setEffect(null);
                    showHint("Zahnrad abgewählt (" + selectedGears.size() + "/2)");
                } else if (selectedGears.size() < 2) {
                    selectedGears.add(node);
                    node.setEffect(new javafx.scene.effect.Glow(0.5));
                    showHint("Zahnrad ausgewählt (" + selectedGears.size() + "/2)");
                    
                    // Wenn 2 Zahnräder ausgewählt, Verbindung erstellen
                    if (selectedGears.size() == 2) {
                        createDriveChainConnection(selectedGears.get(0), selectedGears.get(1));
                        exitDriveChainMode();
                    }
                } else {
                    showHint("Maximal 2 Zahnräder können ausgewählt werden");
                }
                event.consume();
                return;
            } else if (event.isControlDown() && isGear(node)) {
                // Alte Strg+Klick Funktionalität für Kompatibilität
                if (selectedGears.contains(node)) {
                    selectedGears.remove(node);
                    node.setEffect(null);
                    showHint("Zahnrad abgewählt");
                } else if (selectedGears.size() < 2) {
                    selectedGears.add(node);
                    node.setEffect(new javafx.scene.effect.Glow(0.5));
                    showHint("Zahnrad ausgewählt (" + selectedGears.size() + "/2)");
                } else {
                    showHint("Maximal 2 Zahnräder können ausgewählt werden");
                }
                event.consume();
                return;
            }
            
            // Normale Auswahl für Rotation
            if (selectedNode != null) {
                removeHighlight(selectedNode);
            }
            selectedNode = node;
            highlightSelected(node);
            updateButtonStates();
            event.consume();
        });

        /* ---------- Drag ---------- */
        node.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;

            double deltaX = event.getSceneX() - lastMousePos[0];
            double deltaY = event.getSceneY() - lastMousePos[1];

            double newLayoutX = node.getLayoutX() + deltaX;
            double newLayoutY = node.getLayoutY() + deltaY;

            // Grenzen berechnen (wie bisher)
            javafx.geometry.Bounds localBounds = node.getBoundsInLocal();
            double minLX = -localBounds.getMinX();
            double minLY = -localBounds.getMinY();
            double maxRX = editorCanvas.getWidth() - localBounds.getMaxX();
            double maxDY = editorCanvas.getHeight() - localBounds.getMaxY();

            newLayoutX = clamp(newLayoutX, minLX, maxRX);
            newLayoutY = clamp(newLayoutY, minLY, maxDY);

            // Testweise neue Position setzen
            double oldX = node.getLayoutX();
            double oldY = node.getLayoutY();
            node.setLayoutX(newLayoutX);
            node.setLayoutY(newLayoutY);

            // Überlappung prüfen
            if (overlapsExisting(node, node)) {
                node.setLayoutX(oldX);
                node.setLayoutY(oldY);
                // Kein Warnhinweis beim Verschieben!
            } else {
                lastMousePos[0] = event.getSceneX();
                lastMousePos[1] = event.getSceneY();
                
                // Verbindungslinien aktualisieren wenn Zahnrad bewegt wird
                if (isGear(node)) {
                    updateAllConnections();
                }
            }

            event.consume();
        });

        /* ---------- Release / Hover ---------- */
        node.setOnMouseReleased(event -> { 
            node.setOpacity(1); 

            /* Ende-Layout vergleichen & History pushen für UNDO/REDO */
            double endX = node.getLayoutX();
            double endY = node.getLayoutY();
            if (endX != startPos[0] || endY != startPos[1]) {
                push(new MoveAction(node, startPos[0], startPos[1], endX, endY));
            }

            System.out.println("Mouse RELEASED");
            event.consume(); 
        });
        
        node.setOnMouseEntered(event -> { 
            node.setOpacity(0.8); 
            editorCanvas.setCursor(Cursor.HAND); 
            System.out.println("Mouse ENTERED");
        });
        
        node.setOnMouseExited(event -> { 
            node.setOpacity(1); 
            editorCanvas.setCursor(Cursor.DEFAULT); 
            System.out.println("Mouse EXITED");
        });
    }

    /* Utility */
    private static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : (v > hi) ? hi : v;
    }

    @FXML
    private void handlePlay() {
        System.out.println("Spiel starten");
        
        // Speichere den aktuellen Zustand vor dem Spiel
        saveCurrentState();
        
        if (viewManager != null) {
            // Aktuelle Objektlisten sammeln
            List<PlacedObject> playerObjects = new ArrayList<>(placedObjects);
            List<PlacedObject> levelObjects = new ArrayList<>(prePlacedObjects);
            String objective = taskLabel.getText();
            
            // Zum Game wechseln und Spiel initialisieren
            viewManager.showGame();
            GameController gameController = (GameController) viewManager.getLastController();
            if (gameController != null) {
                gameController.initializeGame(playerObjects, levelObjects, objective);
            }
        }
    }

    @FXML
    private void handleBack() {
        System.out.println("Zurück zur Level-Auswahl");
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }

    @FXML
    private void handleReset() {
        System.out.println("Level zurücksetzen");
        
        // Nur die vom Spieler platzierten Objekte entfernen
        for (PlacedObject po : placedObjects) {
            editorCanvas.getChildren().remove(po.getNode());
        }
        placedObjects.clear();
        
        // Vorgeladene Objekte bleiben bestehen

        // Undo/Redo-Verlauf zurücksetzen
        undoStack.clear();
        redoStack.clear();
        
        // Selektion zurücksetzen
        clearSelection();
        
        updateButtonStates();
        updateInventoryDisplay();
    }

    @FXML
    private void handleUndo() {
        System.out.println("Rückgängig");
        if (!undoStack.isEmpty()) {
            Action a = undoStack.pop();
            a.undo();
            redoStack.push(a);
            updateButtonStates();
        }
    }

    @FXML
    private void handleRedo() {
        System.out.println("Wiederholen");
        if (!redoStack.isEmpty()) {
            Action a = redoStack.pop();
            a.redo();
            undoStack.push(a);
            updateButtonStates();
        }
    }

    @FXML
    private void handleRotateLeft() {
        if (selectedNode != null) {
            double oldRotation = selectedNode.getRotate();
            double newRotation = oldRotation - 10.0; // 10 Grad nach links
            selectedNode.setRotate(newRotation);
            
            // Rotation-Aktion für Undo/Redo
            push(new RotateAction(selectedNode, oldRotation, newRotation));
        }
    }

    @FXML
    private void handleRotateRight() {
        if (selectedNode != null) {
            double oldRotation = selectedNode.getRotate();
            double newRotation = oldRotation + 10.0; // 10 Grad nach rechts
            selectedNode.setRotate(newRotation);
            
            // Rotation-Aktion für Undo/Redo
            push(new RotateAction(selectedNode, oldRotation, newRotation));
        }
    }



    /** Blendet für ein paar Sekunden eine Hinweisbox in der Mitte des Spielfelds ein. */
    private void showDropWarning(String message, int durationSeconds) {
        // Implementierung der Warnung
    }

    public interface Action {
        void undo();
        void redo();
    }

    /** Entsteht, wenn ein Objekt neu platziert wurde. */
    private class AddAction implements Action {
        private final Node node;
        private final PlacedObject po;
        private final int index;
        AddAction(PlacedObject po) {
            this.node  = po.getNode();
            this.po    = po;
            this.index = editorCanvas.getChildren().indexOf(node);
        }
        public void undo() {
            editorCanvas.getChildren().remove(node);
            placedObjects.remove(po);
            updateInventoryDisplay();
        }
        public void redo() {
            editorCanvas.getChildren().add(index, node);
            placedObjects.add(po);
            updateInventoryDisplay();
        }
    }
    
    /** Entsteht, wenn ein Objekt gelöscht wurde. */
    private class DeleteAction implements Action {
        private final Node node;
        private final PlacedObject po;
        private final int oldIndex;
        DeleteAction(PlacedObject po) {
            this.node     = po.getNode();
            this.po       = po;
            this.oldIndex = editorCanvas.getChildren().indexOf(node);
        }
        public void undo() {
            editorCanvas.getChildren().add(oldIndex, node);
            placedObjects.add(po);
            updateInventoryDisplay();
        }
        public void redo() {
            editorCanvas.getChildren().remove(node);
            placedObjects.remove(po);
            updateInventoryDisplay();
        }
    }

    /** Entsteht, wenn ein Objekt bewegt wurde (alter ≠ neuer Ort). */
    private class MoveAction implements Action {
        private final Node node;
        private final double ox, oy, nx, ny;

        MoveAction(Node node, double oldX, double oldY, double newX, double newY) {
            this.node = node;
            this.ox = oldX; this.oy = oldY;
            this.nx = newX; this.ny = newY;
        }
        @Override public void undo() { node.setLayoutX(ox); node.setLayoutY(oy); }
        @Override public void redo() { node.setLayoutX(nx); node.setLayoutY(ny); }
    }

    /** Entsteht, wenn ein Objekt rotiert wurde. */
    private class RotateAction implements Action {
        private final Node node;
        private final double oldRotation, newRotation;

        RotateAction(Node node, double oldRotation, double newRotation) {
            this.node = node;
            this.oldRotation = oldRotation;
            this.newRotation = newRotation;
        }
        @Override public void undo() { node.setRotate(oldRotation); }
        @Override public void redo() { node.setRotate(newRotation); }
    }

    /** Fügt eine neue Aktion hinzu, kappt den Verlauf auf 20 Einträge. */
    private void push(Action a) {
        undoStack.push(a);          // neueste oben
        redoStack.clear();          // alter Redo-Zweig verwerfen
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.removeLast(); // älteste abschneiden
        }
        updateButtonStates();       // Buttons aktiv/deaktiv
    }

    /** Aktiviert/Deaktiviert die Undo/Redo-Buttons (falls gewünscht) */
    private void updateButtonStates() {
        undoButton.setDisable(undoStack.isEmpty());
        redoButton.setDisable(redoStack.isEmpty());
        updateRotationButtons();
    }

    /** Aktiviert/Deaktiviert die Rotations-Buttons basierend auf der Selektion */
    private void updateRotationButtons() {
        boolean hasSelection = selectedNode != null;
        rotateLeftButton.setDisable(!hasSelection);
        rotateRightButton.setDisable(!hasSelection);
    }

    /** Entfernt die aktuelle Selektion */
    private void clearSelection() {
        if (selectedNode != null) {
            removeHighlight(selectedNode);
            selectedNode = null;
            updateRotationButtons();
        }
    }

    /** Markiert das ausgewählte Objekt visuell */
    private void highlightSelected(Node node) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStroke(Color.ORANGE);
            circle.setStrokeWidth(3);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStroke(Color.ORANGE);
            rect.setStrokeWidth(3);
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                if (child instanceof javafx.scene.shape.Circle) {
                    ((javafx.scene.shape.Circle) child).setStroke(Color.ORANGE);
                    ((javafx.scene.shape.Circle) child).setStrokeWidth(3);
                } else if (child instanceof Rectangle) {
                    ((Rectangle) child).setStroke(Color.ORANGE);
                    ((Rectangle) child).setStrokeWidth(3);
                }
            }
        }
    }

    /** Entfernt die visuelle Markierung */
    private void removeHighlight(Node node) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStroke(getOriginalStroke(rect));
            rect.setStrokeWidth(1);
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                if (child instanceof javafx.scene.shape.Circle) {
                    ((javafx.scene.shape.Circle) child).setStroke(Color.BLACK);
                    ((javafx.scene.shape.Circle) child).setStrokeWidth(1);
                } else if (child instanceof Rectangle) {
                    Rectangle rect = (Rectangle) child;
                    rect.setStroke(getOriginalStroke(rect));
                    rect.setStrokeWidth(1);
                }
            }
        }
    }

    /** Gibt die ursprüngliche Strichfarbe für Rechtecke zurück */
    private Color getOriginalStroke(Rectangle rect) {
        // Standard-Strichfarbe für Rechtecke
        return Color.BLACK;
    }

    /** Aktualisiert die Anzeige der Inventory-Items (zeigt verfügbare Anzahl) */
    private void updateInventoryDisplay() {
        // Für jedes Item in der Sidebar die verfügbare Anzahl anzeigen
        for (Node child : itemsContainer.getChildren()) {
            if (child instanceof VBox) {
                VBox itemBox = (VBox) child;
                if (itemBox.getChildren().size() >= 3) {  // Bild, Text und Counter
                    Label nameLabel = (Label) itemBox.getChildren().get(1);  // Text-Label
                    Label counter = (Label) itemBox.getChildren().get(2);    // Counter-Label
                    
                    // Objekttyp aus dem Namen ableiten
                    String objectType = nameLabel.getText().toLowerCase().replace(" ", "");
                    int current = getCurrentCount(objectType);
                    int max = currentLimits.getOrDefault(objectType, 999);
                    
                    if (max < 999) {
                        counter.setText(current + "/" + max);
                        
                        // Visuelles Feedback wenn Limit erreicht
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
    
    // Liste für ausgewählte Zahnräder (für Antriebsstrang)
    private List<Node> selectedGears = new ArrayList<>();
    
    @FXML private Label hintLabel;
    
    /**
     * Zeigt einen Hinweis für ein paar Sekunden an
     */
    private void showHint(String message) {
        if (hintLabel != null) {
            hintLabel.setText(message);
            hintLabel.setVisible(true);
            
            FadeTransition fade = new FadeTransition(Duration.seconds(3), hintLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> hintLabel.setVisible(false));
            fade.play();
        }
    }
    
    /**
     * Erstellt eine visuelle Darstellung für einen Antriebsstrang
     */
    private Group createDriveChainVisualization(double x1, double y1, double x2, double y2) {
        Group chainGroup = new Group();
        
        // Gestrichelte Linie
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.DARKGRAY);
        line.setStrokeWidth(5);
        line.getStrokeDashArray().addAll(5d, 5d); // Gestrichelte Linie
        
        // Kettenrad-Symbole an den Enden
        double symbolSize = 15;
        Circle symbolA = createChainSymbol(x1, y1, symbolSize);
        Circle symbolB = createChainSymbol(x2, y2, symbolSize);
        
        chainGroup.getChildren().addAll(line, symbolA, symbolB);
        return chainGroup;
    }
    
    /**
     * Erstellt ein kleines Kettenrad-Symbol
     */
    private Circle createChainSymbol(double x, double y, double size) {
        Circle symbol = new Circle(x, y, size/2);
        symbol.setFill(Color.DARKGRAY);
        symbol.setStroke(Color.BLACK);
        symbol.setStrokeWidth(1);
        return symbol;
    }
}   