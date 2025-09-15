package mm.objects.components;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mm.objects.Component;
import mm.objects.GameObject;
import org.jbox2d.common.Vec2;

/**
 * Komponente für die visuelle Darstellung eines GameObjects.
 */
public class RenderComponent extends Component {
    private Node visualNode;
    private boolean followPhysics = true;
    private float offsetX = 0;
    private float offsetY = 0;
    private float scale = 1.0f;
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private double width = 32;
    private double height = 32;
    private double strokeWidth = 1.0;
    private double opacity = 1.0;
    private boolean visible = true;
    private static final float M_TO_PX = 80.0f; // 1 Meter = 80 Pixel

    /**
     * Erstellt eine neue RenderComponent für ein GameObject.
     * @param gameObject Das GameObject, zu dem diese Komponente gehört
     */
    public RenderComponent(GameObject gameObject) {
        super(gameObject);
    }

    /**
     * Erstellt eine neue RenderComponent mit einem spezifischen visuellen Node.
     * @param gameObject Das GameObject, zu dem diese Komponente gehört
     * @param visualNode Der JavaFX-Node für die visuelle Darstellung
     */
    public RenderComponent(GameObject gameObject, Node visualNode) {
        super(gameObject);
        this.visualNode = visualNode;
    }

    @Override
    protected void onAttach() {
        updateTransform();
    }

    @Override
    protected void onDetach() {
        // Optional: Node aus der Scene entfernen
    }

    @Override
    public void update(float deltaTime) {
        if (followPhysics) {
            updateTransform();
        }
    }

    /**
     * Aktualisiert die Transformation des visuellen Nodes.
     */
    private void updateTransform() {
        GameObject gameObject = getGameObject();
        if (gameObject != null && visualNode != null) {
            PhysicsComponent physics = gameObject.getComponent(PhysicsComponent.class);
            if (physics != null && physics.getBody() != null) {
                // Position aus der Physik-Engine
                Vec2 position = physics.getBody().getPosition();
                float angle = physics.getBody().getAngle();

                // Umrechnung in Bildschirmkoordinaten
                visualNode.setTranslateX((position.x + offsetX) * GameObject.M_TO_PX * scale);
                visualNode.setTranslateY(visualNode.getScene().getHeight() - 
                    ((position.y + offsetY) * GameObject.M_TO_PX * scale));
                visualNode.setRotate(-Math.toDegrees(angle));
                visualNode.setScaleX(scale);
                visualNode.setScaleY(scale);
            }
        }
    }

    /**
     * Setzt den visuellen Node.
     * @param node Der neue JavaFX-Node
     */
    public void setVisualNode(Node node) {
        this.visualNode = node;
        updateTransform();
    }

    /**
     * Gibt den visuellen Node zurück.
     * @return Der JavaFX-Node
     */
    public Node getVisualNode() {
        return visualNode;
    }

    /**
     * Aktiviert oder deaktiviert das Folgen der Physik-Position.
     * @param follow true wenn der Node der Physik folgen soll
     */
    public void setFollowPhysics(boolean follow) {
        this.followPhysics = follow;
    }

    /**
     * Setzt den X-Offset für die Darstellung.
     * @param offset Der X-Offset in Metern
     */
    public void setOffsetX(float offset) {
        this.offsetX = offset;
        updateTransform();
    }

    /**
     * Setzt den Y-Offset für die Darstellung.
     * @param offset Der Y-Offset in Metern
     */
    public void setOffsetY(float offset) {
        this.offsetY = offset;
        updateTransform();
    }

    /**
     * Setzt die Skalierung der Darstellung.
     * @param scale Die Skalierung (1.0 = normale Größe)
     */
    public void setScale(float scale) {
        this.scale = scale;
        updateTransform();
    }

    /**
     * Rendert das GameObject.
     * @param gc Der GraphicsContext zum Rendern
     */
    public void render(GraphicsContext gc) {
        if (!visible || !isEnabled()) return;

        GameObject obj = getGameObject();
        if (obj == null) return;

        PhysicsComponent physics = obj.getPhysicsComponent();
        if (physics == null || physics.getBody() == null) return;

        // Save the current graphics state
        gc.save();

        // Set rendering properties
        gc.setFill(fillColor);
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);

        // Get position and convert to screen coordinates
        Vec2 position = physics.getBody().getPosition();
        float angle = physics.getBody().getAngle();
        
        // Convert physics coordinates to screen coordinates
        double screenX = position.x * GameObject.M_TO_PX;
        double screenY = gc.getCanvas().getHeight() - (position.y * GameObject.M_TO_PX);

        // Transform context for rotation
        gc.translate(screenX, screenY);
        gc.rotate(-Math.toDegrees(angle));

        // Draw based on the physics shape
        if (physics.getBody().getFixtureList() != null) {
            switch (physics.getBody().getFixtureList().getShape().getType()) {
                case CIRCLE:
                    float radius = physics.getBody().getFixtureList().getShape().getRadius();
                    double screenRadius = radius * GameObject.M_TO_PX;
                    gc.fillOval(-screenRadius, -screenRadius, screenRadius * 2, screenRadius * 2);
                    gc.strokeOval(-screenRadius, -screenRadius, screenRadius * 2, screenRadius * 2);
                    break;
                case POLYGON:
                    // Add polygon rendering if needed
                    break;
                default:
                    // Unsupported shape type
                    break;
            }
        }

        // Restore the graphics state
        gc.restore();
    }
    
    /**
     * Setzt die Füllfarbe des Objekts.
     * @param color Die neue Füllfarbe
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }
    
    /**
     * Setzt die Randfarbe des Objekts.
     * @param color Die neue Randfarbe
     */
    public void setStrokeColor(Color color) {
        this.strokeColor = color;
    }
    
    /**
     * Setzt die Breite des Objekts.
     * @param width Die neue Breite in Pixeln
     */
    public void setWidth(double width) {
        this.width = width;
    }
    
    /**
     * Setzt die Höhe des Objekts.
     * @param height Die neue Höhe in Pixeln
     */
    public void setHeight(double height) {
        this.height = height;
    }
    
    /**
     * Gibt die aktuelle Breite zurück.
     * @return Die Breite in Pixeln
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Gibt die aktuelle Höhe zurück.
     * @return Die Höhe in Pixeln
     */
    public double getHeight() {
        return height;
    }

    /**
     * Setzt die Randbreite.
     */
    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
    }

    /**
     * Gibt die Randbreite zurück.
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Setzt die Sichtbarkeit.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gibt die Sichtbarkeit zurück.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Setzt die Transparenz.
     * @param opacity Wert zwischen 0.0 (transparent) und 1.0 (undurchsichtig)
     */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    /**
     * Gibt die Transparenz zurück.
     */
    public double getOpacity() {
        return opacity;
    }

    @Override
    public void reset() {
        // Standardwerte für Rendering-Eigenschaften wiederherstellen
        visible = true;
        opacity = 1.0;
        // Weitere Rendering-spezifische Resets können hier hinzugefügt werden
    }
} 