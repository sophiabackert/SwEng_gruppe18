package mm.objects;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import mm.objects.components.RenderComponent;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.CollisionComponent;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.util.*;

/**
 * Basisklasse für alle Spielobjekte mit einem Komponenten-System.
 */
public abstract class GameObject {
    // Konstanten für die Umrechnung zwischen Physik- und Bildschirmkoordinaten
    public static final float M_TO_PX = 80f;
    public static final float PX_TO_M = 1f / M_TO_PX;

    // Basis-Eigenschaften
    private String id;
    private String name;
    private boolean active;

    // Physik-Komponenten
    private Body body;
    private BodyDef bodyDef;
    private Shape shape;

    // Rendering-Komponenten
    private Node visualNode;

    // Komponenten-System
    private final Map<Class<? extends Component>, Component> components;
    private final List<Component> componentList;
    private Vec2 position;
    private float rotation;
    private boolean isDestroyed;

    // Physikalische Eigenschaften
    private float density = 1.0f;
    private float friction = 0.3f;
    private float restitution = 0.5f;

    /**
     * Erstellt ein neues GameObject an der Position (0,0).
     */
    protected GameObject() {
        this(new Vec2(0, 0));
    }

    /**
     * Erstellt ein neues GameObject an der angegebenen Position.
     * @param position Die Startposition des Objekts
     */
    protected GameObject(Vec2 position) {
        this.id = UUID.randomUUID().toString();
        this.active = true;
        this.components = new HashMap<>();
        this.componentList = new ArrayList<>();
        this.position = position;
        this.rotation = 0.0f;
        this.isDestroyed = false;
    }

    /**
     * Initialisiert die Physik-Komponenten des Objekts.
     * @param world Die JBox2D-Welt
     */
    public void initializePhysics(World world) {
        if (bodyDef != null && world != null) {
            body = world.createBody(bodyDef);
            body.setUserData(this);
        }
    }

    /**
     * Aktualisiert das Objekt und seine Komponenten.
     * @param deltaTime Die vergangene Zeit seit dem letzten Update in Sekunden
     */
    public void update(float deltaTime) {
        if (!active || isDestroyed) return;

        // Komponenten aktualisieren
        for (Component component : componentList) {
            if (component.isEnabled()) {
                component.update(deltaTime);
            }
        }

        // Visuelle Darstellung aktualisieren
        updateVisuals();
    }

    /**
     * Aktualisiert die visuelle Darstellung basierend auf der Physik-Position.
     */
    protected void updateVisuals() {
        if (body != null && visualNode != null) {
            Vec2 position = body.getPosition();
            visualNode.setTranslateX(position.x * M_TO_PX);
            visualNode.setTranslateY(visualNode.getScene().getHeight() - (position.y * M_TO_PX));
            visualNode.setRotate(-Math.toDegrees(body.getAngle()));
        }
    }

    /**
     * Fügt eine Komponente zum GameObject hinzu.
     * @param component Die hinzuzufügende Komponente
     * @param <T> Der Typ der Komponente
     * @return Die hinzugefügte Komponente
     */
    public <T extends Component> T addComponent(T component) {
        if (component != null) {
            components.put(component.getClass(), component);
            componentList.add(component);
            component.onAttach();
        }
        return component;
    }

    /**
     * Entfernt eine Komponente vom GameObject.
     * @param componentClass Die Klasse der zu entfernenden Komponente
     * @param <T> Der Typ der Komponente
     * @return Die entfernte Komponente oder null
     */
    public <T extends Component> T removeComponent(Class<T> componentClass) {
        Component component = components.remove(componentClass);
        if (component != null) {
            componentList.remove(component);
            component.onDetach();
        }
        return componentClass.cast(component);
    }

    /**
     * Gibt eine Komponente zurück.
     * @param componentClass Die Klasse der gewünschten Komponente
     * @param <T> Der Typ der Komponente
     * @return Die Komponente oder null
     */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    /**
     * Prüft, ob das GameObject eine bestimmte Komponente hat.
     * @param componentClass Die zu prüfende Komponenten-Klasse
     * @return true wenn die Komponente vorhanden ist
     */
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    /**
     * Rendert das GameObject.
     * @param gc Der GraphicsContext zum Rendern
     */
    public void render(GraphicsContext gc) {
        if (!active || isDestroyed) return;
        
        RenderComponent renderComponent = getComponent(RenderComponent.class);
        if (renderComponent != null) {
            renderComponent.render(gc);
        }
    }

