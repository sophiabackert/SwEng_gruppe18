package mm.service.collision;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.geometry.Bounds;
import mm.domain.editor.PlacedObject;
import mm.domain.config.GoalZoneConf;
import mm.domain.config.RestrictionZoneConf;
import java.util.List;

/**
 * Verwaltet Kollisionserkennung und Überlappungsprüfungen für platzierte Objekte im Editor.
 * <p>
 * Unterstützt verschiedene Objektformen (Kreise, Rechtecke, Eimer/Bucket) und spezielle Zonen (GoalZone, RestrictionZone).
 * Bietet Methoden zur Überprüfung von Überlappungen, Kollisionen und speziellen Regeln für bestimmte Objekttypen.
 * </p>
 */
public class CollisionManager {
    /**
     * Prüft, ob ein Node mit bestehenden Objekten (platziert oder vorplatziert) kollidiert.
     * @param n Zu prüfender Node
     * @param ignore Node, der ignoriert werden soll (z.B. das zu bewegende Objekt selbst)
     * @param placedObjects Liste der platzierten Objekte
     * @param prePlacedObjects Liste der vorplatzierten Objekte
     * @return true, wenn eine Überlappung vorliegt
     */
    public boolean overlapsExisting(Node n, Node ignore, List<PlacedObject> placedObjects, List<PlacedObject> prePlacedObjects) {
        for (PlacedObject po : placedObjects) {
            Node other = po.getNode();
            if (other == n || other == ignore) continue;
            if (checkObjectCollision(n, other, placedObjects, prePlacedObjects)) {
                return true;
            }
        }
        
        for (PlacedObject po : prePlacedObjects) {
            Node other = po.getNode();
            if (other == n || other == ignore) continue;
            if (checkObjectCollision(n, other, placedObjects, prePlacedObjects)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Prüft, ob zwei Nodes kollidieren, unter Berücksichtigung von Spezialregeln (Goalzone, RestrictionZone, Bucket).
     * @param node1 Erstes Objekt
     * @param node2 Zweites Objekt
     * @param placedObjects Liste der platzierten Objekte
     * @param prePlacedObjects Liste der vorplatzierten Objekte
     * @return true, wenn eine Kollision vorliegt
     */
    public boolean checkObjectCollision(Node node1, Node node2, List<PlacedObject> placedObjects, List<PlacedObject> prePlacedObjects) {
        if (isGoalzone(node1, placedObjects, prePlacedObjects) || isGoalzone(node2, placedObjects, prePlacedObjects)) {
            return false;
        }
        
        ObjectInfo obj1 = getObjectInfo(node1);
        ObjectInfo obj2 = getObjectInfo(node2);
        
        if (obj1 == null || obj2 == null) return false;
        
        if (isRestrictionZone(node1, placedObjects, prePlacedObjects) || isRestrictionZone(node2, placedObjects, prePlacedObjects)) {
            return checkGeneralCollision(obj1, obj2);
        }
        
        if (obj1.type == ObjectType.BUCKET) {
            return !isInsideBucket(obj2, obj1, node1) && checkGeneralCollision(obj1, obj2);
        }
        if (obj2.type == ObjectType.BUCKET) {
            return !isInsideBucket(obj1, obj2, node2) && checkGeneralCollision(obj1, obj2);
        }
        
        return checkGeneralCollision(obj1, obj2);
    }

    /**
     * Prüft, ob ein Node eine GoalZone ist.
     * @param node Zu prüfender Node
     * @param placedObjects Platzierte Objekte
     * @param prePlacedObjects Vorplatzierte Objekte
     * @return true, wenn es sich um eine GoalZone handelt
     */
    private boolean isGoalzone(Node node, List<PlacedObject> placedObjects, List<PlacedObject> prePlacedObjects) {
        for (PlacedObject po : placedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == GoalZoneConf.class;
            }
        }
        for (PlacedObject po : prePlacedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == GoalZoneConf.class;
            }
        }
        return false;
    }

    /**
     * Prüft, ob ein Node eine RestrictionZone ist.
     * @param node Zu prüfender Node
     * @param placedObjects Platzierte Objekte
     * @param prePlacedObjects Vorplatzierte Objekte
     * @return true, wenn es sich um eine RestrictionZone handelt
     */
    private boolean isRestrictionZone(Node node, List<PlacedObject> placedObjects, List<PlacedObject> prePlacedObjects) {
        for (PlacedObject po : placedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == RestrictionZoneConf.class;
            }
        }
        for (PlacedObject po : prePlacedObjects) {
            if (po.getNode() == node) {
                return po.getConfigClass() == RestrictionZoneConf.class;
            }
        }
        return false;
    }

