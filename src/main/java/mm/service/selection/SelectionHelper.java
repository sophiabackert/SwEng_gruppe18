package mm.service.selection;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;

/**
 * Hilfsklasse zur Verwaltung der Auswahl und Hervorhebung von Objekten im Editor.
 * <p>
 * Unterstützt das Hervorheben, Entfernen der Hervorhebung, das Löschen der Auswahl und das Aktivieren/Deaktivieren von Rotationsbuttons.
 * </p>
 */
public class SelectionHelper {
    /**
     * Hebt das angegebene Node-Objekt hervor (z.B. durch orange Umrandung).
     * @param node Das zu markierende Node
     * @param editorCanvas Zeichenfläche
     */
    public static void highlightSelected(Node node, Pane editorCanvas) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStrokeWidth(3.0);
            circle.setStroke(Color.ORANGE);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStrokeWidth(3.0);
            rect.setStroke(Color.ORANGE);
        } else if (node instanceof Group) {
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

    /**
     * Entfernt die Hervorhebung vom angegebenen Node-Objekt.
     * @param node Das zu entmarkierende Node
     * @param editorCanvas Zeichenfläche
     */
    public static void removeHighlight(Node node, Pane editorCanvas) {
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            circle.setStrokeWidth(1.0);
            circle.setStroke(Color.BLACK);
        } else if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            rect.setStrokeWidth(1.0);
            if (rect.getFill() instanceof ImagePattern) {
                rect.setStroke(null);
            } else {
                rect.setStroke(getOriginalStroke(rect));
            }
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                if (child instanceof Line) {
                    Line line = (Line) child;
                    line.setStrokeWidth(0.05 * 100.0);
                    line.setStroke(Color.BLUE);
                }
            }
        }
    }

    /**
     * Gibt die ursprüngliche Umrandungsfarbe für ein Rechteck zurück.
     * @param rect Rechteck
     * @return Ursprüngliche Farbe
     */
    private static Color getOriginalStroke(Rectangle rect) {
        Color fill = (Color) rect.getFill();
        if (fill.equals(Color.SADDLEBROWN)) return Color.DARKGOLDENROD;
        if (fill.equals(Color.LIGHTGRAY)) return Color.BLACK;
        if (fill.equals(Color.BURLYWOOD)) return Color.SADDLEBROWN;
        return Color.BLACK;
    }

    /**
     * Entfernt die Auswahl und deren Hervorhebung.
     * @param editorCanvas Zeichenfläche
     * @param selectedNode Aktuell ausgewähltes Node
     * @return null (keine Auswahl mehr)
     */
    public static Node clearSelection(Pane editorCanvas, Node selectedNode) {
        if (selectedNode != null) {
            removeHighlight(selectedNode, editorCanvas);
            selectedNode = null;
        }
        return selectedNode;
    }

    /**
     * Aktiviert oder deaktiviert die Rotationsbuttons je nach Auswahl.
     * @param rotateLeftButton Button für Linksrotation
     * @param rotateRightButton Button für Rechtsrotation
     * @param selectedNode Aktuell ausgewähltes Node
     */
    public static void updateRotationButtons(Button rotateLeftButton, Button rotateRightButton, Node selectedNode) {
        boolean hasSelection = selectedNode != null;
        rotateLeftButton.setDisable(!hasSelection);
        rotateRightButton.setDisable(!hasSelection);
    }
} 