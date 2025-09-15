package mm.rules;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import mm.objects.GameObject;
import org.jbox2d.common.Vec2;

import java.util.List;
import java.util.Map;

/**
 * Verwaltet die Regeln für die Platzierung von Objekten im Level Editor.
 */
public class PlacementRules {
    private static final double GRID_SIZE = 20.0; // Größe des Rasters in Pixeln
    private static final double MIN_DISTANCE = 10.0; // Minimaler Abstand zwischen Objekten
    private static final double CANVAS_PADDING = 20.0; // Mindestabstand zum Rand
    
    /**
     * Prüft, ob ein Objekt an der angegebenen Position platziert werden kann.
     */
    public static boolean canPlaceObject(double x, double y, String type, 
                                       Map<Node, GameObject> existingObjects,
                                       double canvasWidth, double canvasHeight) {
        // Prüfe Grenzen der Zeichenfläche
        if (!isWithinCanvas(x, y, type, canvasWidth, canvasHeight)) {
            return false;
        }
        
        // Prüfe Überlappungen mit anderen Objekten
        if (overlapsWithOtherObjects(x, y, type, existingObjects)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Prüft, ob die Position innerhalb der Zeichenfläche liegt.
     */
    private static boolean isWithinCanvas(double x, double y, String type, 
                                        double canvasWidth, double canvasHeight) {
        double objectWidth = getObjectWidth(type);
        double objectHeight = getObjectHeight(type);
        
        return x >= CANVAS_PADDING && 
               x <= canvasWidth - CANVAS_PADDING - objectWidth &&
               y >= CANVAS_PADDING && 
               y <= canvasHeight - CANVAS_PADDING - objectHeight;
    }
    
    /**
     * Prüft, ob das Objekt mit anderen Objekten überlappt.
     */
    private static boolean overlapsWithOtherObjects(double x, double y, String type,
                                                  Map<Node, GameObject> existingObjects) {
        double objectWidth = getObjectWidth(type);
        double objectHeight = getObjectHeight(type);
        
        Bounds newObjectBounds = new javafx.geometry.BoundingBox(
            x - objectWidth/2, y - objectHeight/2, objectWidth, objectHeight
        );
        
        for (Node node : existingObjects.keySet()) {
            Bounds existingBounds = node.getBoundsInParent();
            if (newObjectBounds.intersects(existingBounds)) {
                return true;
            }
            
            // Prüfe auch den Mindestabstand
            if (isWithinMinDistance(newObjectBounds, existingBounds)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Prüft, ob zwei Objekte zu nahe beieinander sind.
     */
    private static boolean isWithinMinDistance(Bounds bounds1, Bounds bounds2) {
        double centerX1 = bounds1.getMinX() + bounds1.getWidth() / 2;
        double centerY1 = bounds1.getMinY() + bounds1.getHeight() / 2;
        double centerX2 = bounds2.getMinX() + bounds2.getWidth() / 2;
        double centerY2 = bounds2.getMinY() + bounds2.getHeight() / 2;
        
        double distance = Math.sqrt(
            Math.pow(centerX2 - centerX1, 2) + 
            Math.pow(centerY2 - centerY1, 2)
        );
        
        return distance < MIN_DISTANCE;
    }
    
    /**
     * Rundet eine Position auf das Raster.
     */
    public static double snapToGrid(double value) {
        return Math.round(value / GRID_SIZE) * GRID_SIZE;
    }
    
    /**
     * Gibt die Breite eines Objekttyps zurück.
     */
    private static double getObjectWidth(String type) {
        switch (type) {
            case "plank":
                return 60;
            case "bucket":
                return 50;
            default:
                return 40;
        }
    }
    
    /**
     * Gibt die Höhe eines Objekttyps zurück.
     */
    private static double getObjectHeight(String type) {
        switch (type) {
            case "plank":
                return 20;
            case "bucket":
                return 50;
            default:
                return 40;
        }
    }
}
 