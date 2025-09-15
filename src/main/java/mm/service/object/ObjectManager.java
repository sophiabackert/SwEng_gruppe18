package mm.service.object;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

import mm.domain.editor.PlacedObject;
import mm.domain.config.ObjectConf;
import mm.domain.config.TennisballConf;
import mm.domain.config.BowlingballConf;
import mm.domain.config.BilliardballConf;
import mm.domain.config.BalloonConf;
import mm.domain.config.LogConf;
import mm.domain.config.PlankConf;
import mm.domain.config.DominoConf;
import mm.domain.config.CrateboxConf;
import mm.domain.config.BucketConf;
import mm.domain.config.GameBallConf;
import mm.domain.config.GoalZoneConf;
import mm.domain.config.RestrictionZoneConf;
import mm.service.collision.CollisionManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Verwaltet das Platzieren, Entfernen und Verwalten von Objekten im Editor.
 * <p>
 * Unterstützt das Erstellen von Objekten, Limit-Prüfungen, Drag & Drop, Kollisionserkennung und das Handling von PrePlaced-Objekten.
 * </p>
 */
public class ObjectManager {
    
    private final List<PlacedObject> placedObjects = new ArrayList<>();
    private final List<PlacedObject> prePlacedObjects = new ArrayList<>();
    private final Map<String, Integer> currentLimits = new HashMap<>();
    private final CollisionManager collisionManager = new CollisionManager();
    
    private static final List<String> UNIQUE_ITEMS = List.of("gameball", "goalzone");
    
    private static final double SCALE = 100.0;
    
    /**
     * Erstellt einen leeren ObjectManager.
     */
    public ObjectManager() {}
    
    /**
     * Erstellt einen ObjectManager mit Limits.
     * @param limits Map mit Objekt-Limits
     */
    public ObjectManager(Map<String, Integer> limits) {
        this.currentLimits.putAll(limits);
    }
    
    /**
     * Gibt die Liste der platzierten Objekte zurück.
     * @return Liste der platzierten Objekte
     */
    public List<PlacedObject> getPlacedObjects() {
        return placedObjects;
    }
    
    /**
     * Gibt die Liste der vorplatzierten Objekte zurück.
     * @return Liste der vorplatzierten Objekte
     */
    public List<PlacedObject> getPrePlacedObjects() {
        return prePlacedObjects;
    }
    
    /**
     * Gibt die aktuellen Objekt-Limits zurück.
     * @return Map der Limits
     */
    public Map<String, Integer> getCurrentLimits() {
        return currentLimits;
    }
    
    /**
     * Setzt die Objekt-Limits neu.
     * @param limits Neue Limits
     */
    public void setLimits(Map<String, Integer> limits) {
        currentLimits.clear();
        currentLimits.putAll(limits);
    }
    
    /**
     * Entfernt alle platzierten und vorplatzierten Objekte.
     */
    public void clear() {
        placedObjects.clear();
        prePlacedObjects.clear();
    }
    
    /**
     * Prüft, ob das Limit für einen Objekttyp erreicht ist.
     * @param type Objekttyp
     * @return true, wenn das Limit erreicht ist
     */
    public boolean checkLimitReached(String type) {
        long already = placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
        int allowed = currentLimits.getOrDefault(type, 999);
        return already >= allowed;
    }
    
    /**
     * Gibt die aktuelle Anzahl eines Objekttyps zurück.
     * @param type Objekttyp
     * @return Anzahl
     */
    public int getCurrentCount(String type) {
        return (int) placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
    }
    
    /**
     * Prüft, ob das Limit für ein einzigartiges Objekt (z.B. gameball, goalzone) erreicht ist.
     * @param type Objekttyp
     * @return true, wenn das Limit erreicht ist
     */
    public boolean isUniqueItemLimitReached(String type) {
        if (!UNIQUE_ITEMS.contains(type)) {
            return false;
        }
        
        long existing = placedObjects.stream()
                .map(po -> po.getConfigClass().getSimpleName().toLowerCase().replace("conf", ""))
                .filter(t -> t.equals(type))
                .count();
        return existing >= 1;
    }
    
    /**
     * Erstellt ein neues platziertes Objekt.
     * @param type Objekttyp
     * @param x X-Position
     * @param y Y-Position
     * @return PlacedObject oder null
     */
    public PlacedObject createPlacedObject(String type, double x, double y) {
        return createPlacedObject(type, x, y, false);
    }
    
