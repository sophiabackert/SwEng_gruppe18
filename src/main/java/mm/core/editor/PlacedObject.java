package mm.core.editor;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import mm.core.config.ObjectConf;
import mm.core.config.*;

public class PlacedObject {
    private final Node node;                  // GUI-Komponente (z. B. ImageView)
    private final Class<? extends ObjectConf> configClass;  // z. B. TennisballConf.class
    private final String skinId;              // z.B. "smallgear", "largegear", "drivechain"

    public PlacedObject(Node node, Class<? extends ObjectConf> configClass) {
        this(node, configClass, null);
    }
    
    public PlacedObject(Node node, Class<? extends ObjectConf> configClass, String skinId) {
        this.node = node;
        this.configClass = configClass;
        this.skinId = skinId;
    }

    public Node getNode() {
        return node;
    }

    public Class<? extends ObjectConf> getConfigClass() {
        return configClass;
    }
    
    public String getSkinId() {
        return skinId;
    }

    /**
     * Erstellt eine tiefe Kopie dieses PlacedObject
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
            
            // Kopiere alle Kinder der Gruppe
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
                }
            }
            
            copy.setLayoutX(original.getLayoutX());
            copy.setLayoutY(original.getLayoutY());
            copy.setRotate(original.getRotate());
            copiedNode = copy;
        } else {
            throw new IllegalStateException("Unsupported node type for copying: " + node.getClass());
        }
        
        return new PlacedObject(copiedNode, configClass, skinId);
    }

    public ObjectConf toConfig() {
        final float SCALE = 100f;           // 1 m = 100 px

        double cx, cy;                      // Mittelpunkt in Pixel

        /* ▶︎ Formabhängig Mittelpunkt bestimmen  */
        if (node instanceof Circle) {
            Circle c = (Circle) node;
            // Für Kreise: layoutX/Y + centerX/Y (da Kreise zentriert gezeichnet werden)
            cx = c.getLayoutX() + c.getCenterX();
            cy = c.getLayoutY() + c.getCenterY();
        } else if (node instanceof Rectangle) {
            Rectangle r = (Rectangle) node;
            // Für Rechtecke: layoutX/Y + halbe Breite/Höhe
            cx = r.getLayoutX() + r.getWidth()  / 2;
            cy = r.getLayoutY() + r.getHeight() / 2;
        } else if (node instanceof Group) {
            // Für Groups (z.B. Bucket): layoutX/Y ist bereits der Mittelpunkt
            cx = node.getLayoutX();
            cy = node.getLayoutY();
        } else {
            // Fallback: layoutX/Y verwenden
            cx = node.getLayoutX();
            cy = node.getLayoutY();
        }

        // WICHTIG: Wenn das Node in einer Gruppe ist, addiere die Gruppenposition
        if (node.getParent() instanceof Group) {
            Group parentGroup = (Group) node.getParent();
            cx += parentGroup.getLayoutX();
            cy += parentGroup.getLayoutY();
            System.out.println("Node in group during save: node(" + node.getLayoutX() + "," + node.getLayoutY() + ") + group(" + parentGroup.getLayoutX() + "," + parentGroup.getLayoutY() + ") = (" + cx + "," + cy + ")");
        } else {
            System.out.println("Node standalone during save: (" + cx + "," + cy + ")");
        }

        float x = (float) (cx / SCALE);     // Pixel → Meter
        float y = (float) (cy / SCALE);
        float angle = (float) Math.toRadians(node.getRotate());
        boolean isStatic = false;           // ggf. anpassen

        // Spezifische Config-Objekte basierend auf dem Typ erstellen
        if (configClass == mm.core.config.TennisballConf.class) {
            return new mm.core.config.TennisballConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.BowlingballConf.class) {
            return new mm.core.config.BowlingballConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.BilliardballConf.class) {
            return new mm.core.config.BilliardballConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.BalloonConf.class) {
            return new mm.core.config.BalloonConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.LogConf.class) {
            return new mm.core.config.LogConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.PlankConf.class) {
            return new mm.core.config.PlankConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.DominoConf.class) {
            return new mm.core.config.DominoConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.CrateboxConf.class) {
            return new mm.core.config.CrateboxConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.BucketConf.class) {
            return new mm.core.config.BucketConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.GameBallConf.class) {
            return new mm.core.config.GameBallConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.GoalZoneConf.class) {
            return new mm.core.config.GoalZoneConf(x, y, angle, isStatic);
        } else if (configClass == mm.core.config.GearConf.class) {
            // Bestimme Zahnrad-Größe basierend auf skinId
            GearConf.GearSize gearSize = "largegear".equals(skinId) ? 
                                       GearConf.GearSize.LARGE : 
                                       GearConf.GearSize.SMALL;
            return new mm.core.config.GearConf(x, y, angle, isStatic, gearSize);
        } else if (configClass == mm.core.config.DriveChainConf.class) {
            // Für Antriebsstränge müssen die Zahnrad-IDs aus der skinId oder anderem Mechanismus geholt werden
            // TODO: Implementierung vervollständigen wenn DriveChain-Speicherung implementiert wird
            return new mm.core.config.DriveChainConf(x, y, angle, "gear1", "gear2");
        } else if (configClass == mm.core.config.PaddleConf.class) {
            // Für Paddles muss die angebundene Zahnrad-ID ermittelt werden
            // TODO: Diese Informationen müssen von der LevelEditorController bereitgestellt werden
            return new mm.core.config.PaddleConf(x, y, angle, "unknown_gear");
        }

        throw new IllegalStateException("Unknown config type: " + configClass);
    }
}