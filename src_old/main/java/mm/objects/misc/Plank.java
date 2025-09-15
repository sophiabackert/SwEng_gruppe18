package mm.objects.misc;

import javafx.scene.paint.Color;
import mm.objects.GameObject;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * Eine Planke für das Spiel.
 */
public class Plank extends GameObject {
    private static final float DEFAULT_WIDTH = 2.0f;
    private static final float DEFAULT_HEIGHT = 0.2f;
    private static final float DEFAULT_DENSITY = 1.0f;
    private static final float DEFAULT_FRICTION = 0.3f;
    private static final float DEFAULT_RESTITUTION = 0.5f;
    private static final Color DEFAULT_COLOR = Color.SADDLEBROWN;

    private final float width;
    private final float height;

    /**
     * Erstellt eine neue Planke mit Standardwerten.
     * @param position Die Position der Planke
     */
    public Plank(Vec2 position) {
        this(position, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Erstellt eine neue Planke mit angegebener Größe.
     * @param position Die Position der Planke
     * @param width Die Breite der Planke
     * @param height Die Höhe der Planke
     */
    public Plank(Vec2 position, float width, float height) {
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
     * Gibt die Breite der Planke zurück.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gibt die Höhe der Planke zurück.
     */
    public float getHeight() {
        return height;
    }
}