    /**
     * Erstellt ein neues platziertes oder vorplatziertes Objekt.
     * @param type Objekttyp
     * @param x X-Position
     * @param y Y-Position
     * @param isPrePlaced true, wenn vorplatziert
     * @return PlacedObject oder null
     */
    public PlacedObject createPlacedObject(String type, double x, double y, boolean isPrePlaced) {
        Node node;
        Class<? extends ObjectConf> cfg;
        
        switch (type) {
            case "tennisball": {
                TennisballConf tempConf = new TennisballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    circle.setFill(pattern);
                } else {
                    circle.setFill(isPrePlaced ? Color.YELLOW.darker() : Color.YELLOW);
                }
                circle.setStroke(Color.BLACK);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = TennisballConf.class;
                break;
            }
            
            case "bowlingball": {
                BowlingballConf tempConf = new BowlingballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    circle.setFill(pattern);
                } else {
                    circle.setFill(isPrePlaced ? Color.BROWN.darker() : Color.BROWN);
                }
                circle.setStroke(Color.BLACK);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = BowlingballConf.class;
                break;
            }
            
            case "billiardball": {
                BilliardballConf tempConf = new BilliardballConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    circle.setFill(pattern);
                } else {
                    circle.setFill(isPrePlaced ? Color.WHITE.darker() : Color.WHITE);
                }
                circle.setStroke(Color.BLACK);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = BilliardballConf.class;
                break;
            }
            
            case "balloon": {
                BalloonConf tempConf = new BalloonConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                
                // Erstelle eine Group für Ballon + Schnur
                Group balloonGroup = new Group();
                
                // Hauptkreis für die physikalische Hitbox (normale Größe)
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(Color.TRANSPARENT); // Transparent für Hitbox
                circle.setStroke(Color.TRANSPARENT); // Kein sichtbarer Rand
                
                // ImageView für das Ballon-Bild (unten überhängend für Schnur)
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    // Verwende das Bild mit korrekter Größe
                    Image balloonImage = pattern.getImage();
                    if (balloonImage != null) {
                        ImageView imageView = new ImageView(balloonImage);
                        // Skaliere das Bild so, dass es mehr nach oben und unten überhängt
                        double imageSize = radius * 2.8; // Noch größer für mehr Überhang
                        imageView.setFitWidth(imageSize);
                        imageView.setFitHeight(imageSize);
                        // Positioniere das Bild so, dass es mehr nach oben und unten überhängt
                        imageView.setX(radius - imageSize/2); // Zentriert horizontal
                        imageView.setY(radius - imageSize/2 + radius * 0.4); // Mehr nach unten verschoben
                        balloonGroup.getChildren().add(imageView);
                    }
                } else {
                    // Fallback: Farbiger Kreis
                    Circle colorCircle = new Circle(radius);
                    colorCircle.setCenterX(radius);
                    colorCircle.setCenterY(radius);
                    colorCircle.setFill(isPrePlaced ? Color.RED.darker() : Color.RED);
                    colorCircle.setStroke(Color.BLACK);
                    balloonGroup.getChildren().add(colorCircle);
                }
                
                // Füge die Hitbox als letztes hinzu (wichtig für CollisionManager)
                balloonGroup.getChildren().add(circle);
                
                balloonGroup.setLayoutX(x - radius);
                balloonGroup.setLayoutY(y - radius);
                node = balloonGroup;
                cfg = BalloonConf.class;
                break;
            }
            
            case "log": {
                LogConf tempConf = new LogConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                
                // Erstelle eine Group für Log + Bild
                Group logGroup = new Group();
                
                // Hauptkreis für die physikalische Hitbox
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                circle.setFill(Color.TRANSPARENT); // Transparent für Hitbox
                circle.setStroke(Color.TRANSPARENT); // Kein sichtbarer Rand
                
                // ImageView für das Log-Bild
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    // Verwende das Bild mit korrekter Größe
                    Image logImage = pattern.getImage();
                    if (logImage != null) {
                        ImageView imageView = new ImageView(logImage);
                        // Skaliere das Bild so, dass es die runde Form zeigt
                        double imageSize = radius * 2;
                        imageView.setFitWidth(imageSize);
                        imageView.setFitHeight(imageSize);
                        imageView.setPreserveRatio(true);
                        imageView.setX(radius - imageSize/2);
                        imageView.setY(radius - imageSize/2);
                        logGroup.getChildren().add(imageView);
                    }
                } else {
                    // Fallback: Braune Farbe für Log
                    Circle visualCircle = new Circle(radius);
                    visualCircle.setCenterX(radius);
                    visualCircle.setCenterY(radius);
                    visualCircle.setFill(isPrePlaced ? Color.BROWN.darker() : Color.BROWN);
                    visualCircle.setStroke(Color.DARKGOLDENROD);
                    logGroup.getChildren().add(visualCircle);
                }
                
                // Füge den transparenten Hitbox-Kreis hinzu
                logGroup.getChildren().add(circle);
                
                logGroup.setLayoutX(x - radius);
                logGroup.setLayoutY(y - radius);
                node = logGroup;
                cfg = LogConf.class;
                break;
            }
            
            case "plank": {
                PlankConf tempConf = new PlankConf(0, 0, 0, false);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    rect.setFill(pattern);
                } else {
                    rect.setFill(isPrePlaced ? Color.SADDLEBROWN.darker() : Color.SADDLEBROWN);
                }
                rect.setStroke(Color.BROWN);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = PlankConf.class;
                break;
            }
            
            case "domino": {
                DominoConf tempConf = new DominoConf(0, 0, 0, false);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    rect.setFill(pattern);
                } else {
                    rect.setFill(isPrePlaced ? Color.GRAY.darker() : Color.GRAY);
                }
                rect.setStroke(Color.DARKGRAY);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = DominoConf.class;
                break;
            }
            
            case "cratebox": {
                CrateboxConf tempConf = new CrateboxConf(0, 0, 0, false);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    rect.setFill(pattern);
                } else {
                    rect.setFill(isPrePlaced ? Color.ORANGE.darker() : Color.ORANGE);
                }
                rect.setStroke(Color.DARKORANGE);
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = CrateboxConf.class;
                break;
            }
            
            case "bucket": {
                BucketConf tempConf = new BucketConf(0, 0, 0, false);
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
            
            case "gameball": {
                GameBallConf tempConf = new GameBallConf(0, 0, 0, false);
                double radius = tempConf.getRadius() * SCALE;
                Circle circle = new Circle(radius);
                circle.setCenterX(radius);
                circle.setCenterY(radius);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    circle.setFill(pattern);
                } else {
                    circle.setFill(isPrePlaced ? Color.GREEN.darker() : Color.GREEN);
                }
                circle.setStroke(Color.DARKGREEN);
                circle.setLayoutX(x - radius);
                circle.setLayoutY(y - radius);
                node = circle;
                cfg = GameBallConf.class;
                break;
            }
            
            case "goalzone": {
                GoalZoneConf tempConf = new GoalZoneConf(0, 0, 0, true);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    rect.setFill(pattern);
                    rect.setOpacity(0.7);
                } else {
                    rect.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.3));
                }
                rect.setStroke(Color.DARKGREEN);
                rect.setStrokeWidth(2);
                rect.getStrokeDashArray().addAll(5d, 5d);
                
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = GoalZoneConf.class;
                break;
            }
            
            case "restrictionzone": {
                RestrictionZoneConf tempConf = new RestrictionZoneConf(0, 0, 0, true);
                double width = tempConf.getWidth() * SCALE;
                double height = tempConf.getHeight() * SCALE;
                Rectangle rect = new Rectangle(width, height);
                
                ImagePattern pattern = loadSkinPattern(tempConf.getSkinId());
                if (pattern != null) {
                    rect.setFill(pattern);
                    rect.setOpacity(0.7);
                } else {
                    rect.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
                }
                rect.setStroke(Color.DARKRED);
                rect.setStrokeWidth(2);
                rect.getStrokeDashArray().addAll(5d, 5d);
                
                rect.setLayoutX(x - width / 2);
                rect.setLayoutY(y - height / 2);
                node = rect;
                cfg = RestrictionZoneConf.class;
                break;
            }
            
            default:
                return null;
        }
        
        return new PlacedObject(node, cfg);
    }
    
    /**
     * Prüft, ob ein Node mit bestehenden Objekten kollidiert.
     * @param n Zu prüfender Node
     * @param ignore Node, der ignoriert werden soll
     * @param editorCanvas Zeichenfläche
     * @return true, wenn Überlappung vorliegt
     */
    public boolean overlapsExisting(Node n, Node ignore, Pane editorCanvas) {
        return collisionManager.overlapsExisting(n, ignore, placedObjects, prePlacedObjects);
    }
    
    /**
     * Prüft, ob ein Node innerhalb der Spielfeldgrenzen liegt.
     * @param node Zu prüfender Node
     * @param editorCanvas Zeichenfläche
     * @return true, wenn innerhalb
     */
    public boolean isWithinBounds(Node node, Pane editorCanvas) {
        javafx.geometry.Bounds bl = node.getBoundsInLocal();
        double minX = node.getLayoutX() + bl.getMinX();
        double minY = node.getLayoutY() + bl.getMinY();
        double maxX = node.getLayoutX() + bl.getMaxX();
        double maxY = node.getLayoutY() + bl.getMaxY();
        
        return minX >= 0 && minY >= 0 &&
               maxX <= editorCanvas.getWidth() &&
               maxY <= editorCanvas.getHeight();
    }
    
    /**
     * Fügt ein platziertes Objekt hinzu.
     * @param po PlacedObject
     * @param editorCanvas Zeichenfläche
     */
    public void addPlacedObject(PlacedObject po, Pane editorCanvas) {
        editorCanvas.getChildren().add(po.getNode());
        placedObjects.add(po);
    }
    
    /**
     * Fügt ein vorplatziertes Objekt hinzu.
     * @param po PlacedObject
     * @param editorCanvas Zeichenfläche
     */
    public void addPrePlacedObject(PlacedObject po, Pane editorCanvas) {
        editorCanvas.getChildren().add(po.getNode());
        prePlacedObjects.add(po);
    }
    
    /**
     * Entfernt ein platziertes Objekt.
     * @param po PlacedObject
     * @param editorCanvas Zeichenfläche
     */
    public void removePlacedObject(PlacedObject po, Pane editorCanvas) {
        editorCanvas.getChildren().remove(po.getNode());
        placedObjects.remove(po);
    }
    
    /**
     * Entfernt ein vorplatziertes Objekt.
     * @param po PlacedObject
     * @param editorCanvas Zeichenfläche
     */
    public void removePrePlacedObject(PlacedObject po, Pane editorCanvas) {
        editorCanvas.getChildren().remove(po.getNode());
        prePlacedObjects.remove(po);
    }
    
    /**
     * Sucht ein platziertes Objekt anhand des zugehörigen Nodes.
     * @param node Node
     * @return PlacedObject oder null
     */
    public PlacedObject findPlacedObjectByNode(Node node) {
        return placedObjects.stream()
                .filter(p -> p.getNode() == node)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Sucht ein vorplatziertes Objekt anhand des zugehörigen Nodes.
     * @param node Node
     * @return PlacedObject oder null
     */
    public PlacedObject findPrePlacedObjectByNode(Node node) {
        return prePlacedObjects.stream()
                .filter(p -> p.getNode() == node)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Richtet Dragging- und Auswahl-Events für ein Objekt ein.
     * @param node Node
     * @param editorCanvas Zeichenfläche
     * @param onSelectionChanged Callback bei Auswahl
     * @param onInventoryUpdate Callback bei Inventar-Update
     */
    public void setupObjectDragging(Node node, Pane editorCanvas, 
                                  Runnable onSelectionChanged, 
                                  Runnable onInventoryUpdate) {
        node.setPickOnBounds(true);
        node.setMouseTransparent(false);
        
        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }
        
        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (onSelectionChanged != null) {
                    onSelectionChanged.run();
                }
                event.consume();
            }
        });
    }
    
    /**
     * Richtet Events für vorplatzierte Objekte ein (Hover, Klick, etc.).
     * @param node Node
     */
    public void setupPrePlacedObjectEvents(Node node) {
        node.setPickOnBounds(true);
        node.setMouseTransparent(false);
        
        if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                child.setPickOnBounds(true);
                child.setMouseTransparent(false);
            }
        }
        
        node.setOnMouseEntered(event -> { 
            node.setOpacity(0.8); 
        });
        
        node.setOnMouseExited(event -> { 
            node.setOpacity(1); 
        });
        
        node.setOnMousePressed(event -> event.consume());
        node.setOnMouseDragged(event -> event.consume());
        node.setOnMouseReleased(event -> event.consume());
    }
    
    /**
     * Lädt ein Skin-Bild als Pattern.
     * @param skinId Skin-ID
     * @return ImagePattern oder null
     */
    private ImagePattern loadSkinPattern(String skinId) {
        try {
            String path = "/assets/entities/" + skinId + ".png";
            Image img = new Image(getClass().getResourceAsStream(path));
            return new ImagePattern(img);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Klemmt einen Wert zwischen zwei Grenzen ein.
     * @param v Wert
     * @param lo Untere Grenze
     * @param hi Obere Grenze
     * @return Eingeklemmter Wert
     */
    public static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : (v > hi) ? hi : v;
    }
} 