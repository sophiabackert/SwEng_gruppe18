package mm.objects.containers;

import javafx.scene.paint.Color;
import mm.objects.GameObject;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * Eine Box-Container-Klasse für das Spiel.
 */
public class Box extends GameObject {
    private static final float DEFAULT_WIDTH = 1.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;
    private static final float DEFAULT_DENSITY = 1.0f;
    private static final float DEFAULT_FRICTION = 0.3f;
    private static final float DEFAULT_RESTITUTION = 0.5f;
    private static final Color DEFAULT_COLOR = Color.BROWN;

    private final float width;
    private final float height;

    /**
     * Erstellt eine neue Box mit Standardwerten.
     * @param position Die Position der Box
     */
    public Box(Vec2 position) {
        this(position, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Erstellt eine neue Box mit angegebener Größe.
     * @param position Die Position der Box
     * @param width Die Breite der Box
     * @param height Die Höhe der Box
     */
    public Box(Vec2 position, float width, float height) {
        super(position);
        this.width = width;
        this.height = height;
        initComponents();
    }

    private void initComponents() {
        // Physik-Komponente
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createBoxBody(width, height, DEFAULT_DENSITY, DEFAULT_FRICTION, DEFAULT_RESTITUTION, BodyType.DYNAMIC);
        addComponent(physics);

        // Render-Komponente
        RenderComponent render = new RenderComponent(this);
        render.setFillColor(DEFAULT_COLOR);
        addComponent(render);
    }

    /**
     * Gibt die Breite der Box zurück.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gibt die Höhe der Box zurück.
     */
    public float getHeight() {
        return height;
    }
}