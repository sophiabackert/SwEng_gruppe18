package mm.domain.editor;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
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

/**
 * Repräsentiert ein platziertes Objekt im Editor (z. B. im Level- oder Game-Editor).
 * <p>
 * Kapselt die JavaFX-Node (grafische Darstellung) und die zugehörige Konfigurationsklasse
 * (z. B. TennisballConf, PlankConf etc.). Bietet Methoden zum Kopieren und zur Umwandlung
 * in eine Konfigurationsinstanz für die Serialisierung.
 * </p>
 */
public class PlacedObject {
    /** Die grafische JavaFX-Node, die das Objekt im Editor repräsentiert */
    private final Node node;
    /** Die zugehörige Konfigurationsklasse (z. B. TennisballConf.class) */
    private final Class<? extends ObjectConf> configClass;

    /**
     * Erstellt ein neues platziertes Objekt.
     * @param node Die JavaFX-Node
     * @param configClass Die zugehörige Konfigurationsklasse
     */
    public PlacedObject(Node node, Class<? extends ObjectConf> configClass) {
        this.node = node;
        this.configClass = configClass;
    }

    /**
     * @return Die JavaFX-Node des Objekts
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return Die zugehörige Konfigurationsklasse
     */
    public Class<? extends ObjectConf> getConfigClass() {
        return configClass;
    }

    /**
     * Erzeugt eine tiefe Kopie dieses platzierten Objekts (inkl. Node).
     * @return Neue PlacedObject-Instanz mit kopierter Node
     */
    public PlacedObject copy() {
        Node copiedNode;
        
        if (node instanceof Circle) {
            Circle original = (Circle) node;
            Circle copy = new Circle(original.getCenterX(), original.getCenterY(), original.getRadius());
            copy.setFill(original.getFill());
            copy.setStroke(original.getStroke());
            copy.setLayoutX(original.getLayoutX());
            copy.setLayoutY(original.getLayoutY());
            copy.setRotate(original.getRotate());
            copiedNode = copy;
        } else if (node instanceof Rectangle) {
            Rectangle original = (Rectangle) node;
            Rectangle copy = new Rectangle(original.getWidth(), original.getHeight());
            copy.setFill(original.getFill());
            copy.setStroke(original.getStroke());
            copy.setLayoutX(original.getLayoutX());
            copy.setLayoutY(original.getLayoutY());
            copy.setRotate(original.getRotate());
            copiedNode = copy;
        } else if (node instanceof ImageView) {
            ImageView original = (ImageView) node;
            ImageView copy = new ImageView(original.getImage());
            copy.setFitWidth(original.getFitWidth());
            copy.setFitHeight(original.getFitHeight());
            copy.setPreserveRatio(original.isPreserveRatio());
            copy.setLayoutX(original.getLayoutX());
            copy.setLayoutY(original.getLayoutY());
            copy.setRotate(original.getRotate());
            copiedNode = copy;
        } else if (node instanceof Group) {
            Group original = (Group) node;
            Group copy = new Group();
            
            for (Node child : original.getChildren()) {
                if (child instanceof Circle) {
                    Circle childOriginal = (Circle) child;
                    Circle childCopy = new Circle(childOriginal.getCenterX(), childOriginal.getCenterY(), childOriginal.getRadius());
                    childCopy.setFill(childOriginal.getFill());
                    childCopy.setStroke(childOriginal.getStroke());
                    copy.getChildren().add(childCopy);
                } else if (child instanceof Rectangle) {
                    Rectangle childOriginal = (Rectangle) child;
                    Rectangle childCopy = new Rectangle(childOriginal.getWidth(), childOriginal.getHeight());
                    childCopy.setFill(childOriginal.getFill());
                    childCopy.setStroke(childOriginal.getStroke());
                    childCopy.setLayoutX(childOriginal.getLayoutX());
                    childCopy.setLayoutY(childOriginal.getLayoutY());
                    copy.getChildren().add(childCopy);
                } else if (child instanceof ImageView) {
                    ImageView childOriginal = (ImageView) child;
                    ImageView childCopy = new ImageView(childOriginal.getImage());
                    childCopy.setFitWidth(childOriginal.getFitWidth());
                    childCopy.setFitHeight(childOriginal.getFitHeight());
                    childCopy.setPreserveRatio(childOriginal.isPreserveRatio());
                    childCopy.setX(childOriginal.getX());
                    childCopy.setY(childOriginal.getY());
                    copy.getChildren().add(childCopy);
                } else if (child instanceof Line) {
                    Line childOriginal = (Line) child;
                    Line childCopy = new Line(
                        childOriginal.getStartX(), childOriginal.getStartY(),
                        childOriginal.getEndX(), childOriginal.getEndY()
                    );
                    childCopy.setStroke(childOriginal.getStroke());
                    childCopy.setStrokeWidth(childOriginal.getStrokeWidth());
                    copy.getChildren().add(childCopy);
                }
            }
            
            copy.setLayoutX(original.getLayoutX());
            copy.setLayoutY(original.getLayoutY());
            copy.setRotate(original.getRotate());
            copiedNode = copy;
        } else {
            throw new IllegalStateException("Unsupported node type for copying: " + node.getClass());
        }
        
        return new PlacedObject(copiedNode, configClass);
    }