    /**
     * Prüft die allgemeine Kollision zwischen zwei Objekten (Kreis, Rechteck, Bucket).
     * @param obj1 Erstes Objekt
     * @param obj2 Zweites Objekt
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkGeneralCollision(ObjectInfo obj1, ObjectInfo obj2) {
        if (obj1.type == ObjectType.CIRCLE && obj2.type == ObjectType.CIRCLE) {
            return checkCircleCircleCollision(obj1, obj2);
        } else if (obj1.type == ObjectType.CIRCLE && obj2.type == ObjectType.RECTANGLE) {
            return checkCircleRectangleCollision(obj1, obj2);
        } else if (obj1.type == ObjectType.RECTANGLE && obj2.type == ObjectType.CIRCLE) {
            return checkCircleRectangleCollision(obj2, obj1);
        } else if (obj1.type == ObjectType.RECTANGLE && obj2.type == ObjectType.RECTANGLE) {
            return checkRectangleRectangleCollision(obj1, obj2);
        }
        
        return obj1.bounds.intersects(obj2.bounds);
    }

    /**
     * Prüft, ob ein Objekt innerhalb eines Buckets liegt.
     * @param obj Zu prüfendes Objekt
     * @param bucket Bucket-Objekt
     * @param bucketNode Node des Buckets
     * @return true, wenn das Objekt im Bucket liegt
     */
    private boolean isInsideBucket(ObjectInfo obj, ObjectInfo bucket, Node bucketNode) {
        if (bucket.type != ObjectType.BUCKET || !(bucketNode instanceof Group)) return false;
        
        Group bucketGroup = (Group) bucketNode;
        
        java.util.List<javafx.scene.shape.Line> bucketLines = new java.util.ArrayList<>();
        for (Node child : bucketGroup.getChildren()) {
            if (child instanceof javafx.scene.shape.Line) {
                bucketLines.add((javafx.scene.shape.Line) child);
            }
        }
        
        if (bucketLines.isEmpty()) return false;
        
        for (javafx.scene.shape.Line line : bucketLines) {
            if (checkObjectLineCollision(obj, line, bucketNode.getLayoutX(), bucketNode.getLayoutY())) {
                return false;
            }
        }
        
        double objCenterX = obj.centerX;
        double bucketCenterX = bucket.centerX;
        double bucketWidth = bucket.width;
        
        boolean roughlyInside = Math.abs(objCenterX - bucketCenterX) < bucketWidth / 2 + 10;
        
        return roughlyInside;
    }

    /**
     * Prüft, ob ein Objekt mit einer Linie kollidiert (z.B. für Bucket-Kanten).
     * @param obj Objektinfo
     * @param line Linie
     * @param bucketX X-Position des Buckets
     * @param bucketY Y-Position des Buckets
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkObjectLineCollision(ObjectInfo obj, javafx.scene.shape.Line line, double bucketX, double bucketY) {
        double x1 = line.getStartX() + bucketX;
        double y1 = line.getStartY() + bucketY;
        double x2 = line.getEndX() + bucketX;
        double y2 = line.getEndY() + bucketY;
        
        if (obj.type == ObjectType.CIRCLE) {
            return checkCircleLineCollision(obj.centerX, obj.centerY, obj.radius, x1, y1, x2, y2);
        } else {
            double[][] corners = getRectangleCorners(obj);
            for (double[] corner : corners) {
                if (checkPointToLineDistance(corner[0], corner[1], x1, y1, x2, y2) < 1) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Prüft, ob ein Kreis mit einer Linie kollidiert.
     * @param cx Mittelpunkt X des Kreises
     * @param cy Mittelpunkt Y des Kreises
     * @param radius Radius des Kreises
     * @param x1 Startpunkt X der Linie
     * @param y1 Startpunkt Y der Linie
     * @param x2 Endpunkt X der Linie
     * @param y2 Endpunkt Y der Linie
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkCircleLineCollision(double cx, double cy, double radius, double x1, double y1, double x2, double y2) {
        double distance = checkPointToLineDistance(cx, cy, x1, y1, x2, y2);
        return distance < radius + 1;
    }

    /**
     * Berechnet den Abstand eines Punkts zu einer Linie.
     * @param px Punkt X
     * @param py Punkt Y
     * @param x1 Startpunkt X der Linie
     * @param y1 Startpunkt Y der Linie
     * @param x2 Endpunkt X der Linie
     * @param y2 Endpunkt Y der Linie
     * @return Abstand
     */
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

