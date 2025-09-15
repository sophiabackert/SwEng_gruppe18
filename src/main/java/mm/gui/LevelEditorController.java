package mm.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.Group;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.geometry.Bounds;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import mm.core.config.*;
import mm.core.editor.PlacedObject;
import mm.core.storage.LevelStorage;
import mm.core.storage.LevelData;
import mm.core.storage.Difficulty;
import mm.core.json.LevelValidator;

import java.util.Optional;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Controller für den Level-Editor.
 */
public class LevelEditorController extends Controller {

    /** Verfügbare Objekttypen für Limits */
    private static final List<String> ITEM_TYPES = List.of(
            "tennisball", "bowlingball", "billiardball", "balloon", "log",
            "plank", "domino", "cratebox", "bucket", "gameball", "goalzone",
            "smallgear", "largegear", "paddle"
    );

    /** Objekttypen, die nur einmal platziert werden können */
    private static final List<String> UNIQUE_ITEMS = List.of("gameball", "goalzone");

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
    private Button rotateLeftButton;

    @FXML
    private Button rotateRightButton;

    @FXML 
    private Label hintLabel;

    private final List<PlacedObject> placedObjects = new ArrayList<>();
    
    /** Das aktuell ausgewählte Objekt für Rotation */
    private Node selectedNode = null;

    /** Stack für undo/redo */
    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 20;

    // Liste für ausgewählte Zahnräder (für Antriebsstrang)
    private List<Node> selectedGears = new ArrayList<>();
    
    // ID-Verwaltung für Zahnräder
    private final Map<Node, String> gearIds = new HashMap<>();  // Node -> eindeutige ID
    private final Map<String, Node> nodesByGearId = new HashMap<>();  // ID -> Node
    private int nextGearId = 1;  // Zähler für neue IDs
    private boolean isLoadingLevel = false;  // Flag für das Laden von Levels
    
    // Separate Speicherung der Zahnrad-Verbindungen
    private final Map<String, Set<String>> gearConnections = new HashMap<>();  // GearID -> Set von verbundenen GearIDs

    private LevelData lastMeta = new LevelData(
        "My level",            // Default-Name
        Difficulty.EASY,       // Default
        "Reach the goal",      // Default-Objective
        List.of(),             // wird beim Speichern überschrieben
        defaultLimits()        // 3 pro Typ
    );