    /**
     * Convenience getter for the RenderComponent.
     * @return The RenderComponent or null if not present
     */
    public RenderComponent getRenderComponent() {
        return getComponent(RenderComponent.class);
    }

    /**
     * Convenience getter for the PhysicsComponent.
     * @return The PhysicsComponent or null if not present
     */
    public PhysicsComponent getPhysicsComponent() {
        return getComponent(PhysicsComponent.class);
    }

    /**
     * Convenience getter for the CollisionComponent.
     * @return The CollisionComponent or null if not present
     */
    public CollisionComponent getCollisionComponent() {
        return getComponent(CollisionComponent.class);
    }

    // Getter und Setter

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Body getBody() {
        return body;
    }

    public void setBodyDef(BodyDef bodyDef) {
        this.bodyDef = bodyDef;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Node getVisualNode() {
        return visualNode;
    }

    public void setVisualNode(Node visualNode) {
        this.visualNode = visualNode;
    }

    /**
     * Setzt die Position des GameObjects.
     * @param x Die x-Koordinate
     * @param y Die y-Koordinate
     */
    public void setPosition(float x, float y) {
        this.position = new Vec2(x, y);
        if (body != null) {
            body.setTransform(new Vec2(x, y), body.getAngle());
            updateVisuals();
        }
    }

    /**
     * Setzt die Rotation des GameObjects.
     * @param angle Winkel in Grad
     */
    public void setRotation(float angle) {
        this.rotation = angle;
        if (body != null) {
            body.setTransform(body.getPosition(), (float) Math.toRadians(angle));
            updateVisuals();
        }
    }

    /**
     * Gibt die aktuelle Position zurück.
     * @return Die Position als Vec2
     */
    public Vec2 getPosition() {
        return position;
    }

    /**
     * Gibt die x-Koordinate zurück.
     */
    public float getX() {
        return position.x;
    }

    /**
     * Gibt die y-Koordinate zurück.
     */
    public float getY() {
        return position.y;
    }

    /**
     * Gibt die aktuelle Rotation in Grad zurück.
     * @return Die Rotation in Grad
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Markiert das GameObject als zerstört.
     */
    public void destroy() {
        isDestroyed = true;
    }

    /**
     * Prüft, ob das GameObject zerstört wurde.
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Setzt das GameObject auf seinen Ausgangszustand zurück.
     */
    public void reset() {
        isDestroyed = false;
        rotation = 0.0f;
        // Komponenten zurücksetzen
        for (Component component : components.values()) {
            component.reset();
        }
    }

    /**
     * Setzt die Dichte des Objekts.
     */
    public void setDensity(float density) {
        this.density = density;
        PhysicsComponent physics = getPhysicsComponent();
        if (physics != null && physics.getFixtureDef() != null) {
            physics.getFixtureDef().density = density;
        }
    }

    /**
     * Gibt die Dichte des Objekts zurück.
     */
    public float getDensity() {
        return density;
    }

    /**
     * Setzt den Reibungskoeffizienten des Objekts.
     */
    public void setFriction(float friction) {
        this.friction = friction;
        PhysicsComponent physics = getPhysicsComponent();
        if (physics != null && physics.getFixtureDef() != null) {
            physics.getFixtureDef().friction = friction;
        }
    }

    /**
     * Gibt den Reibungskoeffizienten des Objekts zurück.
     */
    public float getFriction() {
        return friction;
    }

    /**
     * Setzt den Restitutionskoeffizienten des Objekts.
     */
    public void setRestitution(float restitution) {
        this.restitution = restitution;
        PhysicsComponent physics = getPhysicsComponent();
        if (physics != null && physics.getFixtureDef() != null) {
            physics.getFixtureDef().restitution = restitution;
        }
    }

    /**
     * Gibt den Restitutionskoeffizienten des Objekts zurück.
     */
    public float getRestitution() {
        return restitution;
    }
} 