    /**
     * Prüft die Kollision zweier Kreise.
     * @param circle1 Erstes Kreisobjekt
     * @param circle2 Zweites Kreisobjekt
     * @return true, wenn sich die Kreise überlappen
     */
    private boolean checkCircleCircleCollision(ObjectInfo circle1, ObjectInfo circle2) {
        double dx = circle1.centerX - circle2.centerX;
        double dy = circle1.centerY - circle2.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = circle1.radius + circle2.radius + 1;
        return distance < minDistance;
    }

    /**
     * Prüft die Kollision zwischen Kreis und Rechteck.
     * @param circle Kreisobjekt
     * @param rect Rechteckobjekt
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkCircleRectangleCollision(ObjectInfo circle, ObjectInfo rect) {
        if (Math.abs(rect.rotation) > 0.1) {
            return checkCircleToRotatedRectangle(circle, rect);
        }
        
        double closestX = Math.max(rect.centerX - rect.width/2, 
                         Math.min(circle.centerX, rect.centerX + rect.width/2));
        double closestY = Math.max(rect.centerY - rect.height/2, 
                         Math.min(circle.centerY, rect.centerY + rect.height/2));
        
        double dx = circle.centerX - closestX;
        double dy = circle.centerY - closestY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance < (circle.radius + 1);
    }

    /**
     * Prüft die Kollision zweier Rechtecke (ggf. rotiert).
     * @param rect1 Erstes Rechteck
     * @param rect2 Zweites Rechteck
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkRectangleRectangleCollision(ObjectInfo rect1, ObjectInfo rect2) {
        if (Math.abs(rect1.rotation) > 0.1 || Math.abs(rect2.rotation) > 0.1) {
            return checkRotatedRectangles(rect1, rect2);
        }
        
        double puffer = 1;
        
        return !(rect1.centerX + rect1.width/2 + puffer <= rect2.centerX - rect2.width/2 ||
                 rect1.centerX - rect1.width/2 - puffer >= rect2.centerX + rect2.width/2 ||
                 rect1.centerY + rect1.height/2 + puffer <= rect2.centerY - rect2.height/2 ||
                 rect1.centerY - rect1.height/2 - puffer >= rect2.centerY + rect2.height/2);
    }

    /**
     * Prüft die Kollision zwischen Kreis und rotiertem Rechteck.
     * @param circle Kreisobjekt
     * @param rect Rechteckobjekt (rotiert)
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkCircleToRotatedRectangle(ObjectInfo circle, ObjectInfo rect) {
        double dx = circle.centerX - rect.centerX;
        double dy = circle.centerY - rect.centerY;
        
        double cos = Math.cos(-rect.rotation);
        double sin = Math.sin(-rect.rotation);
        double localX = dx * cos - dy * sin;
        double localY = dx * sin + dy * cos;
        
        double closestX = Math.max(-rect.width/2, Math.min(localX, rect.width/2));
        double closestY = Math.max(-rect.height/2, Math.min(localY, rect.height/2));
        
        double distanceX = localX - closestX;
        double distanceY = localY - closestY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        
        return distance < (circle.radius + 1);
    }

    /**
     * Prüft die Kollision zweier rotierten Rechtecke mittels Separating Axis Theorem.
     * @param rect1 Erstes Rechteck
     * @param rect2 Zweites Rechteck
     * @return true, wenn eine Kollision vorliegt
     */
    private boolean checkRotatedRectangles(ObjectInfo rect1, ObjectInfo rect2) {
        double[][] corners1 = getRectangleCorners(rect1);
        double[][] corners2 = getRectangleCorners(rect2);
        
        double[][] axes = {
            {Math.cos(rect1.rotation), Math.sin(rect1.rotation)},
            {-Math.sin(rect1.rotation), Math.cos(rect1.rotation)},
            {Math.cos(rect2.rotation), Math.sin(rect2.rotation)},
            {-Math.sin(rect2.rotation), Math.cos(rect2.rotation)}
        };
        
        for (double[] axis : axes) {
            if (isSeparatingAxis(corners1, corners2, axis)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Berechnet die Eckpunkte eines Rechtecks (ggf. rotiert).
     * @param rect Rechteckobjekt
     * @return Array mit vier Eckpunkten
     */
    private double[][] getRectangleCorners(ObjectInfo rect) {
        double halfWidth = rect.width / 2;
        double halfHeight = rect.height / 2;
        double cos = Math.cos(rect.rotation);
        double sin = Math.sin(rect.rotation);
        
        double[][] localCorners = {
            {-halfWidth, -halfHeight}, {halfWidth, -halfHeight},
            {halfWidth, halfHeight}, {-halfWidth, halfHeight}
        };
        
        double[][] corners = new double[4][2];
        for (int i = 0; i < 4; i++) {
            double x = localCorners[i][0];
            double y = localCorners[i][1];
            corners[i][0] = rect.centerX + (x * cos - y * sin);
            corners[i][1] = rect.centerY + (x * sin + y * cos);
        }
        
        return corners;
    }

    /**
     * Prüft, ob eine Achse eine Separating Axis zwischen zwei Rechtecken ist.
     * @param corners1 Eckpunkte Rechteck 1
     * @param corners2 Eckpunkte Rechteck 2
     * @param axis Zu prüfende Achse
     * @return true, wenn die Achse trennt (keine Kollision)
     */
    private boolean isSeparatingAxis(double[][] corners1, double[][] corners2, double[] axis) {
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
        
        return max1 + 1 < min2 || max2 + 1 < min1;
    }

    /**
     * Extrahiert die Objektinformationen (Typ, Geometrie, Bounds) aus einem Node.
     * @param node Zu analysierender Node
     * @return Objektinfo oder null
     */
    private ObjectInfo getObjectInfo(Node node) {
        double centerX, centerY, width = 0, height = 0, radius = 0;
        ObjectType type;
        javafx.geometry.Bounds bounds = node.localToParent(node.getBoundsInLocal());
        double rotation = Math.toRadians(node.getRotate());
        
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
            
            if (!group.getChildren().isEmpty() && group.getChildren().get(0) instanceof javafx.scene.shape.Line) {
                javafx.scene.shape.Line bottomLine = (javafx.scene.shape.Line) group.getChildren().get(0);
                width = Math.abs(bottomLine.getEndX() - bottomLine.getStartX());
                height = 50;
                type = ObjectType.BUCKET;
            } else {
                for (Node child : group.getChildren()) {
                    if (child instanceof javafx.scene.shape.Circle) {
                        javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) child;
                        centerX = node.getLayoutX() + circle.getCenterX();
                        centerY = node.getLayoutY() + circle.getCenterY();
                        radius = circle.getRadius();
                        type = ObjectType.CIRCLE;
                        return new ObjectInfo(type, centerX, centerY, width, height, radius, rotation, bounds);
                    }
                }
                width = bounds.getWidth();
                height = bounds.getHeight();
                type = ObjectType.RECTANGLE;
            }
        } else {
            centerX = bounds.getCenterX();
            centerY = bounds.getCenterY();
            width = bounds.getWidth();
            height = bounds.getHeight();
            type = ObjectType.RECTANGLE;
        }
        
        return new ObjectInfo(type, centerX, centerY, width, height, radius, rotation, bounds);
    }

    /**
     * Interne Aufzählung für Objekttypen.
     */
    private enum ObjectType {
        CIRCLE, RECTANGLE, BUCKET
    }

    /**
     * Hilfsklasse zur Speicherung von Objektinformationen für die Kollisionserkennung.
     */
    private static class ObjectInfo {
        final ObjectType type;
        final double centerX, centerY;
        final double width, height, radius, rotation;
        final Bounds bounds;
        
        ObjectInfo(ObjectType type, double centerX, double centerY, double width, double height, double radius, double rotation, Bounds bounds) {
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
} 