    /**
     * Wandelt das platzierte Objekt in eine Konfigurationsinstanz (ObjectConf) um.
     * Die Position und Rotation werden aus der Node extrahiert und auf das Spielfeldmaß umgerechnet.
     * @return Die entsprechende ObjectConf-Instanz
     * @throws IllegalStateException bei unbekannter Konfigurationsklasse
     */
    public ObjectConf toConfig() {
        final float SCALE = 100f;

        double cx, cy;

        if (node instanceof Circle) {
            Circle c = (Circle) node;
            cx = c.getLayoutX() + c.getCenterX();
            cy = c.getLayoutY() + c.getCenterY();
        } else if (node instanceof Rectangle) {
            Rectangle r = (Rectangle) node;
            cx = r.getLayoutX() + r.getWidth()  / 2;
            cy = r.getLayoutY() + r.getHeight() / 2;
        } else if (node instanceof Group) {
            cx = node.getLayoutX();
            cy = node.getLayoutY();
        } else {
            cx = node.getLayoutX();
            cy = node.getLayoutY();
        }

        float x = (float) (cx / SCALE);
        float y = (float) (cy / SCALE);
        float angle = (float) Math.toRadians(node.getRotate());
        boolean isStatic = false;

        if (configClass == TennisballConf.class) {
            return new TennisballConf(x, y, angle, isStatic);
        } else if (configClass == BowlingballConf.class) {
            return new BowlingballConf(x, y, angle, isStatic);
        } else if (configClass == BilliardballConf.class) {
            return new BilliardballConf(x, y, angle, isStatic);
        } else if (configClass == BalloonConf.class) {
            return new BalloonConf(x, y, angle, isStatic);
        } else if (configClass == LogConf.class) {
            return new LogConf(x, y, angle, isStatic);
        } else if (configClass == PlankConf.class) {
            return new PlankConf(x, y, angle, isStatic);
        } else if (configClass == DominoConf.class) {
            return new DominoConf(x, y, angle, isStatic);
        } else if (configClass == CrateboxConf.class) {
            return new CrateboxConf(x, y, angle, isStatic);
        } else if (configClass == BucketConf.class) {
            return new BucketConf(x, y, angle, isStatic);
        } else if (configClass == GameBallConf.class) {
            return new GameBallConf(x, y, angle, isStatic);
        } else if (configClass == GoalZoneConf.class) {
            return new GoalZoneConf(x, y, angle, isStatic);
        } else if (configClass == RestrictionZoneConf.class) {
            return new RestrictionZoneConf(x, y, angle, isStatic);
        }

        throw new IllegalStateException("Unknown config type: " + configClass);
    }
}