    /** Erzeugt Standard-Limits (3 pro Objekttyp) */
    private static Map<String, Integer> defaultLimits() {
        return ITEM_TYPES.stream().collect(Collectors.toMap(t -> t, t -> 3));
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
        editorCanvas.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");

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
        addInventoryItem("Kleines Zahnrad", "smallgear.png", "smallgear");
        addInventoryItem("Großes Zahnrad", "largegear.png", "largegear");

        // Antriebsstrang-Item hinzufügen (kein Drag&Drop, nur Klick)
        addDriveChainItem();
        
        // Paddle-Item hinzufügen (kein Drag&Drop, nur Klick)
        addPaddleItem();

        setupCanvasDragDrop();
        
        // Initial button states setzen
        updateButtonStates();
        updateRotationButtons();
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

        itemBox.getChildren().addAll(imageView, text);

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
        for (PlacedObject po : placedObjects) {
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
        for (PlacedObject po : placedObjects) {
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
        for (PlacedObject po : placedObjects) {
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

                    /* 2) Prüfung für einzigartige Objekte */
                    if (UNIQUE_ITEMS.contains(objectType)) {
                        long existing = placedObjects.stream()
                                .map(po2 -> po2.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                                .filter(t -> t.equals(objectType))
                                .count();
                        if (existing >= 1) {
                            showDropWarning(objectType + " kann nur einmal platziert werden!", 3);
                            return;
                        }
                    }

                    /* 3) Globale Bounds des Nodes berechnen */
                    //  min/max im Canvas = Layout-Versatz + BoundsInLocal
                    javafx.geometry.Bounds bl = n.getBoundsInLocal();
                    double minX = n.getLayoutX() + bl.getMinX();
                    double minY = n.getLayoutY() + bl.getMinY();
                    double maxX = n.getLayoutX() + bl.getMaxX();
                    double maxY = n.getLayoutY() + bl.getMaxY();
                    
                    // Debug-Ausgabe für neue Objekte
                    if (objectType.equals("gameball") || objectType.equals("goalzone")) {
                        System.out.println("Debug " + objectType + ":");
                        System.out.println("  Drop-Position: (" + p.getX() + ", " + p.getY() + ")");
                        System.out.println("  LayoutX/Y: (" + n.getLayoutX() + ", " + n.getLayoutY() + ")");
                        System.out.println("  BoundsInLocal: " + bl);
                        System.out.println("  Canvas size: " + editorCanvas.getWidth() + " x " + editorCanvas.getHeight());
                        System.out.println("  Calculated bounds: X(" + minX + " to " + maxX + ") Y(" + minY + " to " + maxY + ")");
                    }

                    boolean inside = minX >= 0 && minY >= 0 &&
                                     maxX <= editorCanvas.getWidth() &&
                                     maxY <= editorCanvas.getHeight();
        
                    // NEU: Überlappung prüfen
                    if (inside && !overlapsExisting(n, null)) {
                        editorCanvas.getChildren().add(n);
                        setupObjectSelection(n);  // Auswahl-Funktionalität hinzufügen
                        placedObjects.add(po);
                        push(new AddAction(po));
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
        
        // WICHTIG: Stelle sicher, dass die Canvas keine eigenen Drag&Drop-Handler hat
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

        Node node;                       // JavaFX-Node, das im Canvas angezeigt wird
        Class<? extends ObjectConf> cfg; // zugehörige *Conf-Klasse
        
        // Skalierungsfaktor: Config-Werte (in Meter) -> Pixel
        final double SCALE = 100.0; // 1 Meter = 100 Pixel
        
        // Erzeuge das passende Node + *Conf-Objekt basierend auf Config-Werten
        switch (type) {

            // ---------- Ball-Familie (erbt von BallConf) ----------
            case "tennisball": {
                TennisballConf tempConf = new TennisballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);  // Mittelpunkt im eigenen Koordinatensystem
                circle.setCenterY(radius);
                circle.setFill(Color.YELLOW);
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
                circle.setFill(Color.BLACK);
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
                circle.setFill(Color.RED);
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
                circle.setFill(Color.LIGHTBLUE);
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
                circle.setFill(Color.SADDLEBROWN);
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
                Rectangle rect = new Rectangle(width, height, Color.SADDLEBROWN);
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
                Rectangle rect = new Rectangle(width, height, Color.LIGHTGRAY);
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
                Rectangle rect = new Rectangle(width, height, Color.BURLYWOOD);
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
                
                // Bodenlinie (horizontal, zentriert)
                Line bottomLine = new Line(-width/2, 0, width/2, 0);
                bottomLine.setStroke(Color.BLUE);
                bottomLine.setStrokeWidth(thickness);
                
                // Linke Seitenwand (schräg nach außen, 85°)
                double sideOffsetX = height * Math.cos(wallAngle);
                double sideOffsetY = height * Math.sin(wallAngle);
                Line leftWall = new Line(-width/2, 0, -width/2 - sideOffsetX, -sideOffsetY);
                leftWall.setStroke(Color.BLUE);
                leftWall.setStrokeWidth(thickness);
                
                // Rechte Seitenwand (schräg nach außen, 85°)
                Line rightWall = new Line(width/2, 0, width/2 + sideOffsetX, -sideOffsetY);
                rightWall.setStroke(Color.BLUE);
                rightWall.setStrokeWidth(thickness);
                
                bucketGroup.getChildren().addAll(bottomLine, leftWall, rightWall);
                bucketGroup.setLayoutX(x);
                bucketGroup.setLayoutY(y);
                node = bucketGroup;
                cfg = BucketConf.class;
                break;
            }

            // ---------- Spezielle Level-Objekte ----------
            case "gameball": {               // Spielball - ähnlich dem Tennisball
                GameBallConf tempConf = new GameBallConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(Color.ORANGE);
                circle.setStroke(Color.DARKORANGE);
                circle.setStrokeWidth(2);
                // Korrigierte Positionierung wie bei anderen Kreisen
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
                
                // Korrigierte Positionierung wie bei anderen Rechtecken
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
                                                        12, Color.SILVER);
                gearGroup.setLayoutX(x);
                gearGroup.setLayoutY(y);
                node = gearGroup;
                cfg = GearConf.class;
                
                // Eindeutige ID für Zahnrad vergeben
                String gearId;
                
                // WICHTIG: Beim Laden verwende vorhersagbare IDs basierend auf der Reihenfolge
                // Erkennung: Wenn mehrere Objekte schnell hintereinander erstellt werden (Laden)
                if (!placedObjects.isEmpty() && !isLoadingLevel) {
                    // Prüfe ob wir gerade laden (mehrere Objekte in kurzer Zeit)
                    isLoadingLevel = true;
                    System.out.println("Loading detected - switching to sequential gear IDs");
                }
                
                if (isLoadingLevel) {
                    // Beim Laden: Verwende sequenzielle IDs (gear1, gear2, etc.)
                    int gearCount = (int) placedObjects.stream()
                        .filter(po -> po.getConfigClass() == GearConf.class)
                        .count() + 1;
                    gearId = "gear" + gearCount;
                    System.out.println("Loading small gear: Assigned gear ID: " + gearId);
                } else {
                    // Beim normalen Erstellen: Verwende zufällige IDs
                    gearId = "gear" + nextGearId++;
                    System.out.println("Creating small gear: Assigned gear ID: " + gearId);
                }
                
                gearIds.put(gearGroup, gearId);
                nodesByGearId.put(gearId, gearGroup);
                
                // Drag-Funktionalität hinzufügen
                setupObjectDragging(node);
                return new PlacedObject(node, cfg, "smallgear");
            }

            case "largegear": {              // Großes Zahnrad
                GearConf tempConf = new GearConf(0, 0, 0, false, GearConf.GearSize.LARGE);
                double innerRadius = tempConf.getInnerRadius() * SCALE;
                double outerRadius = tempConf.getOuterRadius() * SCALE;
                
                Group gearGroup = createGearVisualization(innerRadius, outerRadius, 
                                                        18, Color.DARKGRAY);
                gearGroup.setLayoutX(x);
                gearGroup.setLayoutY(y);
                node = gearGroup;
                cfg = GearConf.class;
                
                // Eindeutige ID für Zahnrad vergeben
                String gearId;
                
                // WICHTIG: Beim Laden verwende vorhersagbare IDs basierend auf der Reihenfolge
                // Erkennung: Wenn mehrere Objekte schnell hintereinander erstellt werden (Laden)
                if (!placedObjects.isEmpty() && !isLoadingLevel) {
                    // Prüfe ob wir gerade laden (mehrere Objekte in kurzer Zeit)
                    isLoadingLevel = true;
                    System.out.println("Loading detected - switching to sequential gear IDs");
                }
                
                if (isLoadingLevel) {
                    // Beim Laden: Verwende sequenzielle IDs (gear1, gear2, etc.)
                    int gearCount = (int) placedObjects.stream()
                        .filter(po -> po.getConfigClass() == GearConf.class)
                        .count() + 1;
                    gearId = "gear" + gearCount;
                    System.out.println("Loading large gear: Assigned gear ID: " + gearId);
                } else {
                    // Beim normalen Erstellen: Verwende zufällige IDs
                    gearId = "gear" + nextGearId++;
                    System.out.println("Creating large gear: Assigned gear ID: " + gearId);
                }
                
                gearIds.put(gearGroup, gearId);
                nodesByGearId.put(gearId, gearGroup);
                
                // Drag-Funktionalität hinzufügen
                setupObjectDragging(node);
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
            
            case "paddle": {                // Paddle (beim Laden)
                // Klassische Paddle-Form erstellen
                Group paddleGroup = createPaddleShape();
                
                // Position setzen
                paddleGroup.setLayoutX(x);
                paddleGroup.setLayoutY(y);
                
                node = paddleGroup;
                cfg = PaddleConf.class;
                break;
            }

            default:
                // Unbekannter String → null zurück = Platzierung verweigern
                return null;
        }

        // ------------------------------
        // Drag-Funktionalität für platzierte Objekte hinzufügen  
        // (Mouse Event Konfiguration wird in setupObjectDragging gemacht)
        // ------------------------------
        setupObjectDragging(node);

        return new PlacedObject(node, cfg); // → PlacedObject-Objekt erzeugen
    }

    /**
     * Fügt Auswahl-Funktionalität zu einem platzierten Objekt hinzu
     */
    private void setupObjectSelection(Node node) {
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
            } else if (paddleMode && isGear(node)) {
                // Paddle-Modus: Paddle an Zahnrad befestigen
                createPaddle(node);
                exitPaddleMode();
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
            
            if (event.getButton() == MouseButton.PRIMARY) {
                // Links-Klick: Objekt auswählen
                clearSelection();
                selectedNode = node;
                highlightSelected(node);
                updateRotationButtons();
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                // Rechts-Klick: Objekt löschen
                handleObjectDeletion(node);
                event.consume();
            }
        });
    }

    /**
     * Behandelt das Löschen eines Objekts per Rechtsklick
     */
    private void handleObjectDeletion(Node originalNode) {
        // Wenn das Node eine Gruppe ist, nehmen wir das erste Kind (das Zahnrad)
        final Node node;
        if (originalNode instanceof Group) {
            Group group = (Group) originalNode;
            if (!group.getChildren().isEmpty()) {
                node = group.getChildren().get(0);
            } else {
                node = originalNode;
            }
        } else {
            node = originalNode;
        }
        
        // Passendes PlacedObject finden
                PlacedObject po = placedObjects.stream()
                        .filter(p -> p.getNode() == node)
                        .findFirst()
                        .orElse(null);
        
                if (po != null) {
            // Spezielle Behandlung für Zahnräder
            if (isGear(node)) {
                String gearId = gearIds.get(node);
                if (gearId != null) {
                    // Entferne alle Verbindungen zu diesem Zahnrad
                    removeAllGearConnections(gearId);
                    
                    // Entferne angehängte Paddles
                    Node paddle = gearPaddles.get(gearId);
                    if (paddle != null) {
                        // Wenn das Zahnrad in einer Gruppe ist, entferne die ganze Gruppe
                        Node parent = node.getParent();
                        if (parent instanceof Group) {
                            editorCanvas.getChildren().remove(parent);
                        } else {
                            editorCanvas.getChildren().remove(paddle);
                        }
                        gearPaddles.remove(gearId);
                        placedObjects.removeIf(p -> p.getNode() == paddle);
                    }
                    
                    // Entferne Zahnrad-Daten
                    gearIds.remove(node);
                    nodesByGearId.remove(gearId);
                }
            }
            
            // Vor dem Entfernen in die History legen
            push(new DeleteAction(po));
            
            // Objekt wirklich löschen
            Node parent = node.getParent();
            if (parent instanceof Group) {
                editorCanvas.getChildren().remove(parent);
            } else {
                    editorCanvas.getChildren().remove(node);
            }
                    placedObjects.remove(po);
        
                    // Selektion entfernen falls das gelöschte Objekt ausgewählt war
                    if (selectedNode == node) {
                        clearSelection();
                    }
        
            showHint("Objekt gelöscht");
        }
    }
    
    /**
     * Entfernt alle Verbindungen zu einem Zahnrad
     */
    private void removeAllGearConnections(String gearId) {
        Set<String> connections = gearConnections.get(gearId);
        if (connections != null) {
            // Entferne visuelle Verbindungen
            driveChainMappings.removeIf(mapping -> {
                String gearAId = gearIds.get(mapping.gearA);
                String gearBId = gearIds.get(mapping.gearB);
                if (gearId.equals(gearAId) || gearId.equals(gearBId)) {
                    editorCanvas.getChildren().remove(mapping.line);
                    driveChainConnections.remove(mapping.line);
                    return true;
                }
                return false;
            });
            
            // Entferne bidirektionale Verbindungen
            for (String connectedId : connections) {
                removeGearConnection(connectedId, gearId);
            }
            gearConnections.remove(gearId);
        }
    }
    
    /**
     * Entfernt die visuelle Hervorhebung aller Objekte
     */
    private void clearSelection() {
        if (selectedNode != null) {
            removeHighlight(selectedNode);
            selectedNode = null;
        }
        updateRotationButtons();
    }

    /** Hebt ein Objekt visuell hervor */
    private void highlightSelected(Node node) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStrokeWidth(3.0);
            circle.setStroke(Color.ORANGE);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStrokeWidth(3.0);
            rect.setStroke(Color.ORANGE);
        } else if (node instanceof Group) {
            // Für Groups (Bucket) alle Kinder hervorheben
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                if (child instanceof Line) {
                    Line line = (Line) child;
                    line.setStrokeWidth(5.0);
                    line.setStroke(Color.ORANGE);
                }
            }
        }
    }

    /** Entfernt die visuelle Hervorhebung eines Objekts */
    private void removeHighlight(Node node) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStrokeWidth(1.0);
            circle.setStroke(Color.BLACK);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStrokeWidth(1.0);
            rect.setStroke(getOriginalStroke(rect));
        } else if (node instanceof Group) {
            // Für Groups (Bucket) alle Kinder zurücksetzen
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                if (child instanceof Line) {
                    Line line = (Line) child;
                    line.setStrokeWidth(0.1 * 100.0); // Standard Bucket-Dicke (0.1 m = 10 px)
                    line.setStroke(Color.BLUE);
                }
            }
        }
    }

    /** Bestimmt die ursprüngliche Stroke-Farbe basierend auf der Fill-Farbe */
    private Color getOriginalStroke(Rectangle rect) {
        Color fill = (Color) rect.getFill();
        if (fill.equals(Color.SADDLEBROWN)) return Color.DARKGOLDENROD;
        if (fill.equals(Color.LIGHTGRAY)) return Color.BLACK;
        if (fill.equals(Color.BURLYWOOD)) return Color.SADDLEBROWN;
        return Color.BLACK; // Default
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
     * Fügt Drag-Funktionalität zu einem platzierten Objekt hinzu
     */
    private void setupObjectDragging(Node node) {
        System.out.println("=== SETUP OBJECT DRAGGING ===");
        System.out.println("Node type: " + node.getClass().getSimpleName());
        System.out.println("Node toString: " + node.toString());
        System.out.println("Node parent: " + (node.getParent() != null ? node.getParent().getClass().getSimpleName() : "null"));
        System.out.println("Node parent is Group: " + (node.getParent() instanceof Group));
        
        final double[] lastMousePos = new double[2];

        /* Startposition für History */
        final double[] startPos = new double[2];

        node.setPickOnBounds(true);
        node.setMouseTransparent(false);

        // Bestimme das zu bewegende Node (Gruppe oder einzelnes Node)
        final Node targetNode;
        if (node.getParent() instanceof Group) {
            // Wenn das Node Teil einer Gruppe ist, bewegen wir die Gruppe
            targetNode = node.getParent();
            System.out.println("TARGET: Using parent group - " + targetNode.getClass().getSimpleName());
        } else {
            // Sonst bewegen wir das Node selbst
            targetNode = node;
            System.out.println("TARGET: Using node itself - " + targetNode.getClass().getSimpleName());
        }
        System.out.println("Final targetNode: " + targetNode.toString());
        System.out.println("==============================");

        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }

        /* ---------- Press ---------- */
        node.setOnMousePressed(event -> {
            System.out.println("=== MOUSE PRESSED ===");
            System.out.println("Pressed on: " + node.getClass().getSimpleName());
            System.out.println("Event target: " + event.getTarget().getClass().getSimpleName());
            System.out.println("Primary button: " + event.isPrimaryButtonDown());
            System.out.println("TargetNode: " + targetNode.getClass().getSimpleName());
            System.out.println("TargetNode position: " + targetNode.getLayoutX() + ", " + targetNode.getLayoutY());
            
            if (!event.isPrimaryButtonDown()) return;
            
            /* Start-Layout merken für UNDO/REDO */
            startPos[0] = targetNode.getLayoutX();
            startPos[1] = targetNode.getLayoutY();
            
            // Scene-Koordinaten der Maus merken
            lastMousePos[0] = event.getSceneX();
            lastMousePos[1] = event.getSceneY();
            
            targetNode.setOpacity(0.7);
            targetNode.toFront();
            
            System.out.println("Set opacity and toFront on: " + targetNode.getClass().getSimpleName());
            System.out.println("====================");

            event.consume();
        });

        /* ---------- Drag ---------- */
        node.setOnMouseDragged(event -> {
            System.out.println("=== MOUSE DRAGGED ===");
            System.out.println("Dragged on: " + node.getClass().getSimpleName());
            System.out.println("TargetNode: " + targetNode.getClass().getSimpleName());
            System.out.println("Current targetNode position: " + targetNode.getLayoutX() + ", " + targetNode.getLayoutY());
            
            if (!event.isPrimaryButtonDown()) return;

            double deltaX = event.getSceneX() - lastMousePos[0];
            double deltaY = event.getSceneY() - lastMousePos[1];

            double newLayoutX = targetNode.getLayoutX() + deltaX;
            double newLayoutY = targetNode.getLayoutY() + deltaY;
            
            System.out.println("Delta: " + deltaX + ", " + deltaY);
            System.out.println("New position: " + newLayoutX + ", " + newLayoutY);

            // Grenzen berechnen (wie bisher)
            javafx.geometry.Bounds localBounds = targetNode.getBoundsInLocal();
            double minLX = -localBounds.getMinX();
            double minLY = -localBounds.getMinY();
            double maxRX = editorCanvas.getWidth() - localBounds.getMaxX();
            double maxDY = editorCanvas.getHeight() - localBounds.getMaxY();

            newLayoutX = clamp(newLayoutX, minLX, maxRX);
            newLayoutY = clamp(newLayoutY, minLY, maxDY);

            // Testweise neue Position setzen
            double oldX = targetNode.getLayoutX();
            double oldY = targetNode.getLayoutY();
            System.out.println("Setting targetNode position to: " + newLayoutX + ", " + newLayoutY);
            targetNode.setLayoutX(newLayoutX);
            targetNode.setLayoutY(newLayoutY);
            System.out.println("Position after setting: " + targetNode.getLayoutX() + ", " + targetNode.getLayoutY());

            // Überlappung prüfen
            if (overlapsExisting(targetNode, targetNode)) {
                System.out.println("OVERLAP DETECTED - reverting position");
                targetNode.setLayoutX(oldX);
                targetNode.setLayoutY(oldY);
                // Kein Warnhinweis beim Verschieben!
            } else {
                System.out.println("NO OVERLAP - position accepted");
                lastMousePos[0] = event.getSceneX();
                lastMousePos[1] = event.getSceneY();
                
                // Verbindungslinien aktualisieren wenn Zahnrad bewegt wird
                if (isGear(node) || (targetNode instanceof Group && containsGear((Group)targetNode))) {
                    System.out.println("Updating connections for gear");
                    updateAllConnections();
                }
            }

            System.out.println("====================");
            event.consume();
        });

        /* ---------- Release / Hover ---------- */
        node.setOnMouseReleased(event -> { 
            targetNode.setOpacity(1); 

            /* Ende-Layout vergleichen & History pushen für UNDO/REDO */
            double endX = targetNode.getLayoutX();
            double endY = targetNode.getLayoutY();
            if (endX != startPos[0] || endY != startPos[1]) {
                push(new MoveAction(targetNode, startPos[0], startPos[1], endX, endY));
            }

            event.consume(); 
        });
        
        node.setOnMouseEntered(event -> { 
            targetNode.setOpacity(0.8); 
            editorCanvas.setCursor(Cursor.HAND); 
        });
        
        node.setOnMouseExited(event -> { 
            targetNode.setOpacity(1); 
            editorCanvas.setCursor(Cursor.DEFAULT); 
        });


    }

    /**
     * Prüft ob eine Gruppe ein Zahnrad enthält
     */
    private boolean containsGear(Group group) {
        for (Node child : group.getChildren()) {
            if (isGear(child)) {
                return true;
            }
        }
        return false;
    }

    /* Utility */
    private static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : (v > hi) ? hi : v;
    }

    @FXML
    private void handleBack() {
        System.out.println("Zurück zum Hauptmenü");
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }

    @FXML
    private void handleReset() {
        System.out.println("Level zurücksetzen");
        clearSelection();  // Auswahl zurücksetzen
        editorCanvas.getChildren().clear();
        placedObjects.clear();

        // Zahnrad-Verwaltung zurücksetzen
        gearIds.clear();
        nodesByGearId.clear();
        gearConnections.clear();
        isLoadingLevel = false;  // Loading-Flag zurücksetzen

        // Undo/Redo-Verlauf zurücksetzen
        undoStack.clear();
        redoStack.clear();
        updateButtonStates();
    }

    @FXML
    private void handleSave() {
        showMetaDialog().ifPresent(meta -> {
            try {
                List<ObjectConf> list = placedObjects.stream()
                        .map(this::toConfigWithConnections)  // Neue Methode verwenden
                        .collect(Collectors.toList());
    
                // Metadaten + Objekte zusammenführen
                LevelData level = new LevelData(
                        meta.getName(),
                        meta.getDifficulty(),
                        meta.getObjective(),
                        list,
                        lastMeta.getLimits()      // Limits aus Cache übernehmen
                );

                // FileChooser für Export
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Level exportieren");
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Level-Dateien", "*.json")
                );
                fileChooser.setInitialFileName(level.getName().replaceAll("\\s+", "_").toLowerCase() + ".json");

                File file = fileChooser.showSaveDialog(editorCanvas.getScene().getWindow());
                if (file != null) {
                    // Füge .json-Erweiterung hinzu, falls nicht vorhanden
                    if (!file.getName().toLowerCase().endsWith(".json")) {
                        file = new File(file.getParentFile(), file.getName() + ".json");
                    }

                    LevelStorage.save(level, file.toPath());
                    lastMeta = level;                         // für das nächste Mal vorgesetzt
                    showDropWarning("Level gespeichert: " + file.getName(), 3);
                }
            } catch (IOException ex) {
                showDropWarning("Speichern fehlgeschlagen: " + ex.getMessage(), 5);
                ex.printStackTrace();
            }
        });
    }
    
    /**
     * Konvertiert ein PlacedObject zu ObjectConf und fügt Verbindungen für Zahnräder hinzu
     */
    private ObjectConf toConfigWithConnections(PlacedObject placedObject) {
        ObjectConf config = placedObject.toConfig();
        
        // Für Zahnräder: Verbindungen hinzufügen
        if (config instanceof GearConf) {
            GearConf gearConfig = (GearConf) config;
            String gearId = gearIds.get(placedObject.getNode());
            
            if (gearId != null) {
                Set<String> connections = gearConnections.get(gearId);
                if (connections != null) {
                    // Füge alle Verbindungen zur GearConf hinzu
                    for (String connectedId : connections) {
                        gearConfig.addConnection(connectedId);
                    }
                }
            }
        } else if (config instanceof PaddleConf) {
            // Für Paddles: korrekte angebundene Zahnrad-ID ermitteln
            String attachedGearId = findAttachedGearId(placedObject.getNode());
            if (attachedGearId != null) {
                // Erstelle eine neue PaddleConf mit der korrekten Zahnrad-ID
                float x = config.getX();
                float y = config.getY();
                float angle = config.getAngle();
                return new PaddleConf(x, y, angle, attachedGearId);
            }
        }
        
        return config;
    }
    
    /**
     * Findet die ID des Zahnrads, an dem ein Paddle befestigt ist
     */
    private String findAttachedGearId(Node paddleNode) {
        for (Map.Entry<String, Node> entry : gearPaddles.entrySet()) {
            if (entry.getValue() == paddleNode) {
                return entry.getKey();
            }
        }
        return null;
    }

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
                // Validiere die JSON-Datei
                if (!LevelValidator.isValidLevelFile(file)) {
                    showDropWarning("Ungültiges Level-Format", 5);
                    return;
                }

                // Lade das validierte Level
                LevelData level = LevelValidator.loadValidatedLevel(file);
                lastMeta = level;                            // spätere Saves vorausfüllen
        
                handleReset();
                
                // Erste Phase: Alle Objekte laden und Zahnrad-IDs zuweisen
                Map<String, Node> loadedGearNodes = new HashMap<>();
                for (ObjectConf conf : level.getObjects()) {
                    double x = conf.getX() * 100;
                    double y = conf.getY() * 100;
                    // Verwende skinId statt Klassenname für korrekte Objekttyp-Erkennung
                    String type = conf.getSkinId();
                    PlacedObject po = createPlacedObjectForLoading(type, x, y, conf);
                    if (po != null) {
                        // Rotationswinkel aus der Konfiguration anwenden (von Radiant in Grad umrechnen)
                        po.getNode().setRotate(Math.toDegrees(conf.getAngle()));
                        
                        editorCanvas.getChildren().add(po.getNode());
                        setupObjectSelection(po.getNode());  // Auswahl-Funktionalität hinzufügen
                        
                        // Für geladene Objekte muss Drag-Funktionalität separat hinzugefügt werden
                        // (da createPlacedObject sie nur für direkt erstellte Objekte einrichtet)
                        if (!type.equals("smallgear") && !type.equals("largegear")) {
                            setupObjectDragging(po.getNode());
                        }
                        
                        placedObjects.add(po);
                        
                        // Für Zahnräder: ID-Mapping für spätere Verbindung
                        if (conf instanceof GearConf && (type.equals("smallgear") || type.equals("largegear"))) {
                            GearConf gearConf = (GearConf) conf;
                            
                            // Erstelle eine neue ID für dieses Zahnrad beim Laden
                            String gearId = "gear" + nextGearId++;
                            gearIds.put(po.getNode(), gearId);
                            nodesByGearId.put(gearId, po.getNode());
                            loadedGearNodes.put(gearId, po.getNode());
                            
                            // Verbindungen in separaten Datenstrukturen speichern
                            Set<String> connections = new HashSet<>(gearConf.getConnectedGearIds());
                            if (!connections.isEmpty()) {
                                gearConnections.put(gearId, connections);
                            }
                        } else if (conf instanceof PaddleConf && type.equals("paddle")) {
                            // Für Paddles: Zahnrad-Bindung nach dem Laden aller Objekte wiederherstellen
                            // (wird in dritter Phase behandelt)
                        }
                    }
                }
                
                // Zweite Phase: Verbindungen zwischen Zahnrädern wiederherstellen
                restoreGearConnectionsFromSavedData(level.getObjects());
                
                // Dritte Phase: Paddle-Bindungen wiederherstellen
                restorePaddleBindings(level.getObjects());
                
                // Vierte Phase: Alle Verbindungen nach Paddle-Bindung aktualisieren
                updateAllConnections();
                
                showDropWarning("Level geladen – " + level.getName(), 3);
            } catch (IllegalArgumentException ex) {
                showDropWarning("Ungültiges Level-Format: " + ex.getMessage(), 5);
                ex.printStackTrace();
            } catch (IOException ex) {
                showDropWarning("Fehler beim Laden: " + ex.getMessage(), 5);
                ex.printStackTrace();
            }
        }
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

    /** Blendet für ein paar Sekunden eine Hinweisbox in der Mitte des Spielfelds ein. */
    private void showDropWarning(String message, int duration) {
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setMaxWidth(260); 
        msg.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );

        StackPane box = new StackPane(msg);
        box.setPrefWidth(300);
        box.setPrefHeight(100);
        box.setMaxSize(300, 100);

        box.setStyle(
            "-fx-background-color: red;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 3px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 2);"
        );
        box.setMouseTransparent(true);                  // blockiert nichts
        StackPane.setAlignment(box, Pos.CENTER);        // mittig platzieren

        canvasRoot.getChildren().add(box);              // ins StackPane (oberste Ebene)

        javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(duration));
        wait.setOnFinished(e -> canvasRoot.getChildren().remove(box));
        wait.play();
    }

    /** Zeigt den Dialog zum Festlegen der Objekt-Limits */
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
        for (String type : ITEM_TYPES) {
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
            showDropWarning("Objekt-Limits aktualisiert", 2);
        });
    }

    @FXML
    private void handlePlay() {
        System.out.println("Play button clicked");
        // TODO: Implementiere die Spielstart-Logik
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
        }
        public void redo() {
            editorCanvas.getChildren().add(index, node);
            placedObjects.add(po);
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
        }
        public void redo() {
            editorCanvas.getChildren().remove(node);
            placedObjects.remove(po);
        }
    }

    /** Entsteht, wenn ein Objekt bewegt wurde (alter != neuer Ort). */
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
    }

    /** Aktiviert/Deaktiviert die Rotations-Buttons je nach Objektauswahl */
    private void updateRotationButtons() {
        boolean hasSelection = selectedNode != null;
        rotateLeftButton.setDisable(!hasSelection);
        rotateRightButton.setDisable(!hasSelection);
    }
    
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



    /* ------------ Metadaten Dialog ------------ */
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

    /** Prüft, ob das Limit für einen Objekttyp erreicht wurde */
    private boolean checkLimitReached(String type) {
        // Spezielle Behandlung für einzigartige Objekte
        if (UNIQUE_ITEMS.contains(type)) {
            long existing = placedObjects.stream()
                    .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                    .filter(t -> t.equals(type))
                    .count();
            return existing >= 1; // Nur eines erlaubt
        }
        
        // Normale Limit-Prüfung für andere Objekte
        long already = placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
        int allowed = lastMeta.getLimits().getOrDefault(type, 999); // 999 = unbegrenzt falls nicht definiert
        return already >= allowed;
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
    
    /**
     * Fügt das Paddle-Item zur Sidebar hinzu
     */
    private void addPaddleItem() {
        VBox itemBox = new VBox(5);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
        
        // Icon (kann später durch echtes Bild ersetzt werden)
        Label iconLabel = new Label("🏓");
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        Label nameLabel = new Label("Paddle");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-alignment: center;");
        
        itemBox.getChildren().addAll(iconLabel, nameLabel);
        
        // Klick-Handler für Paddle-Modus
        itemBox.setOnMouseClicked(event -> {
            if (!paddleMode) {
                enterPaddleMode();
            } else {
                exitPaddleMode();
            }
        });
        
        // Hover-Effekte
        itemBox.setOnMouseEntered(e -> itemBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #999; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;"));
        itemBox.setOnMouseExited(e -> {
            if (!paddleMode) {
                itemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
            }
        });
        
        itemsContainer.getChildren().add(itemBox);
        paddleItemBox = itemBox; // Referenz speichern
    }
    
    // Antriebsstrang-Modus Variablen
    private boolean driveChainMode = false;
    private VBox driveChainItemBox;
    private List<Node> driveChainConnections = new ArrayList<>(); // Gespeicherte Verbindungen
    
    // Paddle-Modus Variablen
    private boolean paddleMode = false;
    private VBox paddleItemBox;
    private final Map<String, Node> gearPaddles = new HashMap<>();  // GearID -> Paddle Node
    
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
     * Aktiviert den Paddle-Modus
     */
    private void enterPaddleMode() {
        paddleMode = true;
        paddleItemBox.setStyle("-fx-background-color: #d0d0d0; -fx-border-color: #666; -fx-border-width: 2; -fx-padding: 10; -fx-cursor: hand;");
        
        // Anderen Modus deaktivieren
        if (driveChainMode) {
            exitDriveChainMode();
        }
        
        showHint("Paddle-Modus aktiv\nKlicken Sie auf ein Zahnrad, um ein Paddle zu befestigen");
    }
    
    /**
     * Deaktiviert den Paddle-Modus
     */
    private void exitPaddleMode() {
        paddleMode = false;
        paddleItemBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10; -fx-cursor: hand;");
        showHint("Paddle-Modus deaktiviert");
    }
    
    /**
     * Erstellt eine Antriebsstrang-Verbindung zwischen zwei Zahnrädern
     */
    private void createDriveChainConnection(Node gearA, Node gearB) {
        // IDs der Zahnräder ermitteln
        String gearAId = gearIds.get(gearA);
        String gearBId = gearIds.get(gearB);
        
        if (gearAId == null || gearBId == null) {
            showHint("Fehler: Zahnräder haben keine IDs");
            return;
        }
        
        // Verbindungen in separater Datenstruktur speichern
        addGearConnection(gearAId, gearBId);
        addGearConnection(gearBId, gearAId);
        
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
                // Verbindung aus den Konfigurationen entfernen
                removeGearConnection(gearAId, gearBId);
                removeGearConnection(gearBId, gearAId);
                
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
        // Berechne die absolute Position der Zahnräder (auch wenn sie in Gruppen sind)
        double[] posA = getAbsoluteGearPosition(gearA);
        double[] posB = getAbsoluteGearPosition(gearB);
        
        System.out.println("=== UPDATE CONNECTION LINE ===");
        System.out.println("GearA type: " + gearA.getClass().getSimpleName());
        System.out.println("GearA parent: " + (gearA.getParent() != null ? gearA.getParent().getClass().getSimpleName() : "null"));
        System.out.println("GearA position: " + posA[0] + ", " + posA[1]);
        System.out.println("GearB type: " + gearB.getClass().getSimpleName());
        System.out.println("GearB parent: " + (gearB.getParent() != null ? gearB.getParent().getClass().getSimpleName() : "null"));
        System.out.println("GearB position: " + posB[0] + ", " + posB[1]);
        
        line.setStartX(posA[0]);
        line.setStartY(posA[1]);
        line.setEndX(posB[0]);
        line.setEndY(posB[1]);
    }
    
    /**
     * Berechnet die absolute Position eines Zahnrads (auch wenn es in einer Gruppe ist)
     */
    private double[] getAbsoluteGearPosition(Node gear) {
        double x = gear.getLayoutX();
        double y = gear.getLayoutY();
        
        System.out.println("--- getAbsoluteGearPosition ---");
        System.out.println("Gear type: " + gear.getClass().getSimpleName());
        System.out.println("Gear local position: " + x + ", " + y);
        
        // Wenn das Zahnrad in einer Gruppe ist, addiere die Gruppenposition
        if (gear.getParent() instanceof Group) {
            Group parentGroup = (Group) gear.getParent();
            System.out.println("Group position: " + parentGroup.getLayoutX() + ", " + parentGroup.getLayoutY());
            x += parentGroup.getLayoutX();
            y += parentGroup.getLayoutY();
            System.out.println("Final absolute position: " + x + ", " + y);
        } else {
            System.out.println("Gear is standalone, using direct position: " + x + ", " + y);
        }
        
        // Für Kreise: layoutX/Y ist schon der Mittelpunkt
        // Aber prüfen wir trotzdem die Eigenschaften
        if (gear instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) gear;
            System.out.println("Circle centerX: " + circle.getCenterX() + ", centerY: " + circle.getCenterY());
            System.out.println("Circle radius: " + circle.getRadius());
            
            // Füge den Center-Offset hinzu (sollte normalerweise 0 sein für zentrierte Kreise)
            x += circle.getCenterX();
            y += circle.getCenterY();
            System.out.println("Position with center offset: " + x + ", " + y);
        }
        
        return new double[]{x, y};
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
     * Fügt eine Verbindung zwischen zwei Zahnrädern hinzu
     */
    private void addGearConnection(String gearId1, String gearId2) {
        gearConnections.computeIfAbsent(gearId1, k -> new HashSet<>()).add(gearId2);
    }
    
    /**
     * Entfernt eine Verbindung zwischen zwei Zahnrädern
     */
    private void removeGearConnection(String gearId1, String gearId2) {
        Set<String> connections = gearConnections.get(gearId1);
        if (connections != null) {
            connections.remove(gearId2);
            if (connections.isEmpty()) {
                gearConnections.remove(gearId1);
            }
        }
    }
    
    /**
     * Erstellt ein PlacedObject beim Laden mit Berücksichtigung von Zahnrad-IDs
     */
    private PlacedObject createPlacedObjectForLoading(String type, double x, double y, ObjectConf conf) {
        PlacedObject po = createPlacedObject(type, x, y);
        
        // Für Zahnräder: IDs aus der Konfiguration wiederherstellen
        if (po != null && conf instanceof GearConf && (type.equals("smallgear") || type.equals("largegear"))) {
            // Verwende eine deterministische ID basierend auf Position und Typ
            String gearId = generateGearIdFromConfig((GearConf) conf);
            gearIds.put(po.getNode(), gearId);
            nodesByGearId.put(gearId, po.getNode());
            
            // Aktualisiere nextGearId um Kollisionen zu vermeiden
            if (gearId.startsWith("gear")) {
                try {
                    int id = Integer.parseInt(gearId.substring(4));
                    nextGearId = Math.max(nextGearId, id + 1);
                } catch (NumberFormatException e) {
                    // Ignorieren, wenn ID-Format nicht erkannt wird
                }
            }
        }
        
        return po;
    }
    
    /**
     * Generiert eine deterministische ID für ein Zahnrad basierend auf seiner Konfiguration
     */
    private String generateGearIdFromConfig(GearConf conf) {
        // Verwende Position und Größe für deterministische ID
        String sizePrefix = conf.getSkinId().equals("largegear") ? "L" : "S";
        int hashCode = (sizePrefix + conf.getX() + "," + conf.getY()).hashCode();
        return "gear" + Math.abs(hashCode % 10000);
    }
    
    /**
     * Findet die ID eines Zahnrads oder erstellt eine neue
     */
    private String findOrCreateGearId(Node gearNode) {
        String existingId = gearIds.get(gearNode);
        if (existingId != null) {
            return existingId;
        }
        
        // Neue ID erstellen
        String newId = "gear" + nextGearId++;
        gearIds.put(gearNode, newId);
        nodesByGearId.put(newId, gearNode);
        return newId;
    }
    
    /**
     * Stellt Verbindungen zwischen Zahnrädern beim Laden wieder her
     */
    private void restoreGearConnections(List<ObjectConf> objects, Map<String, Node> loadedGearNodes) {
        // Erstelle ein Mapping: gespeicherte ID -> Node
        Map<String, Node> storedIdToNode = new HashMap<>();
        
        // Durchlaufe alle geladenen Zahnräder und erstelle das Mapping
        for (ObjectConf conf : objects) {
            if (conf instanceof GearConf) {
                GearConf gearConf = (GearConf) conf;
                Node gearNode = findGearNodeByConfig(gearConf);
                if (gearNode != null) {
                    String assignedId = gearIds.get(gearNode);
                    if (assignedId != null) {
                        storedIdToNode.put(assignedId, gearNode);
                    }
                }
            }
        }
        
        // Jetzt erstelle die visuellen Verbindungen basierend auf den gespeicherten Daten
        for (String gearId : gearConnections.keySet()) {
            Node sourceNode = storedIdToNode.get(gearId);
            if (sourceNode != null) {
                Set<String> connections = gearConnections.get(gearId);
                for (String connectedId : connections) {
                    Node targetNode = storedIdToNode.get(connectedId);
                    if (targetNode != null) {
                        // Prüfen ob Verbindung noch nicht existiert (um Duplikate zu vermeiden)
                        boolean connectionExists = driveChainMappings.stream()
                            .anyMatch(mapping -> 
                                (mapping.gearA == sourceNode && mapping.gearB == targetNode) ||
                                (mapping.gearA == targetNode && mapping.gearB == sourceNode));
                        
                        if (!connectionExists) {
                            // Visuelle Verbindung erstellen
                            createVisualConnection(sourceNode, targetNode);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Findet ein Zahnrad-Node basierend auf seiner Konfiguration
     */
    private Node findGearNodeByConfig(GearConf targetConf) {
        System.out.println("=== FIND GEAR NODE BY CONFIG ===");
        System.out.println("Looking for gear at: " + targetConf.getX() + ", " + targetConf.getY() + " (size: " + targetConf.getGearSize() + ")");
        System.out.println("placedObjects count: " + placedObjects.size());
        
        for (PlacedObject po : placedObjects) {
            if (po.getConfigClass() == GearConf.class) {
                GearConf poConf = (GearConf) po.toConfig();
                System.out.println("Checking gear: " + poConf.getX() + ", " + poConf.getY() + " (size: " + poConf.getGearSize() + ")");
                
                // Vergleiche Position und Größe
                if (Math.abs(poConf.getX() - targetConf.getX()) < 0.01f && 
                    Math.abs(poConf.getY() - targetConf.getY()) < 0.01f &&
                    poConf.getGearSize() == targetConf.getGearSize()) {
                    System.out.println("MATCH FOUND!");
                    return po.getNode();
                }
            }
        }
        
        System.out.println("NO MATCH FOUND - Trying direct canvas search...");
        
        // Fallback: Suche direkt auf dem Canvas
        for (Node node : editorCanvas.getChildren()) {
            if (node.getId() != null && node.getId().startsWith("gear")) {
                // Konvertiere Canvas-Position zu JSON-Koordinaten für Vergleich
                double nodeX = node.getLayoutX() / 100.0;
                double nodeY = node.getLayoutY() / 100.0;
                
                System.out.println("Canvas gear at: " + nodeX + ", " + nodeY);
                
                if (Math.abs(nodeX - targetConf.getX()) < 0.01f && 
                    Math.abs(nodeY - targetConf.getY()) < 0.01f) {
                    System.out.println("CANVAS MATCH FOUND!");
                    return node;
                }
            }
        }
        
        System.out.println("NO GEAR FOUND AT ALL!");
        return null;
    }
    
    /**
     * Findet ein Zahnrad-Node basierend auf einer gespeicherten ID
     */
    private Node findGearNodeByStoredId(String storedId, List<ObjectConf> allObjects) {
        // Finde die Konfiguration mit dieser ID in den Verbindungen
        for (ObjectConf conf : allObjects) {
            if (conf instanceof GearConf) {
                GearConf gearConf = (GearConf) conf;
                String computedId = generateGearIdFromConfig(gearConf);
                if (computedId.equals(storedId)) {
                    return findGearNodeByConfig(gearConf);
                }
            }
        }
        return null;
    }
    
    /**
     * Erstellt nur die visuelle Verbindung ohne die Datenstrukturen zu ändern
     */
    private void createVisualConnection(Node gearA, Node gearB) {
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
        String gearAId = gearIds.get(gearA);
        String gearBId = gearIds.get(gearB);
        connection.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                // Verbindung aus den Konfigurationen entfernen
                if (gearAId != null && gearBId != null) {
                    removeGearConnection(gearAId, gearBId);
                    removeGearConnection(gearBId, gearAId);
                }
                
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
    }

    /**
     * Stellt die Verbindungen zwischen Zahnrädern nach dem Laden wieder her
     * Verwendet die gespeicherten Verbindungsdaten aus der JSON
     */
    private void restoreGearConnectionsFromSavedData(List<ObjectConf> objects) {
        // Erstelle ein Mapping: Index im Array -> Node (für Verbindungen)
        Map<Integer, Node> indexToNode = new HashMap<>();
        Map<Integer, String> indexToGearId = new HashMap<>();
        
        // Durchlaufe alle Objekte und erstelle das Mapping
        for (int i = 0; i < objects.size(); i++) {
            ObjectConf conf = objects.get(i);
            if (conf instanceof GearConf) {
                Node gearNode = findGearNodeByConfig((GearConf) conf);
                if (gearNode != null) {
                    indexToNode.put(i, gearNode);
                    String gearId = gearIds.get(gearNode);
                    if (gearId != null) {
                        indexToGearId.put(i, gearId);
                    }
                }
            }
        }
        
        // Erstelle die visuellen Verbindungen basierend auf den gespeicherten Daten
        for (int i = 0; i < objects.size(); i++) {
            ObjectConf conf = objects.get(i);
            if (conf instanceof GearConf) {
                GearConf gearConf = (GearConf) conf;
                Node sourceNode = indexToNode.get(i);
                String sourceGearId = indexToGearId.get(i);
                
                if (sourceNode != null && sourceGearId != null) {
                    // Für jede gespeicherte Verbindung
                    for (String connectedId : gearConf.getConnectedGearIds()) {
                        // Finde das Ziel-Zahnrad basierend auf der gespeicherten ID
                        Node targetNode = findNodeByStoredId(connectedId, objects, indexToNode);
                        
                        if (targetNode != null) {
                            // Prüfen ob Verbindung noch nicht existiert (um Duplikate zu vermeiden)
                            boolean connectionExists = driveChainMappings.stream()
                                .anyMatch(mapping -> 
                                    (mapping.gearA == sourceNode && mapping.gearB == targetNode) ||
                                    (mapping.gearA == targetNode && mapping.gearB == sourceNode));
                            
                            if (!connectionExists) {
                                // Visuelle Verbindung erstellen
                                createVisualConnection(sourceNode, targetNode);
                                
                                // Auch in den neuen Verbindungsdaten speichern
                                String targetGearId = gearIds.get(targetNode);
                                if (targetGearId != null) {
                                    addGearConnection(sourceGearId, targetGearId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Findet ein Node basierend auf einer gespeicherten ID
     */
    private Node findNodeByStoredId(String storedId, List<ObjectConf> objects, Map<Integer, Node> indexToNode) {
        // Strategie: Da die IDs beim Speichern sequenziell waren (gear1, gear2, etc.)
        // und beim Laden in derselben Reihenfolge neue IDs vergeben werden,
        // können wir die numerische ID extrahieren und den entsprechenden Index finden
        
        try {
            // Extrahiere die Nummer aus "gearX"
            String numberStr = storedId.replace("gear", "");
            int storedIndex = Integer.parseInt(numberStr) - 1; // gear1 -> Index 0
            
            // Finde das entsprechende Zahnrad im Array
            int gearCount = 0;
            for (int i = 0; i < objects.size(); i++) {
                if (objects.get(i) instanceof GearConf) {
                    if (gearCount == storedIndex) {
                        return indexToNode.get(i);
                    }
                    gearCount++;
                }
            }
        } catch (NumberFormatException e) {
            // Falls das Format nicht passt, ignorieren
        }
        
        return null;
    }
    
    /**
     * Erstellt ein Paddle an einem Zahnrad
     */
    private void createPaddle(Node gearNode) {
        System.out.println("=== CREATE PADDLE ===");
        System.out.println("GearNode type: " + gearNode.getClass().getSimpleName());
        System.out.println("GearNode position: " + gearNode.getLayoutX() + ", " + gearNode.getLayoutY());
        
        String gearId = gearIds.get(gearNode);
        if (gearId == null) {
            gearId = findOrCreateGearId(gearNode);
        }
        final String finalGearId = gearId;
        System.out.println("GearId: " + finalGearId);
        
        // Prüfen ob bereits ein Paddle an diesem Zahnrad befestigt ist
        if (gearPaddles.containsKey(finalGearId)) {
            System.out.println("Gear already has paddle - returning");
            showHint("An diesem Zahnrad ist bereits ein Paddle befestigt");
            return;
        }
        
        // Paddle visuell erstellen (klassische Paddle-Form)
        double gearCenterX = gearNode.getLayoutX();
        double gearCenterY = gearNode.getLayoutY();
        System.out.println("Gear center: " + gearCenterX + ", " + gearCenterY);
        
        // Paddle als Group mit Polygon erstellen (klassische Paddle-Form)
        System.out.println("Creating paddle shape...");
        Group paddleGroup = createPaddleShape();
        System.out.println("Created paddle group: " + paddleGroup.toString());
        
        // Position: Paddle ragt vom Zahnrad-Zentrum nach oben
        paddleGroup.setLayoutX(gearCenterX);
        paddleGroup.setLayoutY(gearCenterY);
        
        // Paddle zur Canvas hinzufügen
        editorCanvas.getChildren().add(paddleGroup);
        
        // Paddle in Datenstrukturen speichern
        gearPaddles.put(finalGearId, paddleGroup);
        
        // Paddle-Rotation und -Bewegung mit Zahnrad synchronisieren
        bindPaddleToGear(paddleGroup, gearNode);
        
        // Das Paddle für Rechtsklick aktivieren
        paddleGroup.setMouseTransparent(false);
        paddleGroup.setPickOnBounds(true);
        
        // Rechtsklick-Handler für Paddle-Löschung
        paddleGroup.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                removePaddle(paddleGroup, finalGearId);
                event.consume();
                return;
            }
            // Für Primärklicks: Event nicht konsumieren, damit es zum Zahnrad geht
        });
        
        // PlacedObject für das Paddle erstellen und zur Liste hinzufügen
        PlacedObject paddleObject = new PlacedObject(paddleGroup, PaddleConf.class, "paddle");
        placedObjects.add(paddleObject);
        
        showHint("Paddle an Zahnrad befestigt (Rechtsklick zum Löschen)");
    }
    
    /**
     * Entfernt ein Paddle
     */
    private void removePaddle(Node paddleNode, String gearId) {
        // Finde die Gruppe, die das Paddle enthält
        Group gearGroup = (Group) paddleNode.getParent();
        if (gearGroup != null) {
            // Hole das Zahnrad aus der Gruppe
            Node gearNode = gearGroup.getChildren().get(0); // Das Zahnrad ist immer das erste Kind
            
            // Hole die Position der Gruppe
            double groupX = gearGroup.getLayoutX();
            double groupY = gearGroup.getLayoutY();
            double groupRotation = gearGroup.getRotate();
            
            // Entferne die Gruppe aus dem Canvas
            editorCanvas.getChildren().remove(gearGroup);
            
            // Setze das Zahnrad zurück auf die Position der Gruppe
            gearNode.setLayoutX(groupX);
            gearNode.setLayoutY(groupY);
            gearNode.setRotate(groupRotation);
            
            // Füge das Zahnrad wieder direkt zum Canvas hinzu
            editorCanvas.getChildren().add(gearNode);
            
            // Übertrage die Drag-Funktionalität zurück auf das Zahnrad
            setupObjectDragging(gearNode);
        }
        
        // Aus Datenstrukturen entfernen
        gearPaddles.remove(gearId);
        
        // Aus PlacedObjects entfernen
        placedObjects.removeIf(po -> po.getNode() == paddleNode);
        
        showHint("Paddle entfernt");
    }
    
    /**
     * Bindet die Paddle-Position und -Rotation an ein Zahnrad
     */
    private void bindPaddleToGear(Node paddleNode, Node gearNode) {
        bindPaddleToGear(paddleNode, gearNode, gearNode.getRotate());
    }
    
    /**
     * Bindet die Paddle-Position und -Rotation an ein Zahnrad mit initialem Winkel
     */
    private void bindPaddleToGear(Node paddleNode, Node gearNode, double initialAngle) {
        System.out.println("=== BIND PADDLE TO GEAR ===");
        System.out.println("GearNode position before: " + gearNode.getLayoutX() + ", " + gearNode.getLayoutY());
        System.out.println("GearNode parent before: " + (gearNode.getParent() != null ? gearNode.getParent().getClass().getSimpleName() : "null"));
        
        // Erstelle eine Gruppe für Zahnrad und Paddle
        Group gearGroup = new Group();

        
        // Hole die aktuellen Koordinaten des Zahnrads
        double gearX = gearNode.getLayoutX();
        double gearY = gearNode.getLayoutY();
        double gearRotation = gearNode.getRotate();
        
        // WICHTIG: Wenn das Zahnrad schon in einer Gruppe ist, verwende die Gruppenposition
        if (gearNode.getParent() instanceof Group) {
            Group currentGroup = (Group) gearNode.getParent();
            gearX = currentGroup.getLayoutX();
            gearY = currentGroup.getLayoutY();
            gearRotation = currentGroup.getRotate();
        }
        
        // Entferne das Zahnrad aus dem Canvas (oder aus seiner aktuellen Gruppe)
        if (gearNode.getParent() instanceof Group) {
            Group currentGroup = (Group) gearNode.getParent();
            editorCanvas.getChildren().remove(currentGroup);
        } else {
            editorCanvas.getChildren().remove(gearNode);
        }
        
        // Setze das Zahnrad auf Position (0,0) in der neuen Gruppe
        gearNode.setLayoutX(0);
        gearNode.setLayoutY(0);
        
        // Füge das Zahnrad zur Gruppe hinzu
        gearGroup.getChildren().add(gearNode);
        
        // Setze das Paddle auf Position (0,0) in der Gruppe
        paddleNode.setLayoutX(0);
        paddleNode.setLayoutY(0);
        
        // Rotation für das Paddle
        Rotate paddleRotation = new Rotate();
        paddleRotation.setPivotX(0);
        paddleRotation.setPivotY(0);
        paddleRotation.setAngle(initialAngle);
        paddleNode.getTransforms().add(paddleRotation);
        
        // Füge das Paddle zur Gruppe hinzu
        gearGroup.getChildren().add(paddleNode);
        
        // Positioniere die Gruppe an der ursprünglichen Zahnrad-Position
        gearGroup.setLayoutX(gearX);
        gearGroup.setLayoutY(gearY);
        gearGroup.setRotate(gearRotation);
        
        // Füge die Gruppe zum Canvas hinzu
        editorCanvas.getChildren().add(gearGroup);
        
        // Aktualisiere die Referenz in der gearPaddles Map
        String gearId = gearIds.get(gearNode);
        if (gearId != null) {
            gearPaddles.put(gearId, paddleNode);
        }
        
        // Paddle rotiert mit dem Zahnrad
        gearNode.rotateProperty().addListener((obs, oldRotate, newRotate) -> {
            paddleRotation.setAngle(newRotate.doubleValue());
        });
        
        // Das Paddle für Rechtsklick aktivieren
        paddleNode.setMouseTransparent(false);
        paddleNode.setPickOnBounds(true);
        
        // Paddle Click Handler
        paddleNode.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                String currentGearId = gearIds.get(gearNode);
                if (currentGearId != null) {
                    removePaddle(paddleNode, currentGearId);
                    event.consume();
                    return;
                }
            }
            // Für Primärklicks: Event nicht konsumieren, damit es zum Zahnrad geht
        });
        
        // Übertrage die Drag-Funktionalität auf die Gruppe UND auf das Zahnrad direkt
        setupObjectDragging(gearGroup);
        setupObjectDragging(gearNode);  // Das Zahnrad selbst soll auch draggable sein
        setupObjectDragging(paddleNode);
    }
    
    /**
     * Erstellt eine klassische Paddle-Form basierend auf den angegebenen Maßen
     */
    private Group createPaddleShape() {
        Group paddleGroup = new Group();
        
                 // In der Simulation entspricht 1 Meter = 100 Pixel
         // Wir wollen ein 1.50 Meter langes Paddle
         double desiredLength = 150.0; // 1.50 Meter = 150 Pixel
         
         // WICHTIG: Zahnrad-Radius (Zahnräder sind ca. 50px Durchmesser)
         double gearRadius = 25.0;
         
         // Berechne den Skalierungsfaktor basierend auf der gewünschten Länge
         double scale = desiredLength / 1290.0; // Original war 1290 Einheiten lang
         
         // Maße basierend auf den angegebenen Verhältnissen (skaliert auf 1.50m)
         double totalLength = desiredLength;        // 1.50 Meter Gesamtlänge
         double bladeWidth = 145.0 * scale;         // Breite der Paddle-Fläche
         double handleWidth = 32.0 * scale;         // Breite des Stiels
         double curveHeight = 25.0 * scale;         // Krümmung unten
         
         // Verengungsmaße von der Außenseite des Paddles gemessen
         double taperStartFromTop = 280.0 * scale;  // Start der Verengung, von oben gemessen
         double taperEndFromTop = 415.0 * scale;    // Ende der Verengung, von oben gemessen
         
         // ANPASSUNG: Paddle beginnt am Zahnrad-Rand, nicht in der Mitte
         double paddleStartOffset = gearRadius;
         double effectiveLength = totalLength - paddleStartOffset;
         
         // Umrechnung für die Polygon-Konstruktion (von unten gemessen, mit Offset)
         double taperStart = effectiveLength - taperEndFromTop;   // Start der Verengung von unten
         double taperEnd = effectiveLength - taperStartFromTop;   // Ende der Verengung von unten
        
        // Erstelle die Paddle-Form als Polygon
        Polygon paddleShape = new Polygon();
        
        // Definiere die Punkte der Paddle-Form (vom Stiel nach oben)
        // Koordinatensystem: (0,0) ist das Zentrum des Zahnrads, Paddle beginnt bei paddleStartOffset
        
        // Stiel (unten) - beginnt am Zahnrad-Rand
        paddleShape.getPoints().addAll(new Double[]{
            -handleWidth/2, -paddleStartOffset,             // Stiel links unten (am Zahnrad-Rand)
            handleWidth/2, -paddleStartOffset,              // Stiel rechts unten
            handleWidth/2, -(paddleStartOffset + taperStart), // Stiel rechts bis Verengungsbeginn
        });
        
        // Verengung (rechte Seite)
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0; // Parameter von 0 bis 1
            double y = -(paddleStartOffset + taperStart) - t * (taperEnd - taperStart);
            double width = handleWidth/2 + t * (bladeWidth/2 - handleWidth/2);
            paddleShape.getPoints().addAll(new Double[]{width, y});
        }
        
        // Paddle-Fläche (rechte Seite zur Spitze)
        double bladeTop = -(paddleStartOffset + taperEnd + (effectiveLength - taperEnd - curveHeight));
        paddleShape.getPoints().addAll(new Double[]{
            bladeWidth/2, bladeTop,                         // Rechte Seite oben
        });
        
        // Obere Rundung
        for (int i = 0; i <= 8; i++) {
            double angle = i * Math.PI / 8; // Halbkreis
            double x = (bladeWidth/2) * Math.cos(angle);
            double y = bladeTop - curveHeight * Math.sin(angle);
            paddleShape.getPoints().addAll(new Double[]{x, y});
        }
        
        // Linke Seite (spiegelverkehrt)
        paddleShape.getPoints().addAll(new Double[]{
            -bladeWidth/2, bladeTop,                        // Linke Seite oben
        });
        
        // Verengung (linke Seite)
        for (int i = 10; i >= 0; i--) {
            double t = i / 10.0;
            double y = -(paddleStartOffset + taperStart) - t * (taperEnd - taperStart);
            double width = -(handleWidth/2 + t * (bladeWidth/2 - handleWidth/2));
            paddleShape.getPoints().addAll(new Double[]{width, y});
        }
        
        // Zurück zum Stiel
        paddleShape.getPoints().addAll(new Double[]{
            -handleWidth/2, -(paddleStartOffset + taperStart), // Verengungsende links
            -handleWidth/2, -paddleStartOffset                 // Stiel links unten (Schließung)
        });
        
        // Styling
        paddleShape.setFill(Color.SADDLEBROWN);
        paddleShape.setStroke(Color.DARKGOLDENROD);
        paddleShape.setStrokeWidth(1.5);
        
        // Füge die Form zur Gruppe hinzu
        paddleGroup.getChildren().add(paddleShape);
        
        return paddleGroup;
    }
    
    /**
     * Stellt die Paddle-Bindungen nach dem Laden wieder her
     */
    private void restorePaddleBindings(List<ObjectConf> objects) {
        System.out.println("=== RESTORE PADDLE BINDINGS ===");
        for (ObjectConf conf : objects) {
            if (conf instanceof PaddleConf) {
                PaddleConf paddleConf = (PaddleConf) conf;
                String attachedGearId = paddleConf.getAttachedGearId();
                System.out.println("Restoring paddle for gear ID: " + attachedGearId);
                System.out.println("Paddle config position: " + paddleConf.getX() + ", " + paddleConf.getY());
                
                // Finde das Paddle-Node
                Node paddleNode = findPaddleNodeByConfig(paddleConf);
                if (paddleNode == null) {
                    System.out.println("Paddle node not found");
                    continue;
                }
                System.out.println("Found paddle node: " + paddleNode.getClass().getSimpleName());
                
                // Finde das Zahnrad-Node basierend auf der gespeicherten ID
                Node gearNode = findGearNodeByAttachedId(attachedGearId, objects);
                if (gearNode == null) {
                    System.out.println("Gear node not found for ID: " + attachedGearId);
                    continue;
                }
                System.out.println("Found gear node: " + gearNode.getClass().getSimpleName() + " at position: " + gearNode.getLayoutX() + ", " + gearNode.getLayoutY());
                
                // Suche die entsprechende GearConf um die ursprüngliche Position zu sehen
                for (ObjectConf obj : objects) {
                    if (obj instanceof GearConf && attachedGearId.equals("gear1")) { // TODO: Bessere ID-Zuordnung
                        GearConf gearConf = (GearConf) obj;
                        System.out.println("Gear config position: " + gearConf.getX() + ", " + gearConf.getY());
                        break;
                    }
                }
                
                // Bestimme die neue Zahnrad-ID
                System.out.println("gearIds map contents:");
                for (Map.Entry<Node, String> entry : gearIds.entrySet()) {
                    System.out.println("  " + entry.getKey().hashCode() + " -> " + entry.getValue());
                }
                System.out.println("Looking for node: " + gearNode.hashCode());
                
                String newGearId = gearIds.get(gearNode);
                if (newGearId == null) {
                    System.out.println("No new gear ID found for this node - generating new one");
                    // Fallback: Generiere eine neue ID basierend auf der existierenden Struktur
                    newGearId = generateUniqueGearId();
                    gearIds.put(gearNode, newGearId);
                    nodesByGearId.put(newGearId, gearNode);
                    System.out.println("Generated new gear ID: " + newGearId);
                } else {
                    System.out.println("Found existing gear ID: " + newGearId);
                }
                
                final String finalGearId = newGearId;
                
                // Paddle-Bindung in Datenstrukturen wiederherstellen
                gearPaddles.put(finalGearId, paddleNode);
                
                // Bewegungs- und Rotations-Listener wiederherstellen
                // Verwende die Rotation des Zahnrads als initialen Winkel
                System.out.println("Calling bindPaddleToGear with gear at: " + gearNode.getLayoutX() + ", " + gearNode.getLayoutY());
                bindPaddleToGear(paddleNode, gearNode, gearNode.getRotate());
                
                // Das Paddle für Rechtsklick aktivieren
                paddleNode.setMouseTransparent(false);
                paddleNode.setPickOnBounds(true);
                
                // Rechtsklick-Handler für Paddle-Löschung
                paddleNode.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        removePaddle(paddleNode, finalGearId);
                        event.consume();
                        return;
                    }
                    // Für Primärklicks: Event nicht konsumieren, damit es zum Zahnrad geht
                });
            }
        }
    }
    
    /**
     * Findet ein Paddle-Node basierend auf seiner Konfiguration
     */
    private Node findPaddleNodeByConfig(PaddleConf targetConf) {
        for (PlacedObject po : placedObjects) {
            if (po.getConfigClass() == PaddleConf.class) {
                PaddleConf poConf = (PaddleConf) po.toConfig();
                // Vergleiche Position
                if (Math.abs(poConf.getX() - targetConf.getX()) < 0.01f && 
                    Math.abs(poConf.getY() - targetConf.getY()) < 0.01f) {
                    return po.getNode();
                }
            }
        }
        return null;
    }
    
    /**
     * Findet ein Zahnrad-Node basierend auf einer angebundenen Paddle-ID
     */
    private Node findGearNodeByAttachedId(String attachedGearId, List<ObjectConf> objects) {
        // Strategie: Die attachedGearId folgt dem gleichen Muster wie bei DriveChains
        // "gear1", "gear2", etc. -> entspricht dem Index der Zahnräder in der Liste
        
        try {
            String numberStr = attachedGearId.replace("gear", "");
            int targetIndex = Integer.parseInt(numberStr) - 1; // gear1 -> Index 0
            
            int gearCount = 0;
            for (ObjectConf conf : objects) {
                if (conf instanceof GearConf) {
                    if (gearCount == targetIndex) {
                        return findGearNodeByConfig((GearConf) conf);
                    }
                    gearCount++;
                }
            }
        } catch (NumberFormatException e) {
            // Falls das Format nicht passt, ignorieren
        }
        
        return null;
    }
    
    /**
     * Generiert eine eindeutige Zahnrad-ID
     */
    private String generateUniqueGearId() {
        int id = (int) (Math.random() * 10000);
        String candidateId = "gear" + id;
        
        // Prüfe ob die ID bereits existiert
        while (nodesByGearId.containsKey(candidateId)) {
            id = (int) (Math.random() * 10000);
            candidateId = "gear" + id;
        }
        
        return candidateId;
    }
    
    /**
     * Prüft, ob an der angegebenen Position ein Objekt platziert werden kann
     */
    private boolean isValidDropLocation(double x, double y) {
        // Prüfe Kollision mit allen existierenden Objekten
        for (Node node : editorCanvas.getChildren()) {
            if (node instanceof Group || node instanceof Shape) {
                Bounds bounds = node.getBoundsInParent();
                // Füge einen kleinen Puffer hinzu (5 Pixel)
                if (bounds.contains(x, y) || 
                    bounds.contains(x-5, y-5) || 
                    bounds.contains(x+5, y+5)) {
                    return false;
                }
            }
        }
        return true;
    }
}   